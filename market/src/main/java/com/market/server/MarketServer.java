package com.market.server;

import com.core.fix.factory.FixMessageFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class MarketServer {

    private String marketChannelId;
    private SocketChannel socketChannel;
    private final String host;
    private Selector selector;
    private final int port;

    public MarketServer(String host, int port, String marketChannelId) {
        this.host = host;
        this.port = port;
        this.marketChannelId = marketChannelId;
    }

    private void initChannel() throws IOException {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true); // while(!socketChannel.finishConnect()) alternative
            InetSocketAddress address = new InetSocketAddress(host, port);
            if (!socketChannel.connect(address)) {
                throw new IOException("Failed to connect within the time limit");
            }

            String logonMessage = FixMessageFactory.createLogonIdentifier(marketChannelId);
            ByteBuffer buffer = ByteBuffer.wrap(logonMessage.getBytes(StandardCharsets.UTF_8));

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw e;
        }
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
                        connectChannel(key);
                    } else if (key.isReadable()) {
                        readMessage(key);
                    }
                }
            }

        } catch (IOException e) {
            if (selector != null) {
                selector.close();
            }
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
    }

}
