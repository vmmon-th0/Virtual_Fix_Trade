package com.core;

import java.io.IOException;
import java.net.Socket;

public class CoreResources {
    public static void main(String[] args) {
    }

    // todo: deserialize fix message, check validity (checksum compute)

    private static void connectToPort(String host, int port) {
        System.out.println("Attempting to connect to " + host + " on port " + port + "...");
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected successfully to " + host + " on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to connect to " + host + " on port " + port);
            e.printStackTrace();
        }
    }
}