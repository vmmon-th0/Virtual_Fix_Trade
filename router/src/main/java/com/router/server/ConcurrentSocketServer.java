package com.router.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentSocketServer {
    private int marketPort;
    private int brokerPort;

    private ServerSocket marketServerSocket;
    private ServerSocket brokerServerSocket;
    private ExecutorService executorService;

    public ConcurrentSocketServer(int marketPort, int brokerPort) {
        this.marketPort = marketPort;
        this.brokerPort = brokerPort;
    }

    public void start() {
        executorService = Executors.newFixedThreadPool(2);
        try {
            marketServerSocket = new ServerSocket(marketPort);
            brokerServerSocket = new ServerSocket(brokerPort);
            System.out.println("Listening on ports " + marketPort + " and " + brokerPort);

            executorService.submit(() -> handleConnections(marketServerSocket, marketPort));
            executorService.submit(() -> handleConnections(brokerServerSocket, brokerPort));
        } catch (BindException e) {
            System.err.println("Port already in use: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error starting servers: " + e.getMessage());
        }
    }

    private void handleConnections(ServerSocket serverSocket, int port) {
        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection on port " + port + ": " + clientSocket.getInetAddress());
                handleClient(clientSocket, port);
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    System.out.println("ServerSocket on port " + port + " closed. Exiting connection loop.");
                    break;
                }
                System.err.println("Error handling connection on port " + port);
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket, int port) {
        try {
            System.out.println("Handling client on port " + port);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket on port " + port);
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        System.out.println("Stopping server...");
        try {
            if (marketServerSocket != null && !marketServerSocket.isClosed()) {
                marketServerSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing marketServerSocket: " + e.getMessage());
        }
        try {
            if (brokerServerSocket != null && !brokerServerSocket.isClosed()) {
                brokerServerSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing brokerServerSocket: " + e.getMessage());
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
