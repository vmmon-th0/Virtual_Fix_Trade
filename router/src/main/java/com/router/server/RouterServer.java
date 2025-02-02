package com.router.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class RouterServer {
    private final int marketPort;
    private final int brokerPort;

    private ServerSocketChannel marketServerChannel;
    private ServerSocketChannel brokerServerChannel;
    private Selector selector;

    public RouterServer(int marketPort, int brokerPort) {
        this.marketPort = marketPort;
        this.brokerPort = brokerPort;
    }

    private void readMessage(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = sc.read(buffer);
        if (bytesRead == -1) {
            System.out.println("Connection closed by the client : "
                    + sc.getRemoteAddress());
            sc.close();
            key.cancel();
            return;
        }

        buffer.flip();
        byte[] data = new byte[bytesRead];
        buffer.get(data);

        String message = new String(data, StandardCharsets.UTF_8);
        System.out.println("\nReceived from : " + sc.getRemoteAddress() + " : " + message);

        key.interestOps(SelectionKey.OP_WRITE);
        key.attach("Echo: " + message);
        buffer.clear();
    }

    private void acceptChannel(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel sc = serverChannel.accept();

        if (sc != null) {
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
            System.out.println("Accepted connection from " + sc.getRemoteAddress());
            switch (serverChannel.socket().getLocalPort()) {
                case 5000:
                    System.out.println("Market is connected.");
                    break;
                case 5001:
                    System.out.println("Broker is connected.");
                    break;
            }
        }
    }

    private void initChannels() throws IOException {
        marketServerChannel = ServerSocketChannel.open();
        brokerServerChannel = ServerSocketChannel.open();

        marketServerChannel.configureBlocking(false);
        brokerServerChannel.configureBlocking(false);

        marketServerChannel.bind(new InetSocketAddress(marketPort));
        brokerServerChannel.bind(new InetSocketAddress(brokerPort));
    }

    public void runEventLoop() throws IOException {
        try {

            initChannels();
            selector = Selector.open();

            marketServerChannel.register(selector, SelectionKey.OP_ACCEPT);
            brokerServerChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Listen on market port: " + marketPort + " & broker port: " + brokerPort);

            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        acceptChannel(key);
                    }

                    if (key.isReadable()) {
                        readMessage(key);
                    }

                    if (key.isWritable()) {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (marketServerChannel != null) {
                    marketServerChannel.close();
                }
                if (brokerServerChannel != null) {
                    brokerServerChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
