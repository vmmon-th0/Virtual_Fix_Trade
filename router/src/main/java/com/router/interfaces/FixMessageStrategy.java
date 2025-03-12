package com.router.interfaces;

import java.util.Map;

public interface FixMessageStrategy {
    void process(Map<String, String> fixMessage);
}
