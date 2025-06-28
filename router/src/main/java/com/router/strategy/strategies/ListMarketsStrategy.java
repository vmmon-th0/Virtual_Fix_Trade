package com.router.strategy.strategies;

import com.core.fix.factory.FixMessageFactory;
import com.router.interfaces.FixMessageStrategy;
import com.router.interfaces.RouterServerActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;

public class ListMarketsStrategy implements FixMessageStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListMarketsStrategy.class);

    @Override
    public void process(RouterServerActions routerServer, Map<String, String> fixMessage) {
        LOGGER.info("ListMarketsStrategy process");

        Map<String, SocketChannel> markets = routerServer.getMarkets();
        ArrayList<String> marketIds = new ArrayList<>(markets.keySet());
        String message = FixMessageFactory.createListMarkets("", "", marketIds);

        routerServer.sendMessage(fixMessage.get("49"), message);
    }
}
