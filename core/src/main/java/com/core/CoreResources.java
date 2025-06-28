package com.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class CoreResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreResources.class);

    public static void main(String[] args) {
    }

    // todo: deserialize fix message, check validity (checksum compute)

    private static void connectToPort(String host, int port) {
        LOGGER.info("Attempting to connect to " + host + " on port " + port + "...");
        try (Socket socket = new Socket(host, port)) {
            LOGGER.info("Connected successfully to " + host + " on port " + port);
        } catch (IOException e) {
            LOGGER.error("Failed to connect to " + host + " on port " + port);
            e.printStackTrace();
        }
    }
}