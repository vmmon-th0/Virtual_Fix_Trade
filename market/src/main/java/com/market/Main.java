package com.market;

import com.market.server.MarketServer;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("                      _        _   \n" +
                " _ __ ___   __ _ _ __| | _____| |_ \n" +
                "| '_ ` _ \\ / _` | '__| |/ / _ \\ __|\n" +
                "| | | | | | (_| | |  |   <  __/ |_ \n" +
                "|_| |_| |_|\\__,_|_|  |_|\\_\\___|\\__|");
        MarketServer marketServer = new MarketServer("localhost", 5000, "STOCKMARKET404");
        marketServer.runEventLoop();
    }
}