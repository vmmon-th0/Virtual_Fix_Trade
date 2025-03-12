package com.core.fix.processor;

public abstract class FixMessagePreProcessor {

    private FixMessagePreProcessor next;

    public void setNext(FixMessagePreProcessor next) {
        this.next = next;
    }

    public void handle(Object input) {
        process(input);
        if (next != null) {
            next.process(input);
        }
    }

    protected abstract void process(Object input);
}

