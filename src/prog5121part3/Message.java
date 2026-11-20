/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package prog5121part3;

import java.util.Random;
import java.util.Objects;


public class Message {
    private final String messageId;
    private final String sender;
    private final String recipient;
    private final String content;
    private final String messageHash;
    private String flag; 

    public Message(String sender, String recipient, String content, String flag) {
        this(generateMessageId(), sender, recipient, content, flag, null);
    }

    public Message(String messageId, String sender, String recipient, String content, String flag, String explicitHash) {
        this.messageId = (messageId == null || messageId.isBlank()) ? generateMessageId() : messageId.trim();
        this.sender = (sender == null || sender.isBlank()) ? "SystemUser" : sender.trim();
        this.recipient = (recipient == null) ? "" : recipient.trim();
        this.content = (content == null) ? "" : content;
        this.flag = (flag == null) ? "Stored" : flag.trim();
        this.messageHash = (explicitHash != null && !explicitHash.isBlank())
                ? explicitHash.trim()
                : createMessageHash();
    }

    private static String generateMessageId() {
        Random random = new Random();
        long num = 1_000_000_000L + (long) (random.nextDouble() * 9_000_000_000L);
        return String.valueOf(num);
    }

    public final String createMessageHash() {
        String prefixId = messageId.length() >= 2 ? messageId.substring(0, 2) : String.format("%-2s", messageId).replace(' ', '0');
        String[] words = content.trim().split("\\s+");
        String firstWord = (words.length > 0 && !words[0].isEmpty()) ? words[0].replaceAll("[^a-zA-Z0-9]", "") : "MSG";
        String lastWord = (words.length > 0 && !words[words.length - 1].isEmpty())
                ? words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "")
                : "END";
        if (firstWord.isEmpty()) firstWord = "MSG";
        if (lastWord.isEmpty()) lastWord = "END";
        return (prefixId + ":" + firstWord + lastWord).toUpperCase();
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message other)) return false;
        return Objects.equals(messageHash, other.messageHash)
                && Objects.equals(messageId, other.messageId)
                && Objects.equals(recipient, other.recipient)
                && Objects.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, recipient, content, messageHash);
    }
}

