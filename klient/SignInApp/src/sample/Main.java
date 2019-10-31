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

    private loginController loginController;
    private mainViewController mainViewController;
    @Override
    public void start(Stage primaryStage) throws Exception{
        //inicjalizacja i zapis sceny logowania
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loginLoader.load();
        loginController loginController = loginLoader.getController();
        Scene logScene = new Scene(root);

        //inicjalizacja i zapis sceny mainview
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("mainView.fxml"));
        Parent mainViewNode = mainLoader.load();
        mainViewController mainController = mainLoader.getController();
        Scene mainScene = new Scene(mainViewNode);
        System.out.println(mainController.logoutButton.getText());

        mainController.mainViewScene = mainScene;
        mainController.loginScene = logScene;
        loginController.mainViewScene = mainScene;
        loginController.loginScene = logScene;
        loginController.mainViewController = mainController;
        mainController.loginController = loginController;

        primaryStage.setScene(logScene);
        primaryStage.setTitle("TalkieApp");
        primaryStage.show();

        EventHandler quitHandler = new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                if (SocketManager.checkIfClientIsConnectedToServer()) SocketManager.sendMessage("quit", "q");
            }
        };

        primaryStage.setOnCloseRequest(quitHandler);

    }

    public static void main(String[] args) {
        launch(args);
    }
}