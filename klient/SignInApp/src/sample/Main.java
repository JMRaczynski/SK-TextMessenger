package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;


import javax.naming.ldap.Control;
import java.io.IOException;
import java.util.Scanner;
import java.io.*;
import java.net.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        //SocketManager.initializeSocketAndConnect("192.168.0.125", 1235);

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TalkieApp");
        primaryStage.show();

        EventHandler quitHandler = new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                if (SocketManager.checkIfClientIsConnectedToServer()) SocketManager.sendMessage("quit");
            }
        };

        primaryStage.setOnCloseRequest(quitHandler);

    }

    public static void main(String[] args) {
        launch(args);
    }
}