package com.router.strategy.strategies;

import com.router.interfaces.FixMessageStrategy;
import com.router.interfaces.RouterServerActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LogonIdentifierStrategy implements FixMessageStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogonIdentifierStrategy.class);

    @Override
    public void process(RouterServerActions routerServer, Map<String, String> fixMessage) {
        LOGGER.info("LogonIdentifierStrategy");
    }
}