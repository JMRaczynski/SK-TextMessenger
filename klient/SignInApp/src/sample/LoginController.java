package sample;

import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.concurrent.Task;

public class LoginController {
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
    public ChatViewController chatViewController;
    public Scene loginScene;
    public String userNick;


    public void connectToServer(ActionEvent event)
    {
        // pobranie z pól tekstowych adresu ip i portu serwera i próba połączenia z serwerem
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
        // zmiana sceny
        changeIpInputWindowToLoginWindow();
        SocketManager.setIsClientConnectedToServer();

        // kod wątku nasłuchującego wiadomości od serwera
        Task<Integer> readMessagesFromServer = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                ArrayList<String> incomingMessages;
                String received;
                while (true) {
                    incomingMessages = SocketManager.receiveMessage();
                    while (incomingMessages.size() > 0) {
                        System.out.println(incomingMessages.get(0));
                        received = incomingMessages.get(0);
                        incomingMessages.remove(0);
                        System.out.println("Wiadomosc: " + received);
                        switch(received.charAt(0)) {
                            case 'P': // wiadomość o wpisaniu blędnego hasła
                                Platform.runLater(new Runnable() { // runnable są potrzebne, gdyż to wątek główny musi manipulować elementami na scenie (wymaganie JavaFX)
                                    @Override
                                    public void run() {
                                        warningLabel.setText("Dane nieprawidłowe! Spróbuj ponownie");
                                        warningLabel.setVisible(true);
                                    }
                                });
                                break;
                            case 'a': // wiadomość o próbie zalogowania na konto już zalogowanego użytkownika
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        warningLabel.setText("Użytkownik jest już zalogowany. Spróbuj ponownie");
                                        warningLabel.setVisible(true);
                                    }
                                });
                                break;
                            case 'W': // witamy - wiadomość o poprawnym zalogowaniu
                                System.out.println("Jestem " + upperTextField.getText());
                                userNick = upperTextField.getText();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        switchSceneToMainView();
                                    }
                                });
                                break;
                            case 'l': // przeslanie listy uzytkownikow po poprawnym zalogowaniu
                                String[] users = received.split(" ");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainViewController.showActiveUsers(users);
                                        mainViewController.initializeUserMap();
                                        mainViewController.initializeUnreadAuthorsList();
                                        mainViewController.updateNewMessagesLabel();
                                    }
                                });
                                break;
                            case 'i': // powiadomienie o nowym uzytkowniku offline
                                String[] splitMessage = received.split(" ");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainViewController.addUserToListOfActiveUsers(splitMessage[2]);

                                    }
                                });
                                if (splitMessage[2].equals(mainViewController.messageRecipient)) {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatViewController.switchLoggedOutWarningForTextArea();
                                        }
                                    });
                                }
                                break;
                            case 'o': // powiadomienie, ze uzytkownik opuscil system
                                String[] splitReceived = received.split(" ");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainViewController.removeUserFromListOfActiveUsers(splitReceived[2]);
                                    }
                                });
                                if (splitReceived[2].equals(mainViewController.messageRecipient)) {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatViewController.switchTextAreaForLoggedOutWarning();
                                        }
                                    });
                                }
                                break;
                            case 'm': // nowa wiadomosc
                                String[] senderAndMessage = received.split(" ");
                                String properMessage = "";
                                System.out.println("Od: " + senderAndMessage[2]);
                                for (int i = 3; i < senderAndMessage.length; i++) {
                                    properMessage = properMessage + senderAndMessage[i];
                                    if (i != senderAndMessage.length - 1) {
                                        properMessage += " ";
                                    }
                                }
                                System.out.println("Treść: " + properMessage);
                                String finalProperMessage = properMessage;
                                Platform.runLater(new Runnable() {
                                    public void run(){
                                        boolean wasAuthorAdded;
                                        chatViewController.showIncomingMessage(finalProperMessage, senderAndMessage[2]);
                                        wasAuthorAdded = mainViewController.addAuthorToListOfUnreadAuthorsIfNeeded(senderAndMessage[2]);
                                        if (wasAuthorAdded) {
                                            mainViewController.updateNewMessagesLabel();
                                        }
                                    }
                                });
                                break;
                        }
                    }
                }
            }
        };

        // utworzenie wątku i uruchomienie go
        Thread th = new Thread(readMessagesFromServer);
        th.setDaemon(true);
        th.start();

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
    }

    public void sendCredentialsToServer(ActionEvent event) {
        String login = upperTextField.getText();
        String password = lowerTextField.getText();
        if (!login.equals("") && !password.equals("")) {
            SocketManager.sendMessage(login + ' ' + password + '\n', "l");
        }
        else {
            warningLabel.setVisible(true);
        }
    }

    private void switchSceneToMainView() {
        //This line gets the Stage information
        warningLabel.setVisible(false);
        Stage window = (Stage)(loginButton).getScene().getWindow();
        window.setTitle("TalkieApp - " + upperTextField.getText());
        try {
            window.setScene(mainViewController.mainViewScene);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

