package com.core.fix.context;

import java.util.Map;

public class DeserializationContext {
    private static final ThreadLocal<Map<String,String>> currentMessage = new ThreadLocal<>();

    public static void setCurrentMessage(Map<String,String> message) {
        currentMessage.set(message);
    }

    public static Map<String,String> getCurrentMessage() {
        return currentMessage.get();
    }

    public static void clear() {
        currentMessage.remove();
    }
}
