package com.market.server;

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

public class MarketServer {

    private final String marketChannelId;
    private SocketChannel socketChannel;
    private final String host;
    private Selector selector;
    private final int port;

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketServer.class);

    public MarketServer(String host, int port, String marketChannelId) {

        LOGGER.info("                      _        _   \n" +
                " _ __ ___   __ _ _ __| | _____| |_ \n" +
                "| '_ ` _ \\ / _` | '__| |/ / _ \\ __|\n" +
                "| | | | | | (_| | |  |   <  __/ |_ \n" +
                "|_| |_| |_|\\__,_|_|  |_|\\_\\___|\\__|");

        this.host = host;
        this.port = port;
        this.marketChannelId = marketChannelId;
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

        LOGGER.info("Connecting to " + sc.getRemoteAddress());

        if (sc.finishConnect()) {
            LOGGER.info("Connection established with the router server : "
                    + sc.getRemoteAddress());
        }

        String logonMessage = FixMessageFactory.createLogonIdentifier(marketChannelId);
        ByteBuffer buffer = ByteBuffer.wrap(logonMessage.getBytes(StandardCharsets.US_ASCII));
        while (buffer.hasRemaining()) {
            sc.write(buffer);
        }
        LOGGER.info("Sent market identifier successfully");
        key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
    }

}
