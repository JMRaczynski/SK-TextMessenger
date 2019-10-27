package sample;

import java.io.*;
import java.net.*;

public class SocketManager {
    static private Socket mySocket;
    static private PrintWriter outputStream;
    static private InputStream inputStream;

    static public void initializeSocketAndConnect(String ip, int port) throws IOException {
        mySocket = new Socket(ip, port);
        outputStream = new PrintWriter(mySocket.getOutputStream(), true);
        inputStream = mySocket.getInputStream();
    }

    static public void sendMessage(String message) {
        outputStream.println(message);
    }

    static public String receiveMessage() throws IOException {
        byte[] buffer = new byte[100];
        int length = inputStream.read(buffer);
        String receivedMessage = new String(buffer, 0, length - 2);
        return receivedMessage;
    }

    static public void closeSocket() throws IOException {
        mySocket.close();
    }
}
