package sample;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class SocketManager {
    static private Socket mySocket;
    static private PrintWriter outputStream;
    static private InputStream inputStream;
    static private boolean isClientConnectedToServer = false;

    static public void initializeSocketAndConnect(String ip, int port) throws IOException {
        mySocket = new Socket();
        SocketAddress serverSocketAddress = new InetSocketAddress(ip, port);
        mySocket.connect(serverSocketAddress, 500);
        outputStream = new PrintWriter(mySocket.getOutputStream(), true);
        inputStream = mySocket.getInputStream();
    }

    static public void sendMessage(String message, String prefix) {
        if (prefix.charAt(0) == 'm') {
            message = prefix + message;
        }
        else {
            int messageLength = message.getBytes().length + prefix.length() + 4;
            message = prefix + " " + messageLength + " " + message;
        }
        System.out.println("wyslana wiadomosc: " + message);
        outputStream.println(message);
    }

    static public ArrayList<String> receiveMessage() throws IOException {
        byte[] buffer = new byte[10000];
        int expectedLength;
        int partialLength = inputStream.read(buffer);
        int messageLength = partialLength;
        ArrayList<String> receivedMessages = new ArrayList<>(0);
        String receivedMessage = new String(buffer, 0, partialLength);
        String temp;
        String[] words = receivedMessage.split(" ");
        String[] messages;
        String lastMessage;
        expectedLength = Integer.parseInt(words[1]) + words[1].length();
        System.out.println(expectedLength + " " + messageLength);
        while (expectedLength != messageLength) {
            if (expectedLength > messageLength) {  // przychodzi fragment wiadomosci
                Arrays.fill(buffer, (byte) 0);
                partialLength = inputStream.read(buffer);
                temp = new String(buffer, 0, partialLength);
                receivedMessage += temp;
                messageLength += partialLength;
                if (expectedLength == messageLength) {
                    receivedMessages.add(receivedMessage);
                }
            }
            if (expectedLength < messageLength) { // przychodza sklejone wiadomosci
                messages = receivedMessage.split("\n");
                for (int i = 0; i < messages.length - 1; i++) {
                    receivedMessages.add(messages[i].substring(0, messages[i].length() - 2));
                }
                lastMessage = messages[messages.length - 1];
                expectedLength = Integer.parseInt(lastMessage.split(" ")[1]) - lastMessage.split(" ")[1].length();
                if (lastMessage.length() == expectedLength) {
                    receivedMessages.add(lastMessage.substring(0, lastMessage.length() - 2));
                }
                else {
                    receivedMessage = lastMessage;
                    messageLength = lastMessage.length();
                }
            }
        }
        if (receivedMessages.size() == 0) receivedMessages.add(receivedMessage);
        return receivedMessages;
    }

    static public void closeSocket() throws IOException {
        mySocket.close();
    }

    static public boolean checkIfClientIsConnectedToServer() {
        return isClientConnectedToServer;
    }

    static public void setIsClientConnectedToServer() {
        isClientConnectedToServer = true;
    }
}
