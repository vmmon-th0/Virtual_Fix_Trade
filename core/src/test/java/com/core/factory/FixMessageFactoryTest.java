package com.core.factory;

import com.core.fix.factory.FixMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.core.fix.factory.FixMessageFactory.FixMessage.SOH;
import static com.core.fix.factory.FixMessageFactory.FixMessage.computeChecksum;

class FixMessageFactoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixMessageFactoryTest.class);

    @Test
    @DisplayName("Test standard FIX header creation (createFixStdHeader)")
    void testCreateFixStdHeader() {
        String senderCompID = "SENDER";
        String targetCompID = "TARGET";

        FixMessageFactory.FixMessage fixMsg = FixMessageFactory.createFixStdHeader(senderCompID, targetCompID, "D");
        String builtMessage = fixMsg.build();

        Assertions.assertTrue(builtMessage.contains("8=FIX.4.4"),
                "The BeginString (8=FIX.4.4) should be present in the message.");
        Assertions.assertTrue(builtMessage.contains("9="),
                "The BodyLength (9=) field should be present in the message.");
        Assertions.assertTrue(builtMessage.contains("35=D"),
                "The MsgType (35=D) should be present in the message.");
        Assertions.assertTrue(builtMessage.contains("49=" + senderCompID),
                "The SenderCompID (49=SENDER) should be present in the message.");
        Assertions.assertTrue(builtMessage.contains("56=" + targetCompID),
                "The TargetCompID (56=TARGET) should be present in the message.");
        Assertions.assertTrue(builtMessage.contains("34=1"),
                "The MsgSeqNum (34=1) should be present in the message.");
        Assertions.assertTrue(builtMessage.contains("52="),
                "The SendingTime (52=...) should be present in the message.");
        Assertions.assertTrue(builtMessage.matches(".*10=\\d{3}" + SOH + ".*"),
                "The CheckSum (10=XXX) should exist in the final message with 3 digits.");

        String bodyLengthValue = extractFieldValue(builtMessage, "9");
        Assertions.assertNotNull(bodyLengthValue, "Field 9 (BodyLength) should exist and have a value.");

        LOGGER.info("std header fix msg: " + builtMessage);
    }

    @Test
    @DisplayName("Test NewOrder creation (createNewOrder)")
    void testCreateNewOrder() {
        String senderCompID = "SENDER";
        String targetCompID = "TARGET";
        String clOrdID = "CLIENT_ORDER_ID";
        String symbol = "AAPL";
        double orderQty = 100.0;
        char side = '1';
        double price = 1.2345;

        String newOrder = FixMessageFactory.createNewOrder(
                senderCompID, targetCompID, clOrdID, symbol, orderQty, side, price);

        Assertions.assertTrue(newOrder.contains("35=D"),
                "The MsgType should be D (New Order Single).");
        Assertions.assertTrue(newOrder.contains("11=" + clOrdID),
                "The ClOrdID should be present.");
        Assertions.assertTrue(newOrder.contains("55=" + symbol),
                "The Symbol (55=AAPL) should be present.");
        Assertions.assertTrue(newOrder.contains("54=" + side),
                "The Side (54=1) should be present.");
        Assertions.assertTrue(newOrder.contains("38=" + orderQty),
                "The OrderQty (38=100.0) should be present.");
        Assertions.assertTrue(newOrder.contains("44=" + price),
                "The Price (44=1.2345) should be present.");
        Assertions.assertTrue(newOrder.matches(".*10=\\d{3}" + SOH),
                "The CheckSum (10=XXX) should exist at the end of the message with 3 digits.");

        LOGGER.info("new order fix msg: " + newOrder);
    }

    @Test
    @DisplayName("Test Logon creation (createLogon)")
    void testCreateLogon() {
        String senderCompID = "SENDER";
        String targetCompID = "TARGET";

        String logonMessage = FixMessageFactory.createLogon(senderCompID, targetCompID);

        Assertions.assertTrue(logonMessage.contains("35=A") && logonMessage.contains("98=0"),
                "The message should have a relevant MsgType or the EncryptMethod (98=0) for a Logon.");
        Assertions.assertTrue(logonMessage.contains("98=0"),
                "The EncryptMethod (98=0) should be present for a Logon.");
        Assertions.assertTrue(logonMessage.contains("108=30"),
                "The HeartBtInt (108=30) should be present for a Logon.");
        Assertions.assertTrue(logonMessage.matches(".*10=\\d{3}" + SOH),
                "The CheckSum (10=XXX) should be present at the end of the message with 3 digits.");

        LOGGER.info("logon fix msg: " + logonMessage);
    }

    @Test
    @DisplayName("Test Logout creation (createLogout)")
    void testCreateLogout() {
        String senderCompID = "SENDER";
        String targetCompID = "TARGET";

        String logoutMessage = FixMessageFactory.createLogout(senderCompID, targetCompID);

        Assertions.assertTrue(logoutMessage.contains("35=5"),
                "The message should have a relevant MsgType for a Logout.");
        Assertions.assertTrue(logoutMessage.matches(".*10=\\d{3}" + SOH),
                "The CheckSum (10=XXX) should be present at the end of the message with 3 digits.");

        LOGGER.info("logout fix msg: " + logoutMessage);
    }

    @Test
    @DisplayName("Test checksum validity for a FIX Logon message")
    void testChecksumValidity() {
        String logonMessage = FixMessageFactory.createLogon("SENDER", "TARGET");

        String actualChecksum = extractFieldValue(logonMessage, "10");
        Assertions.assertNotNull(actualChecksum, "Checksum field (10=) should be present in the message.");

        String[] fields = logonMessage.split(SOH);

        StringBuilder builderWithoutChecksum = new StringBuilder();
        for (String field : fields) {
            if (!field.startsWith("10=")) {
                builderWithoutChecksum.append(field).append(SOH);
            }
        }
        String recomputedChecksum = computeChecksum(builderWithoutChecksum.toString());
        Assertions.assertEquals(actualChecksum, recomputedChecksum,
                "The checksum in the message should match the recomputed checksum.");
    }

    private String extractFieldValue(String fixMessage, String tag) {
        String[] tagValues = fixMessage.split(SOH);
        for (String tagValue : tagValues) {
            if (tagValue.startsWith(tag + "=")) {
                return tagValue.substring(tagValue.indexOf('=') + 1);
            }
        }
        return null;
    }
}