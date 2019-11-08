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
        //int messageLength = message.length() + prefix.length() + 1;
        //message = prefix + messageLength + " " + message;
        message = prefix + message;
        outputStream.println(message);
    }

    static public String receiveMessage() throws IOException {
        byte[] buffer = new byte[10000];
        int expectedLength;
        int partialLength = inputStream.read(buffer);
        int messageLength = partialLength;
        String receivedMessage = new String(buffer, 0, partialLength);
        String temp;
        String[] words = receivedMessage.split(" ");
        expectedLength = Integer.parseInt(words[1]) - words[1].length();
        while (expectedLength > messageLength) {
            Arrays.fill(buffer, (byte) 0);
            partialLength = inputStream.read(buffer);
            temp = new String(buffer, 0, partialLength);
            receivedMessage += temp;
            messageLength += partialLength;
        }
        return receivedMessage;
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
