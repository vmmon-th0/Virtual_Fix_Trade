package com.router;

import com.router.server.RouterServer;

public class Main {
    public static void main(String[] args) throws Exception {
        RouterServer router = new RouterServer(5000, 5001);
        router.runEventLoop();
    }
}