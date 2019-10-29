package sample;

import java.io.IOException;
import java.net.InetAddress;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.stage.Stage;
import javafx.concurrent.Task;

public class loginController /*implements Initializable*/ {
    @FXML private Label ipPortIntroductionLabel;
    @FXML private Label loginIntroductionLabel;
    @FXML private Label upperLabel;
    @FXML private Label lowerLabel;
    @FXML private Label warningLabel;
    @FXML private Button loginButton;
    @FXML private Button goNextButton;
    @FXML private TextField upperTextField;
    @FXML private TextField lowerTextField;
    private mainViewController mainViewController;



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
                        case 'P':
                            warningLabel.setVisible(true);
                            break;
                        case 'W':
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mainViewController = switchSceneToMainView();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case 'l':
                            String[] users = received.split(" ");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mainViewController.showActiveUsers(users);
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

    private mainViewController switchSceneToMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainView.fxml"));
        Parent tableViewParent = loader.load();
        Scene mainScene = new Scene(tableViewParent);
        mainViewController controller = loader.getController();

        //This line gets the Stage information
        Stage window = (Stage)(loginButton).getScene().getWindow();

        try {
            window.setScene(mainScene);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("dd");

        window.show();
        return controller;
    }

    /*public void initialize(URL url, ResourceBundle rb) {

    }*/

}

