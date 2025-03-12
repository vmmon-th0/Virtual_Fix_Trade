package com.core.fix.processor.handlers;

import com.core.fix.context.DeserializationContext;
import com.core.fix.processor.FixMessagePreProcessor;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.core.fix.factory.FixMessageFactory.FixMessage.SOH;

public class DeserializationHandler extends FixMessagePreProcessor {

    @Override
    protected void process(Object input) {
        if (input == null) {
            throw new IllegalArgumentException("FixMessage is null");
        }
        if (!(input instanceof String fixMessage)) {
            throw new IllegalArgumentException("Fix message is null or not instanceof of String");
        }
        Map<String, String> fields = new LinkedHashMap<>();
        String[] tagValues = fixMessage.split(SOH);

        for (String tagValue : tagValues) {
            if (tagValue.isEmpty()) {
                continue;
            }
            String[] keyValue = tagValue.split("=", 2);
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid field format: " + tagValue);
            }
            fields.put(keyValue[0], keyValue[1]);
        }
        DeserializationContext.setCurrentMessage(fields);
    }
}
