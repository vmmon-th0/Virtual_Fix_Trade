package com.router.server;

import com.core.fix.config.FixPipelineConfig;
import com.core.fix.context.DeserializationContext;
import com.core.fix.factory.FixMessageFactory;
import com.core.fix.processor.FixMessagePreProcessor;
import com.router.context.ConnectionContext;
import com.router.interfaces.RouterServerActions;
import com.router.processor.FixMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RouterServer implements RouterServerActions {
    private ServerSocketChannel marketSocketChannel;
    private ServerSocketChannel brokerSocketChannel;
    private Selector selector;

    private final FixMessageProcessor fixMessageProcessor;
    private final FixMessagePreProcessor fixMessagePreProcessor;

    private final int marketPort;
    private final int brokerPort;
    private static final String routerId = "ROUTER";

//    Todo: check thread use cases
    Map<String, SocketChannel> markets = new ConcurrentHashMap<>();
    Map<String, SocketChannel> brokers = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterServer.class);

    public Map<String, SocketChannel> getMarkets() {
        return markets;
    }

    public RouterServer(int marketPort, int brokerPort) {
        LOGGER.info(" ____   ___  _   _ _____ _____ ____  \n" +
                "|  _ \\ / _ \\| | | |_   _| ____|  _ \\ \n" +
                "| |_) | | | | | | | | | |  _| | |_) |\n" +
                "|  _ <| |_| | |_| | | | | |___|  _ < \n" +
                "|_| \\_\\\\___/ \\___/  |_| |_____|_| \\_\\");

        this.marketPort = marketPort;
        this.brokerPort = brokerPort;
        this.fixMessagePreProcessor = FixPipelineConfig.buildChain();
        this.fixMessageProcessor = new FixMessageProcessor(this);
    }

    private Optional<Map<String, String>> processMessage(String message) {
        this.fixMessagePreProcessor.handle(message);
        Map<String, String> fixMessage = DeserializationContext.getCurrentMessage();
        LOGGER.info("Parse after fixMessagePreProcessor :)");
        for (Map.Entry<String, String> entry : fixMessage.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            LOGGER.info("key: " + key + " value: " + value);
        }
        return Optional.ofNullable(this.fixMessageProcessor.process(fixMessage)).map(msgType -> fixMessage);
    }

    private void readMessage(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        ConnectionContext connectionContext = (ConnectionContext) key.attachment();
        ByteBuffer buffer = connectionContext.getReadBuffer();

        int bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            LOGGER.info("Connection closed by the client : "
                    + socketChannel.getRemoteAddress());
            socketChannel.close();
            key.cancel();
            return;
        }

        buffer.flip();
        byte[] data = new byte[bytesRead];
        buffer.get(data);

        String message = new String(data, StandardCharsets.US_ASCII);
        LOGGER.info("\nReceived from : " + socketChannel.getRemoteAddress() + " : " + message);

        buffer.clear();
        Optional<Map<String, String>> fixMessage = processMessage(message);

        if (fixMessage.isPresent()) {

            String id = fixMessage.map(m -> m.get("58")).orElse(null);
            connectionContext.setId(id);

            int localPort = ((InetSocketAddress) socketChannel.getLocalAddress()).getPort();

            LOGGER.info("Remote port : " + localPort);

            switch (localPort) {
                case 5000:
                    LOGGER.info(String.format("market: %s is now identified by the router", id));
                    markets.put(id, socketChannel);
                    break;
                case 5001:
                    LOGGER.info(String.format("broker: %s is now identified by the router", id));
                    brokers.put(id, socketChannel);
                    break;
            }

            String report = FixMessageFactory.createLogonIdentifierReport(routerId, id, "Registered successfully");
            this.sendMessage(id, report);
        }
    }

    @Override
    public void sendMessage(String targetCompID, String message) {
        try {
            LOGGER.info("Sending to targetCompID: " + targetCompID + " | message: " + message);

            SocketChannel sc = brokers.get(targetCompID);
            if (sc == null) {
                sc = markets.get(targetCompID);
            }

            if (sc == null) {
                LOGGER.error("No SocketChannel found for targetCompID: " + targetCompID);
                return;
            }
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.US_ASCII));
            while (buffer.hasRemaining()) {
                sc.write(buffer);
            }
        } catch (IOException e) {
            LOGGER.error("Error sending message to " + targetCompID + ": " + e.getMessage());
        }
    }


    private void acceptChannel(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();

        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(new ConnectionContext());
            LOGGER.info("Accepted connection from " + socketChannel.getRemoteAddress());
            switch (serverChannel.socket().getLocalPort()) {
                case 5000:
                    LOGGER.info("Market is connected.");
                    break;
                case 5001:
                    LOGGER.info("Broker is connected.");
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

            LOGGER.info("Listen on market port: " + marketPort + " & broker port: " + brokerPort);

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
