package com.router.strategy.strategies;

import com.router.interfaces.FixMessageStrategy;
import com.router.interfaces.RouterServerActions;

import java.util.Map;

public class LogonIdentifierStrategy implements FixMessageStrategy {
    @Override
    public void process(RouterServerActions routerServer, Map<String, String> fixMessage) {
        System.out.println("LogonIdentifierStrategy");
    }
}