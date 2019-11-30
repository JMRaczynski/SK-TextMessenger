package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //inicjalizacja sceny logowania
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loginLoader.load();
        LoginController loginController = loginLoader.getController();
        Scene logScene = new Scene(root);

        //inicjalizacja sceny mainview
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("mainView.fxml"));
        Parent mainViewNode = mainLoader.load();
        MainViewController mainController = mainLoader.getController();
        Scene mainScene = new Scene(mainViewNode);

        //okno czatu
        FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("chatView.fxml"));
        Parent chatViewNode = chatLoader.load();
        ChatViewController chatController = chatLoader.getController();
        Scene chScene = new Scene(chatViewNode);


        mainController.mainViewScene = mainScene;
        mainController.loginController = loginController;
        mainController.chatViewController = chatController;

        loginController.loginScene = logScene;
        loginController.mainViewController = mainController;
        loginController.chatViewController = chatController;

        chatController.chatViewScene = chScene;
        chatController.mainViewController = mainController;
        chatController.loginController = loginController;

        chatController.chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        primaryStage.setScene(logScene);
        primaryStage.setTitle("TalkieApp");
        primaryStage.show();

        EventHandler quitHandler = new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                if (SocketManager.checkIfClientIsConnectedToServer()) {
                    SocketManager.sendMessage("quit", "q");
                    try {
                        SocketManager.closeSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Problem z zamknięciem gniazda");
                    }
                }
            }
        };

        primaryStage.setOnCloseRequest(quitHandler);

    }

    public static void main(String[] args) {
        launch(args);
    }
}