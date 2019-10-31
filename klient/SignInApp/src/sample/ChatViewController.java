package sample;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;


public class ChatViewController {

    @FXML private Button sendButton;
    @FXML private Button goBackButton;
    @FXML private TextField writeTextField;
    @FXML private TextFlow textFlow;

    public MainViewController mainViewController;
    public Scene chatViewScene;

    public void sendButtonHandler(ActionEvent event){
        String message = writeTextField.getText();
        //Text t = new Text(message);
        if (!message.equals(null)) {
            textFlow.getChildren().addAll(new Text(message + "\n"));
        }
        writeTextField.clear();
    }

    public void sendOnEnter(KeyEvent keyEvent){
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String message = writeTextField.getText();
            if (!message.equals(null)) {
                textFlow.getChildren().addAll(new Text(message + "\n"));
            }
            writeTextField.clear();
            writeTextField.requestFocus();
        }
    }

    public void goBackButtonHandler() throws IOException {
        Stage window = (Stage)(goBackButton).getScene().getWindow();
        try {
            window.setScene(mainViewController.mainViewScene);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("gb");
    }

}
