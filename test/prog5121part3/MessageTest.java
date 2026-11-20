package prog5121part3;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

/**
 *
 * @author Student
 */


public class MessageTest {
    private MessageReportManager manager;
    private Message msg1;
    private Message msg2;
    private Message msg4;

@Before
public void setUp() {
    manager = new MessageReportManager();

    msg1 = new Message("1000000001", "Developer", "+27834557896", "Did you get the cake?", "Sent", null);
    msg2 = new Message("1000000002", "Developer", "+27838884567",
            "Where are you? You are late! I have asked you to be on time.", "Stored", null);
        Message msg3 = new Message("1000000003", "Developer", "+27834484567",
                "Yohoooo, I am at your gate.", "Disregard", null);
    msg4 = new Message("0838884567", "Developer", "0838884567",
            "It is dinner time !", "Sent", null);
        Message msg5 = new Message("1000000005", "Developer", "+27838884567",
                "Ok, I am leaving without you.", "Stored", null);

    manager.recordMessage(msg1);
    manager.recordMessage(msg2);
    manager.recordMessage(msg3);
    manager.recordMessage(msg4);
    manager.recordMessage(msg5);
}
    @Test
    public void testSentMessagesArrayPopulated() {
        Message[] sent = manager.getSentMessages();
        assertEquals(2, sent.length);
        assertEquals("Did you get the cake?", sent[0].getContent());
        assertEquals("It is dinner time !", sent[1].getContent());
    }

    @Test
    public void testDisplayLongestMessage() {
        String expected = "Where are you? You are late! I have asked you to be on time.";
        assertEquals(expected, manager.displayLongestMessage());
    }

    @Test
    public void testSearchByMessageId() {
        String result = manager.searchByMessageId("0838884567");
        assertTrue(result.contains("It is dinner time !"));
    }

    @Test
    public void testSearchMessagesByRecipient() {
        String result = manager.searchMessagesByRecipient("+27838884567");
        assertTrue(result.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(result.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testDeleteMessageUsingHash() {
        String response = manager.deleteMessageByHash(msg2.getMessageHash());
        assertEquals(
                "Message \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                response
        );
    }

    @Test
    public void testDisplayReportContainsHashRecipientAndMessage() {
        String report = manager.displayMessageReport();
        assertTrue(report.contains(msg1.getMessageHash()));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains(msg4.getMessageHash()));
        assertTrue(report.contains("It is dinner time !"));
    }

    @Test
    public void testLoadStoredMessagesFromJson() {
        String sampleJson = """
                [
                  {
                    "messageId": "9000000001",
                    "sender": "Khensani",
                    "recipient": "+27838884567",
                    "message": "Stored from JSON payload",
                    "messageHash": "90:STOREDPAYLOAD"
                  }
                ]
                """;
        int loaded = manager.loadStoredMessagesFromJsonString(sampleJson);
        assertEquals(1, loaded);
        assertEquals("Stored from JSON payload", manager.getStoredMessages()[manager.getStoredMessages().length - 1].getContent());
    }
}
