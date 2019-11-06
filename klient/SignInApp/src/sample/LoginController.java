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
    public ChatViewController chatViewController;
    public Scene loginScene;
    public String userNick;
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
                        case 'P': // blędne hasło
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    warningLabel.setText("Dane nieprawidłowe! Spróbuj ponownie");
                                    warningLabel.setVisible(true);
                                }
                            });
                            break;
                        case 'a': // logowanie na już zalogowanego użytkownika
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    warningLabel.setText("Użytkownik jest już zalogowany. Spróbuj ponownie");
                                    warningLabel.setVisible(true);
                                }
                            });
                            break;
                        case 'W': // witamy - poprawne zalogowanie
                            System.out.println("Jestem " + upperTextField.getText());
                            userNick = upperTextField.getText();
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
                                        mainViewController.updateNewMessagesLabel();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case 'i': // powiadomienie o nowym uzytkowniku offline
                            String[] splitMessage = received.split(" ");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    mainViewController.addUserToListOfActiveUsers(splitMessage[1]);

                                }
                            });
                            if (splitMessage[1].equals(mainViewController.messageRecipient)) {
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
                                    mainViewController.removeUserFromListOfActiveUsers(splitReceived[1]);
                                }
                            });
                            if (splitReceived[1].equals(mainViewController.messageRecipient)) {
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
                            System.out.println("Od: " + senderAndMessage[1]);
                            for (int i = 2; i < senderAndMessage.length; i++) {
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
                                    chatViewController.showIncomingMessage(finalProperMessage, senderAndMessage[1]);
                                    wasAuthorAdded = mainViewController.addAuthorToListOfUnreadAuthorsIfNeeded(senderAndMessage[1]);
                                    if (wasAuthorAdded) {
                                        mainViewController.updateNewMessagesLabel();
                                    }
                                }
                            });
                            break;
                    }
                }
            }
        };

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

        //DOUSUNIECIA
        upperTextField.setText("adolf");
        lowerTextField.setText("hitler");
    }

    public void sendCredentialsToServer(ActionEvent event) {
        String login = upperTextField.getText();
        String password = lowerTextField.getText();
        SocketManager.sendMessage(login + ' ' + password + '\n', "l");
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
    }

    /*public void initialize(URL url, ResourceBundle rb) {

    }*/

}

