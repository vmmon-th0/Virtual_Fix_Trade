package com.router.server;

import com.core.fix.config.FixPipelineConfig;
import com.core.fix.context.DeserializationContext;
import com.core.fix.processor.FixMessagePreProcessor;
import com.router.context.ConnectionContext;
import com.router.strategy.StrategyRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RouterServer {
    private final int marketPort;
    private final int brokerPort;

    private ServerSocketChannel marketSocketChannel;
    private ServerSocketChannel brokerSocketChannel;
    private Selector selector;

    private FixMessagePreProcessor fixMessagePreProcessor;

    public RouterServer(int marketPort, int brokerPort) {
        this.marketPort = marketPort;
        this.brokerPort = brokerPort;
        this.fixMessagePreProcessor = FixPipelineConfig.buildChain();
    }

//    todo: optimize in order to search only marketSocketChannel connections

    private void listMarkets() {
        for (SelectionKey key : selector.keys()) {
            Object attachment = key.attachment();
            if (attachment instanceof ConnectionContext) {
                System.out.println(((ConnectionContext) attachment).getId());
            }
        }
    }

    private void processMessage(String message) {
        this.fixMessagePreProcessor.handle(message);
        Map<String, String> fixMessage = DeserializationContext.getCurrentMessage();
        System.out.println("Parse after fixMessagePreProcessor :)");
        for (Map.Entry<String, String> entry : fixMessage.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("key: " + key + " value: " + value);
        }
//        this.strategyProcessor.processor();
    }

    private void readMessage(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        ConnectionContext connectionContext = (ConnectionContext) key.attachment();
        ByteBuffer buffer = connectionContext.getReadBuffer();

        int bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            System.out.println("Connection closed by the client : "
                    + socketChannel.getRemoteAddress());
            socketChannel.close();
            key.cancel();
            return;
        }

        buffer.flip();
        byte[] data = new byte[bytesRead];
        buffer.get(data);

        String message = new String(data, StandardCharsets.UTF_8);
        System.out.println("\nReceived from : " + socketChannel.getRemoteAddress() + " : " + message);

//        Todo: Optimize this part, another thread maybe for the execution depending on fixmessage
//        Todo: Move this part, and implement Optional String return value if required.
//        Todo: Add execution of the process in the main loop to seperate logic.
        buffer.clear();
        processMessage(message);
    }

    private void acceptChannel(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();

        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(new ConnectionContext());
            System.out.println("Accepted connection from " + socketChannel.getRemoteAddress());
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
        marketSocketChannel = ServerSocketChannel.open();
        brokerSocketChannel = ServerSocketChannel.open();

        marketSocketChannel.configureBlocking(false);
        brokerSocketChannel.configureBlocking(false);

        marketSocketChannel.bind(new InetSocketAddress(marketPort));
        brokerSocketChannel.bind(new InetSocketAddress(brokerPort));
    }

    public void runEventLoop() throws IOException {
        try {

            initChannels();
            selector = Selector.open();

            marketSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            brokerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Listen on market port: " + marketPort + " & broker port: " + brokerPort);

            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        acceptChannel(key);
                    }

                    if (key.isReadable()) {
                        readMessage(key);
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
                if (marketSocketChannel != null) {
                    marketSocketChannel.close();
                }
                if (brokerSocketChannel != null) {
                    brokerSocketChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
