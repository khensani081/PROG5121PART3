/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package prog5121part3;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReportManager {
    private Message[] sentMessages = new Message[0];
    private Message[] disregardedMessages = new Message[0];
    private Message[] storedMessages = new Message[0];
    private String[] messageHashes = new String[0];
    private String[] messageIds = new String[0];

    public void recordMessage(Message msg) {
        if (msg == null) return;

        addMessageId(msg.getMessageId());
        addMessageHash(msg.getMessageHash());

        String flagNormalized = msg.getFlag().trim().toLowerCase();
        switch (flagNormalized) {
            case "sent" -> sentMessages = appendMessage(sentMessages, msg);
            case "disregard", "disregarded" -> disregardedMessages = appendMessage(disregardedMessages, msg);
            case "stored" -> storedMessages = appendMessage(storedMessages, msg);
            default -> storedMessages = appendMessage(storedMessages, msg);
        }
    }

    public int loadStoredMessagesFromJson(Path jsonPath) throws IOException {
        if (!Files.exists(jsonPath)) {
            return 0;
        }
        String rawJson = Files.readString(jsonPath, StandardCharsets.UTF_8);
        return loadStoredMessagesFromJsonString(rawJson);
    }

    public int loadStoredMessagesFromJsonString(String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) {
            return 0;
        }

        int loadedCount = 0;
        Pattern objectPattern = Pattern.compile("\\{[^{}]*\\}");
        Matcher objectMatcher = objectPattern.matcher(jsonContent);

        while (objectMatcher.find()) {
            String obj = objectMatcher.group();
            String id = extractJsonField(obj, "messageId");
            String sender = extractJsonField(obj, "sender");
            String recipient = extractJsonField(obj, "recipient");
            String message = extractJsonField(obj, "message");
            if (message.isEmpty()) {
                message = extractJsonField(obj, "content");
            }
            String hash = extractJsonField(obj, "messageHash");

            if (!message.isEmpty()) {
                Message loaded = new Message(
                        id.isEmpty() ? null : id,
                        sender.isEmpty() ? "StoredUser" : sender,
                        recipient,
                        message,
                        "Stored",
                        hash.isEmpty() ? null : hash
                );
                storedMessages = appendMessage(storedMessages, loaded);
                addMessageId(loaded.getMessageId());
                addMessageHash(loaded.getMessageHash());
                loadedCount++;
            }
        }
        return loadedCount;
    }

    private String extractJsonField(String jsonObject, String key) {
        Pattern fieldPattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = fieldPattern.matcher(jsonObject);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return "";
    }

    private String unescapeJson(String value) {
        return value.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
    }

    // 2a. Display sender and recipient of all stored messages
    public String displayStoredSendersAndRecipients() {
        if (storedMessages.length == 0) {
            return "No stored messages available.";
        }
        StringBuilder sb = new StringBuilder("=== Stored Messages: Senders & Recipients ===\n");
        for (int i = 0; i < storedMessages.length; i++) {
            Message m = storedMessages[i];
            sb.append(i + 1)
              .append(". Sender: ").append(m.getSender())
              .append(" | Recipient: ").append(m.getRecipient())
              .append("\n");
        }
        return sb.toString().trim();
    }

    // 2b. Display the longest stored message (also usable across all messages)
    public String displayLongestMessage() {
        Message longest = null;
        for (Message m : storedMessages) {
            if (longest == null || m.getContent().length() > longest.getContent().length()) {
                longest = m;
            }
        }
        for (Message m : sentMessages) {
            if (longest == null || m.getContent().length() > longest.getContent().length()) {
                longest = m;
            }
        }
        return (longest == null) ? "No messages found." : longest.getContent();
    }

    // 2c. Search for a message ID and display the corresponding recipient and message
    public String searchByMessageId(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            return "Message ID not found.";
        }
        String query = targetId.trim();
        Message[] pools = combinePools(storedMessages, sentMessages, disregardedMessages);
        for (Message m : pools) {
            if (m.getMessageId().equalsIgnoreCase(query) || m.getRecipient().equalsIgnoreCase(query)) {
                return "Recipient: " + m.getRecipient() + " | Message: \"" + m.getContent() + "\"";
            }
        }
        return "No message found for ID: " + query;
    }

    public String searchMessagesByRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            return "No messages found for recipient.";
        }
        String target = recipient.trim();
        StringBuilder matches = new StringBuilder();

        for (Message m : sentMessages) {
            if (m.getRecipient().equalsIgnoreCase(target)) {
                if (matches.length() > 0) matches.append("\n");
                matches.append("\"").append(m.getContent()).append("\"");
            }
        }
        for (Message m : storedMessages) {
            if (m.getRecipient().equalsIgnoreCase(target)) {
                if (matches.length() > 0) matches.append("\n");
                matches.append("\"").append(m.getContent()).append("\"");
            }
        }

        return matches.length() > 0
                ? matches.toString()
                : "No sent or stored messages found for recipient: " + target;
    }

    public String deleteMessageByHash(String targetHash) {
        if (targetHash == null || targetHash.isBlank()) {
            return "Invalid message hash.";
        }
        String hashQuery = targetHash.trim();

        DeleteResult storedRes = removeFromMessageArray(storedMessages, hashQuery);
        if (storedRes.removed != null) {
            storedMessages = storedRes.remaining;
            removeHash(hashQuery);
            return "Message \"" + storedRes.removed.getContent() + "\" successfully deleted.";
        }

        DeleteResult sentRes = removeFromMessageArray(sentMessages, hashQuery);
        if (sentRes.removed != null) {
            sentMessages = sentRes.remaining;
            removeHash(hashQuery);
            return "Message \"" + sentRes.removed.getContent() + "\" successfully deleted.";
        }

        DeleteResult disregardedRes = removeFromMessageArray(disregardedMessages, hashQuery);
        if (disregardedRes.removed != null) {
            disregardedMessages = disregardedRes.remaining;
            removeHash(hashQuery);
            return "Message \"" + disregardedRes.removed.getContent() + "\" successfully deleted.";
        }

        return "Message with hash \"" + hashQuery + "\" not found.";
    }

    public String displayMessageReport() {
        Message[] reportItems = (storedMessages.length > 0) ? storedMessages : sentMessages;
        if (reportItems.length == 0 && sentMessages.length == 0) {
            return "No messages available for report.";
        }

        StringBuilder sb = new StringBuilder("=== MESSAGE REPORT ===\n");
        for (Message m : sentMessages) {
            sb.append("Message Hash: ").append(m.getMessageHash())
              .append(" | Recipient: ").append(m.getRecipient())
              .append(" | Message: \"").append(m.getContent()).append("\"\n");
        }
        for (Message m : storedMessages) {
            sb.append("Message Hash: ").append(m.getMessageHash())
              .append(" | Recipient: ").append(m.getRecipient())
              .append(" | Message: \"").append(m.getContent()).append("\"\n");
        }
        return sb.toString().trim();
    }

    public Message[] getSentMessages() {
        return sentMessages.clone();
    }

    public Message[] getDisregardedMessages() {
        return disregardedMessages.clone();
    }

    public Message[] getStoredMessages() {
        return storedMessages.clone();
    }

    public String[] getMessageHashes() {
        return messageHashes.clone();
    }

    public String[] getMessageIds() {
        return messageIds.clone();
    }

    private void addMessageId(String id) {
        if (id == null || id.isBlank()) return;
        for (String existing : messageIds) {
            if (existing.equals(id)) return;
        }
        String[] next = new String[messageIds.length + 1];
        System.arraycopy(messageIds, 0, next, 0, messageIds.length);
        next[messageIds.length] = id;
        messageIds = next;
    }

    private void addMessageHash(String hash) {
        if (hash == null || hash.isBlank()) return;
        for (String existing : messageHashes) {
            if (existing.equals(hash)) return;
        }
        String[] next = new String[messageHashes.length + 1];
        System.arraycopy(messageHashes, 0, next, 0, messageHashes.length);
        next[messageHashes.length] = hash;
        messageHashes = next;
    }

    private void removeHash(String hash) {
        int count = 0;
        for (String h : messageHashes) {
            if (!h.equalsIgnoreCase(hash)) count++;
        }
        String[] next = new String[count];
        int idx = 0;
        for (String h : messageHashes) {
            if (!h.equalsIgnoreCase(hash)) {
                next[idx++] = h;
            }
        }
        messageHashes = next;
    }

    private Message[] appendMessage(Message[] source, Message item) {
        Message[] next = new Message[source.length + 1];
        System.arraycopy(source, 0, next, 0, source.length);
        next[source.length] = item;
        return next;
    }

    private Message[] combinePools(Message[] a, Message[] b, Message[] c) {
        Message[] combined = new Message[a.length + b.length + c.length];
        int pos = 0;
        for (Message m : a) combined[pos++] = m;
        for (Message m : b) combined[pos++] = m;
        for (Message m : c) combined[pos++] = m;
        return combined;
    }

    private DeleteResult removeFromMessageArray(Message[] source, String targetHash) {
        Message removed = null;
        int keepCount = 0;
        for (Message m : source) {
            if (removed == null && m.getMessageHash().equalsIgnoreCase(targetHash)) {
                removed = m;
            } else {
                keepCount++;
            }
        }
        if (removed == null) {
            return new DeleteResult(null, source);
        }
        Message[] remaining = new Message[keepCount];
        boolean skipped = false;
        int idx = 0;
        for (Message m : source) {
            if (!skipped && m.getMessageHash().equalsIgnoreCase(targetHash)) {
                skipped = true;
                continue;
            }
            remaining[idx++] = m;
        }
        return new DeleteResult(removed, remaining);
    }

    private record DeleteResult(Message removed, Message[] remaining) {}
}
