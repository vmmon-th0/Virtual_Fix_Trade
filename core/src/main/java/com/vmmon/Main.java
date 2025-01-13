package com.vmmon;

import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        String host = "localhost";
        int[] ports = {5000, 5001};

        for (int port : ports) {
            connectToPort(host, port);
        }
    }

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