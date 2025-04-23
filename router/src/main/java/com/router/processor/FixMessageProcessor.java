package com.router.processor;

import com.router.interfaces.FixMessageStrategy;
import com.router.strategy.StrategyRegistry;
import com.router.strategy.strategies.ListMarketsStrategy;
import com.router.strategy.strategies.LogonIdentifierStrategy;

import java.util.Map;

public class FixMessageProcessor {
    private FixMessageStrategy strategy;
    private static final StrategyRegistry registry = new StrategyRegistry();

    static {
        registry.registerStrategy("UD1", new LogonIdentifierStrategy());
        registry.registerStrategy("UD2", new ListMarketsStrategy());
    }

    public void process(Map<String, String> message) {
        String msgType = message.get("msgType");
        strategy = registry.getStrategy(msgType);
        if (strategy != null) {
            strategy.process(message);
        } else {
            System.out.println("No strategy found for MsgType: " + msgType);
        }
    }

}
