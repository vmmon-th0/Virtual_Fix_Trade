package com.router.interfaces;

import java.util.Map;

public interface FixMessageStrategy {
    void process(RouterServerActions routerServer, Map<String, String> fixMessage);
}
