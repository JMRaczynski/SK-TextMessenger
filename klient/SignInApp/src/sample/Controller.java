package sample;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Controller /*implements Initializable*/ {
    @FXML private Label introductionLabel;
    @FXML private Label introductionLabel1;
    @FXML private Label upperLabel;
    @FXML private Label lowerLabel;
    @FXML private Label warningLabel;
    @FXML private Button loginButton;
    @FXML private Button goNextButton;
    @FXML private TextField upperTextField;
    @FXML private TextField lowerTextField;



    public void connectToServer(ActionEvent event) throws IOException
    {
        String ipAddress = upperTextField.getText();
        try {
            Integer portNumber = Integer.parseInt(lowerTextField.getText());
            SocketManager.initializeSocketAndConnect(ipAddress, portNumber);
        }
        catch (Exception e) {
            introductionLabel.setText("Ponów próbę");
            warningLabel.setVisible(true);
            //upperTextField.clear();
            //lowerTextField.clear();
            return;
        }
        changeIpInputWindowToLoginWindow();
        SocketManager.setIsClientConnectedToServer();
        /*
        TODO put ip adress and port number in right place :)

        ?? idk o co chodzi
         */

    }

    public void sendCredentialsAndShowFeedback(ActionEvent event) throws IOException
    {

        String login = upperTextField.getText();
        String password = lowerTextField.getText();
        String serverAnswer;
        SocketManager.sendMessage(login + ' ' + password + '\n');
        serverAnswer = SocketManager.receiveMessage();
        System.out.println(serverAnswer);
        System.out.println(serverAnswer.length());

        if (serverAnswer.equals("Witamy")) {
            System.out.println(serverAnswer);
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("logout.fxml"));
            Scene logoutScene = new Scene(tableViewParent);

            //This line gets the Stage information
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(logoutScene);
            window.show();
        }
        else
        {
            warningLabel.setVisible(true);
        }

    }

    private void changeIpInputWindowToLoginWindow() {
        introductionLabel.setVisible(false);
        introductionLabel1.setVisible(true);
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

    /*public void initialize(URL url, ResourceBundle rb) {

    }*/

}

