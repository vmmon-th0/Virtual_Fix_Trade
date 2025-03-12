package com.router.context;

import java.nio.ByteBuffer;

public class ConnectionContext {

    private String id;
    private ByteBuffer buffer;

    public ConnectionContext() {
        this.buffer = ByteBuffer.allocate(1024);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ByteBuffer getReadBuffer() {
        return buffer;
    }
}