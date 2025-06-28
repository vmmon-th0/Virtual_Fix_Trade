package com.core.fix.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FixMessagePreProcessor {

    private FixMessagePreProcessor next;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

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

