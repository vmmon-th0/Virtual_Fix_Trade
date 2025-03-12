package com.core.fix.factory;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class FixMessageFactory {

    public static class FixMessage {
        private final Map<String, String> fields = new LinkedHashMap<>();
        public static final String SOH = "\u0001";

        public FixMessage addField(String tag, String value) {
            fields.put(tag, value);
            return this;
        }

        public String build() {

            int totalBytes = fields.entrySet().stream()
                    .skip(2)
                    .mapToInt(e -> e.getKey().getBytes(StandardCharsets.UTF_8).length
                            + e.getValue().getBytes(StandardCharsets.UTF_8).length)
                    .sum();

            totalBytes += (fields.size() - 2) * 2;
            fields.replace("9", String.valueOf(totalBytes));
            StringJoiner joiner = new StringJoiner(SOH);
            fields.forEach((tag, value) -> joiner.add(tag + "=" + value));

            String finalbuild = joiner + SOH;
            String checksum = computeChecksum(finalbuild);
            return finalbuild + "10=" + checksum + SOH;
        }

        public static String computeChecksum(String fixMessage) {
            int sum = 0;
            for (int i = 0; i < fixMessage.length(); i++) {
                sum += fixMessage.charAt(i);
            }
            int chksum = sum % 256;
            return String.format("%03d", chksum);
        }
    }

    public static FixMessage createFixStdHeader(String senderCompID, String targetCompID, String msgType) {
        FixMessage fixMessage = new FixMessage();
        fixMessage
                .addField("8", "FIX.4.4") // BeginString
                .addField("9", "0") // BodyLength
                .addField("35", msgType) // MsgType, ici un "New Order - Single" (Type D)
                .addField("49", senderCompID) // SenderCompID
                .addField("56", targetCompID) // TargetCompID
                .addField("34", "1") // MsgSeqNum
                .addField("52", getCurrentTimestamp()); // SendingTime
        return fixMessage;
    }

    public static String createNewOrder(String senderCompID, String targetCompID, String clOrdID, String symbol, double orderQty, char side, double price) {
        FixMessage fixMessage = createFixStdHeader(senderCompID, targetCompID, "D");
        fixMessage
                .addField("11", clOrdID) // ClOrdID (Client Order ID)
                .addField("55", symbol) // Symbol
                .addField("54", String.valueOf(side)) // Side (1=Buy, 2=Sell)
                .addField("38", String.valueOf(orderQty)) // OrderQty
                .addField("44", String.valueOf(price)); // Price
        return fixMessage.build();
    }

    public static String createLogon(String senderCompID, String targetCompID) {
        FixMessage fixMessage = createFixStdHeader(senderCompID, targetCompID, "A");
        fixMessage
                .addField("98", "0") // EncryptMethod
                .addField("108", "30"); // HeartBtInt
        return fixMessage.build();
    }

    public static String createLogonIdentifier(String identifier) {
        FixMessage fixMessage = createFixStdHeader("", "", "UD1"); // User Defined type
        fixMessage
                .addField("58", identifier); // Text
        return fixMessage.build();
    }

    public static String createListMarkets() {
        FixMessage fixMessage = createFixStdHeader("", "", "UD2");
        return fixMessage.build();
    }

    public static String createLogout(String senderCompID, String targetCompID) {
        FixMessage fixMessage = createFixStdHeader(senderCompID, targetCompID, "5");
        return fixMessage.build();
    }

    private static String getCurrentTimestamp() {
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")
                .format(java.time.LocalDateTime.now());
    }
}

