package sample;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.concurrent.Task;

public class LoginController /*implements Initializable*/ {
    @FXML private Label ipPortIntroductionLabel;
    @FXML private Label loginIntroductionLabel;
    @FXML private Label upperLabel;
    @FXML private Label lowerLabel;
    @FXML private Label warningLabel;
    @FXML private Button loginButton;
    @FXML private Button goNextButton;
    @FXML private TextField upperTextField;
    @FXML private TextField lowerTextField;
    public MainViewController mainViewController;
    public Scene loginScene;
    //public Scene mainViewScene;


    public void connectToServer(ActionEvent event) throws IOException
    {
        String ipAddress = upperTextField.getText();
        try {
            Integer portNumber = Integer.parseInt(lowerTextField.getText());
            SocketManager.initializeSocketAndConnect(ipAddress, portNumber);
        }
        catch (Exception e) {
            warningLabel.setVisible(true);
            upperTextField.requestFocus();
            return;
        }
        changeIpInputWindowToLoginWindow();
        SocketManager.setIsClientConnectedToServer();

        Task<Integer> readMessagesFromServer = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                String received;
                while (true) {
                    received = SocketManager.receiveMessage();
                    System.out.println("Wiadomosc: " + received);
                    switch(received.charAt(0)) {
                        case 'P': // blad logowania
                            warningLabel.setVisible(true);
                            break;
                        case 'W': // witamy - poprawne zalogowanie
                            System.out.println("Jestem " + upperTextField.getText());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        switchSceneToMainView();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case 'l': // przeslanie listy uzytkownikow po poprawnym zalogowaniu
                            String[] users = received.split(" ");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mainViewController.showActiveUsers(users);
                                        mainViewController.initializeUserMap();
                                        mainViewController.initializeUnreadAuthorsList();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case 'i':
                            String[] splitMessage = received.split(" ");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    mainViewController.addUserToList(splitMessage[1]);
                                }
                            });
                            break;
                        case 'o':
                            String[] splitReceived = received.split(" ");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    mainViewController.removeUserFromList(splitReceived[1]);
                                }
                            });
                            break;
                        case 'm':
                            String[] senderAndMessage = received.split(" ");
                            String properMessage = "";
                            System.out.println("Od: " + senderAndMessage[1]);
                            for (int i = 2; i < senderAndMessage.length; i++) {
                                properMessage = properMessage + senderAndMessage[i];
                                if (i != senderAndMessage.length - 1) {
                                    properMessage += " ";
                                }
                            }
                            System.out.println("Treść: " + properMessage);
                            boolean wasAuthorAdded;
                            wasAuthorAdded = mainViewController.addAuthorToListOfUnreadAuthorsIfNeeded(senderAndMessage[1]);
                            if (wasAuthorAdded) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainViewController.updateNewMessagesLabel();
                                    }
                                });
                            }
                            break;
                    }
                }
            }
        };

        Thread th = new Thread(readMessagesFromServer);
        th.setDaemon(true);
        th.start();

    }

    public void sendCredentialsAndShowFeedback(ActionEvent event) throws IOException
    {

        String login = upperTextField.getText();
        String password = lowerTextField.getText();
        String serverAnswer;
        SocketManager.sendMessage(login + ' ' + password + '\n', "l");
        //serverAnswer = SocketManager.receiveMessage();
        //System.out.println(serverAnswer);
        //System.out.println(serverAnswer.length());

        //if (serverAnswer.equals("Witamy")) {
            //System.out.println(serverAnswer);
        //}
        //else
        //{
            //warningLabel.setVisible(true);
        //}

    }

    private void changeIpInputWindowToLoginWindow() {
        ipPortIntroductionLabel.setVisible(false);
        loginIntroductionLabel.setVisible(true);
        upperLabel.setText("Nick");
        upperTextField.clear();
        upperTextField.requestFocus();
        lowerLabel.setText("Hasło");
        lowerTextField.clear();
        loginButton.setVisible(true);
        goNextButton.setDisable(true);
        goNextButton.setVisible(false);
        warningLabel.setVisible(false);

        //DOUSUNIECIA
        upperTextField.setText("adolf");
        lowerTextField.setText("hitler");
    }

    private void switchSceneToMainView() throws IOException {
        //This line gets the Stage information
        Stage window = (Stage)(loginButton).getScene().getWindow();
        window.setTitle("TalkieApp - " + upperTextField.getText());
        try {
            window.setScene(mainViewController.mainViewScene);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("dd");
        //window.show();
        //return controller;
    }

    /*public void initialize(URL url, ResourceBundle rb) {

    }*/

}

