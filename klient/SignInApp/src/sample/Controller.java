package sample;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
    @FXML private Label nickLabel;
    @FXML private Label passwordLabel;
    @FXML private Button loginButton;
    @FXML private TextField nickTextField;
    @FXML private TextField passwordTextField;


    public void sendCredentialsAndShowFeedback(ActionEvent event) throws IOException
    {
        String login = nickTextField.getText();
        String password = passwordTextField.getText();
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

    }

    /*public void initialize(URL url, ResourceBundle rb) {

    }*/

}

