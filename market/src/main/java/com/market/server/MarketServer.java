package com.market.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class MarketServer {
    private SocketChannel socketChannel;
    private final String host;
    private Selector selector;
    private final int port;

    public MarketServer(String host, int port) {
        this.host = host;
        this.port = port;
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
    }

}
