package com.broker.client;

import com.core.fix.factory.FixMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

public class BrokerClient {
    private SocketChannel socketChannel;
    private Selector selector;
    private final int port;
    private final String host;
    private final String brokerChannelId;

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerClient.class);

    public BrokerClient(String host, int port, String brokerChannelId) {

        LOGGER.info(" _               _             \n" +
                "| |__  _ __ ___ | | _____ _ __ \n" +
                "| '_ \\| '__/ _ \\| |/ / _ \\ '__|\n" +
                "| |_) | | | (_) |   <  __/ |   \n" +
                "|_.__/|_|  \\___/|_|\\_\\___|_|   ");

        this.host = host;
        this.port = port;
        this.brokerChannelId = brokerChannelId;
    }

    private void initChannel() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(host, port);
        socketChannel.connect(address);
    }

    private void readMessage(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = sc.read(buffer);

        if (bytesRead == -1) {
            LOGGER.info("Connection closed by the server : "
            + sc.getRemoteAddress());
            sc.close();
            key.cancel();
            return;
        }

        buffer.flip();
        byte[] data = new byte[bytesRead];
        buffer.get(data);

        String message = new String(data);
        LOGGER.info("Received from : " + sc.getRemoteAddress() + " : " + message);
        buffer.clear();
    }

    public void runEventLoop() throws Exception {
        try {
            initChannel();
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            while (socketChannel.isOpen()) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (!key.isValid()) {
                        continue;
                    }
                    if ((key.readyOps() & SelectionKey.OP_CONNECT) != 0) {
                        connectChannel(key);
                    }
                    if ((key.readyOps() & SelectionKey.OP_READ) != 0) {
                        readMessage(key);
                    }
                    keyIterator.remove();
                }
            }

            LOGGER.info("Connection closed, see you soon in virtual FIX trading");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socketChannel.isOpen()) {
                socketChannel.close();
            }
            if (selector.isOpen()) {
                selector.close();
            }
        }
    }

    private void connectChannel(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        if (sc.finishConnect()) {
            LOGGER.info("Connection established with the router server : "
                    + sc.getRemoteAddress());

            String logonMessage = FixMessageFactory.createLogonIdentifier(brokerChannelId);
            ByteBuffer buffer = ByteBuffer.wrap(logonMessage.getBytes(StandardCharsets.US_ASCII));
            while (buffer.hasRemaining()) {
                sc.write(buffer);
            }
            LOGGER.info("Sent market identifier successfully");

            key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            new Thread(new WriterTask(sc, this)).start();
        }
    }


    private static class WriterTask implements Runnable {
        private final SocketChannel sc;
        private final BrokerClient brokerClient;

        public WriterTask(SocketChannel sc, BrokerClient brokerClient) {
            this.sc = sc;
            this.brokerClient = brokerClient;
        }

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            try {
                while (sc.isConnected()) {
                    String line = scanner.nextLine();
                    if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                        LOGGER.info("disconnection...");
                        sc.close();
                        this.brokerClient.selector.wakeup();
                        break;
                    } else if (line.equalsIgnoreCase("buy")) {

                        String senderCompID = "SENDER";
                        String targetCompID = "MARKET_SERVER";
                        String clOrdID = "CLIENT_ORDER_ID";
                        String symbol = "AAPL";
                        double orderQty = 100.0;
                        char side = '1';
                        double price = 1.2345;

                        String newOrder = FixMessageFactory.createNewOrder(
                                senderCompID, targetCompID, clOrdID, symbol, orderQty, side, price);

                        ByteBuffer buffer = ByteBuffer.wrap(newOrder.getBytes());
                        while(buffer.hasRemaining()){
                            sc.write(buffer);
                        }
                    } else if (line.equalsIgnoreCase("sell")) {

                    } else if (line.equalsIgnoreCase("list-markets")) {
                    } else {
                        LOGGER.info("line: " + line);
                        ByteBuffer buffer = ByteBuffer.wrap(line.getBytes(StandardCharsets.US_ASCII));
                        while(buffer.hasRemaining()){
                            sc.write(buffer);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                scanner.close();
            }
        }
    }
}
