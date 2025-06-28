package com.broker;

import com.broker.client.BrokerClient;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(" _               _             \n" +
                "| |__  _ __ ___ | | _____ _ __ \n" +
                "| '_ \\| '__/ _ \\| |/ / _ \\ '__|\n" +
                "| |_) | | | (_) |   <  __/ |   \n" +
                "|_.__/|_|  \\___/|_|\\_\\___|_|   ");
        BrokerClient brokerClient = new BrokerClient("localhost", 5001, "WOLFOFWALLSTREET");
        brokerClient.runEventLoop();
    }
}