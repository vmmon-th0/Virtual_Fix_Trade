package com.router.interfaces;

import java.nio.channels.SocketChannel;
import java.util.Map;

public interface RouterServerActions {
    void sendMessage(String targetCompID, String message);
    Map<String, SocketChannel> getMarkets();
}
