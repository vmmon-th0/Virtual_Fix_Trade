package com.core.fix.processor.handlers;

import com.core.fix.processor.FixMessagePreProcessor;
import static com.core.fix.factory.FixMessageFactory.FixMessage.computeChecksum;

public class ValidationHandler extends FixMessagePreProcessor {

    @Override
    protected void process(Object input) {
        String checksumPrefix = "10=";
        String fixMessage = (String) input;
        int checksumStartIndex = fixMessage.lastIndexOf(checksumPrefix);
        if (checksumStartIndex == -1) {
            System.out.println("Fixed message is missing a checksum");
            return ;
        }
        String checksum = fixMessage.substring(checksumStartIndex + checksumPrefix.length(), fixMessage.length() - 1);
        String messageWithoutChecksum = fixMessage.substring(0, checksumStartIndex);
        String computedChecksum = computeChecksum(messageWithoutChecksum);

        System.out.println(checksum);
        System.out.println(computedChecksum);

        if (!computedChecksum.equals(checksum)) {
            throw new IllegalArgumentException("Invalid checksum");
        }
    }
}