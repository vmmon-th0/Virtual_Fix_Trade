package com.broker;

import com.broker.client.BrokerClient;

public class Main {
    public static void main(String[] args) throws Exception {
        BrokerClient brokerClient = new BrokerClient("localhost", 5001, "WOLFOFWALLSTREET");
        brokerClient.runEventLoop();
    }
}