package com.router.processor;

import com.router.interfaces.FixMessageStrategy;
import com.router.interfaces.RouterServerActions;
import com.router.server.RouterServer;
import com.router.strategy.StrategyRegistry;
import com.router.strategy.strategies.ListMarketsStrategy;
import com.router.strategy.strategies.LogonIdentifierStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FixMessageProcessor {
    private FixMessageStrategy strategy;
    private RouterServerActions routerServer;
    private static final StrategyRegistry registry = new StrategyRegistry();

    private static final Logger LOGGER = LoggerFactory.getLogger(FixMessageProcessor.class);

    static {
        registry.registerStrategy("UD1", new LogonIdentifierStrategy());
        registry.registerStrategy("UD2", new ListMarketsStrategy());
    }

    public FixMessageProcessor(RouterServer routerServer) {
        this.routerServer = routerServer;
    }

    public Optional<String> process(Map<String, String> message) {
        String msgType = message.get("35");

        LOGGER.info("Fix Message process read: " +  msgType);

        if (Objects.equals(msgType, "UD1") || Objects.equals(msgType, "UD2")) {
            return msgType.describeConstable();
        }

        strategy = registry.getStrategy(msgType);
        if (strategy != null) {
            strategy.process(routerServer, message);
        } else {
            LOGGER.info("No strategy found for MsgType: " + msgType);
        }
        return Optional.empty();
    }

}
