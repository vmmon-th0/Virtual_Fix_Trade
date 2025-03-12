package com.router.interfaces;

import com.core.fix.factory.FixMessageFactory;

interface FixMessageProcessor {
    void process(FixMessageFactory.FixMessage fixMessage);
}
