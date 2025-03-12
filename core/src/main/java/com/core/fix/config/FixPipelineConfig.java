package com.core.fix.config;

import com.core.fix.processor.FixMessagePreProcessor;
import com.core.fix.processor.handlers.LoggingHandler;
import com.core.fix.processor.handlers.ValidationHandler;
import com.core.fix.processor.handlers.DeserializationHandler;

public class FixPipelineConfig {
    public static FixMessagePreProcessor buildChain() {
        FixMessagePreProcessor deserializationHandler = new DeserializationHandler();
        FixMessagePreProcessor validationHandler = new ValidationHandler();
        FixMessagePreProcessor loggingHandler = new LoggingHandler();

        deserializationHandler.setNext(validationHandler);
        validationHandler.setNext(loggingHandler);

        return deserializationHandler;
    }
}
