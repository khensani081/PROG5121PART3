/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package prog5121part3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class PROG5121PART3 {

public class Main {
    private static final String DEFAULT_JSON_FILE = "stored_messages.json";

    public static void main(String[] args) {
       try (Scanner scan = new Scanner(System.in)) {
            MessageReportManager reportManager = new MessageReportManager();
            Path jsonPath = Path.of(DEFAULT_JSON_FILE);
            ensureSampleJsonExists(jsonPath);

            try {
                int loadedCount = reportManager.loadStoredMessagesFromJson(jsonPath);
                System.out.println("Loaded " + loadedCount + " stored message(s) from " + jsonPath.getFileName());
            } catch (IOException e) {
                System.out.println("Could not read " + jsonPath.getFileName() + ": " + e.getMessage());
            }

            boolean running = true;
            while (running) {
                printMainMenu();
                String choice = scan.nextLine().trim();

                switch (choice) {
                    case "1" -> sendNewMessageFlow(scan, reportManager);
                    case "2" -> showRecentlySentMessages(reportManager);
                    case "3" -> showDisregardedMessages(reportManager);
                    case "4" -> handleStoredMessagesMenu(scan, reportManager, jsonPath);
                    case "5" -> {
                        running = false;
                        System.out.println("Exiting QuickChat. Goodbye!");
                    }
                    default -> System.out.println("Invalid option. Please choose 1 to 5.");
                }
            }
        }
    } 
    private static void printMainMenu() {
        System.out.println("\n=== QUICKCHAT MAIN MENU ===");
        System.out.println("1. Send / Create Message");
        System.out.println("2. View Sent Messages");
        System.out.println("3. View Disregarded Messages");
        System.out.println("4. Stored Messages");
        System.out.println("5. Quit");
        System.out.print("Enter choice: ");
    }

    private static void sendNewMessageFlow(Scanner scanner, MessageReportManager reportManager) {
        System.out.print("Enter sender name: ");
        String sender = scanner.nextLine().trim();
        if (sender.isEmpty()) sender = "User";

        System.out.print("Enter recipient (e.g. +27834557896): ");
        String recipient = scanner.nextLine().trim();

        System.out.print("Enter message: ");
        String content = scanner.nextLine();

        System.out.print("Enter flag (Sent / Stored / Disregard): ");
        String flagInput = scanner.nextLine().trim();
        String flag = flagInput.isEmpty() ? "Sent" : flagInput;

        Message message = new Message(sender, recipient, content, flag);
        reportManager.recordMessage(message);

        System.out.println("Recorded [" + message.getFlag() + "] | ID: " + message.getMessageId()
                + " | Hash: " + message.getMessageHash());
    }

    private static void showRecentlySentMessages(MessageReportManager reportManager) {
        Message[] sent = reportManager.getSentMessages();
        if (sent.length == 0) {
            System.out.println("No sent messages recorded yet.");
            return;
        }
        System.out.println("\n--- Sent Messages ---");
        for (Message m : sent) {
            System.out.println("To: " + m.getRecipient() + " | Hash: " + m.getMessageHash()
                    + " | Msg: \"" + m.getContent() + "\"");
        }
    }

    private static void showDisregardedMessages(MessageReportManager reportManager) {
        Message[] disregarded = reportManager.getDisregardedMessages();
        if (disregarded.length == 0) {
            System.out.println("No disregarded messages recorded yet.");
            return;
        }
        System.out.println("\n--- Disregarded Messages ---");
        for (Message m : disregarded) {
            System.out.println("To: " + m.getRecipient() + " | Msg: \"" + m.getContent() + "\"");
        }
    }

    private static void handleStoredMessagesMenu(Scanner scanner, MessageReportManager reportManager, Path jsonPath) {
        boolean inSubMenu = true;
        while (inSubMenu) {
            System.out.println("\n=== STORED MESSAGES MENU ===");
            System.out.println("a. Display sender and recipient of all stored messages");
            System.out.println("b. Display the longest stored message");
            System.out.println("c. Search for a Message ID and display recipient and message");
            System.out.println("d. Search for all messages stored/sent for a particular recipient");
            System.out.println("e. Delete a message using the message hash");
            System.out.println("f. Display report listing full details of messages");
            System.out.println("g. Reload JSON file");
            System.out.println("x. Back to Main Menu");
            System.out.print("Select an option: ");

            String option = scanner.nextLine().trim().toLowerCase();
            switch (option) {
                case "a" -> System.out.println(reportManager.displayStoredSendersAndRecipients());
                case "b" -> System.out.println("Longest Message: " + reportManager.displayLongestMessage());
                case "c" -> {
                    System.out.print("Enter Message ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.println(reportManager.searchByMessageId(id));
                }
                case "d" -> {
                    System.out.print("Enter Recipient: ");
                    String recipient = scanner.nextLine().trim();
                    System.out.println(reportManager.searchMessagesByRecipient(recipient));
                }
                case "e" -> {
                    System.out.print("Enter Message Hash to delete: ");
                    String hash = scanner.nextLine().trim();
                    System.out.println(reportManager.deleteMessageByHash(hash));
                }
                case "f" -> System.out.println(reportManager.displayMessageReport());
                case "g" -> {
                    try {
                        int count = reportManager.loadStoredMessagesFromJson(jsonPath);
                        System.out.println("Reloaded " + count + " message(s) from " + jsonPath.getFileName() + ".");
                    } catch (IOException e) {
                        System.out.println("Error reloading JSON: " + e.getMessage());
                    }
                }
                case "x" -> inSubMenu = false;
                default -> System.out.println("Invalid option. Choose a-g or x.");
            }
        }
    }

    private static void ensureSampleJsonExists(Path jsonPath) {
        if (Files.exists(jsonPath)) {
            return;
        }
        String starterJson = """
                [
                  {
                    "messageId": "1000000002",
                    "sender": "Developer",
                    "recipient": "+27838884567",
                    "message": "Where are you? You are late! I have asked you to be on time.",
                    "messageHash": "10:WHERETIME"
                  },
                  {
                    "messageId": "1000000005",
                    "sender": "Developer",
                    "recipient": "+27838884567",
                    "message": "Ok, I am leaving without you.",
                    "messageHash": "10:OKYOU"
                  }
                ]
                """;
        try {
            Files.writeString(jsonPath, starterJson, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
    
        }
    }
}

    }
    
