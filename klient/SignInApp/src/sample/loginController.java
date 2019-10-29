package sample;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    }

    public void sendCredentialsAndShowFeedback(ActionEvent event) throws IOException
    {

        String login = upperTextField.getText();
        String password = lowerTextField.getText();
        String serverAnswer;
        SocketManager.sendMessage(login + ' ' + password + '\n', "l");
        serverAnswer = SocketManager.receiveMessage();
        System.out.println(serverAnswer);
        System.out.println(serverAnswer.length());

        if (serverAnswer.equals("Witamy")) {
            System.out.println(serverAnswer);
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("mainView.fxml"));
            Scene mainScene = new Scene(tableViewParent);

            //This line gets the Stage information
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(mainScene);

            window.show();
        }
        else
        {
            warningLabel.setVisible(true);
        }

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

    /*public void initialize(URL url, ResourceBundle rb) {

    }*/

}

