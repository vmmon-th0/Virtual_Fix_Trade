package com.router.strategy;

import com.router.interfaces.FixMessageStrategy;

import java.util.HashMap;
import java.util.Map;

public class StrategyRegistry {

    private Map<String, FixMessageStrategy> strategies = new HashMap<>();

    public void registerStrategy(String msgType, FixMessageStrategy strategy) {
        strategies.put(msgType, strategy);
    }

    public FixMessageStrategy getStrategy(String msgType) {
        return strategies.get(msgType);
    }
}
