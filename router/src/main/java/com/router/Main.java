package com.router;

import com.router.server.ConcurrentSocketServer;

public class Main {
    public static void main(String[] args) {
        System.out.println(" ____   ___  _   _ _____ _____ ____  \n" +
                "|  _ \\ / _ \\| | | |_   _| ____|  _ \\ \n" +
                "| |_) | | | | | | | | | |  _| | |_) |\n" +
                "|  _ <| |_| | |_| | | | | |___|  _ < \n" +
                "|_| \\_\\\\___/ \\___/  |_| |_____|_| \\_\\");

        ConcurrentSocketServer concurrentSocketServer = new ConcurrentSocketServer(5000, 5001);
        concurrentSocketServer.start();
    }
}