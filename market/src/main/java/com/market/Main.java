package com.market;

import com.market.server.MarketServer;

public class Main {
    public static void main(String[] args) throws Exception {
        MarketServer marketServer = new MarketServer("localhost", 5000, "STOCKMARKET404");
        marketServer.runEventLoop();
    }
}