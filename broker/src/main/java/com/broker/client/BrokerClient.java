package com.broker.client;

import com.core.fix.factory.FixMessageFactory;

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
    private final String host;
    private Selector selector;
    private final int port;
    private String currentMarketId;

    public BrokerClient(String host, int port) {
        this.host = host;
        this.port = port;
        currentMarketId = "MARKET_SERVER";
    }

    private void initChannel() throws IOException {
        socketChannel = socketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
    }

    private void readMessage(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = sc.read(buffer);

        if (bytesRead == -1) {
            System.out.println("Connection closed by the server : "
            + sc.getRemoteAddress());
            sc.close();
            key.cancel();
            return;
        }

        buffer.flip();
        byte[] data = new byte[bytesRead];
        buffer.get(data);

        String message = new String(data);
        System.out.println("Received from : " + sc.getRemoteAddress() + " : " + message);
        buffer.clear();
    }

    public void runEventLoop() throws Exception {
        try {
            initChannel();
            selector = Selector.open();

            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            while (true) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isConnectable()) {
                        System.out.println("key is connectable");
                        connectChannel(key);
                    } else if (key.isReadable()) {
                        readMessage(key);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectChannel(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        sc.configureBlocking(false);

        if (sc.isConnectionPending()) {
            sc.finishConnect();
            System.out.println("Connection established with the router server : "
                    + sc.getRemoteAddress());
        }

        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        new Thread (new WriterTask(sc, this)).start();
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
                        System.out.println("disconnection...");
                        sc.close();
                        // Todo: Exit the process properly
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
                        String listMarkets = FixMessageFactory.createListMarkets();

                        ByteBuffer buffer = ByteBuffer.wrap(listMarkets.getBytes());
                        while(buffer.hasRemaining()){
                            sc.write(buffer);
                        }
                    } else {
                        System.out.println("line: " + line);
                        ByteBuffer buffer = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
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
