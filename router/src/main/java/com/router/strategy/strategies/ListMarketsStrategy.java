package com.router.strategy.strategies;

import com.core.fix.factory.FixMessageFactory;
import com.router.interfaces.FixMessageStrategy;
import com.router.interfaces.RouterServerActions;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;

public class ListMarketsStrategy implements FixMessageStrategy {
    @Override
    public void process(RouterServerActions routerServer, Map<String, String> fixMessage) {
        System.out.println("ListMarketsStrategy process");

        Map<String, SocketChannel> markets = routerServer.getMarkets();
        ArrayList<String> marketIds = new ArrayList<>(markets.keySet());
        String message = FixMessageFactory.createListMarkets("", "", marketIds);

        routerServer.sendMessage(fixMessage.get("49"), message);
    }
}
