package sample;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;


public class ChatViewController {

    //@FXML private Button sendButton;
    @FXML private Button goBackButton;
    @FXML private TextArea writeTextArea;
    @FXML public AnchorPane chatBackgroundPane;
    @FXML public ScrollPane chatScrollPane;

    public MainViewController mainViewController;
    public Scene chatViewScene;

    public void sendButtonHandler(ActionEvent event){
        String message = writeTextArea.getText();
        //Text t = new Text(message);
        if (!message.equals(null)) {
            mainViewController.userChatViews.get(mainViewController.messageRecipient).getChildren().addAll(new Text(message + "\n"));
        }
        writeTextArea.clear();
    }

    public void sendOnEnter(KeyEvent keyEvent){
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String message = writeTextArea.getText();
            if (!message.equals(null)) {
                SocketManager.sendMessage(message, "m " + mainViewController.messageRecipient + " ");
                mainViewController.userChatViews.get(mainViewController.messageRecipient).getChildren().addAll(new Text(message));
            }
            writeTextArea.clear();
            writeTextArea.requestFocus();
        }
    }

    public void goBackButtonHandler() throws IOException {
        Stage window = (Stage)(goBackButton).getScene().getWindow();
        try {

            chatScrollPane.setContent(null);
            writeTextArea.clear();
            window.setScene(mainViewController.mainViewScene);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mainViewController.messageRecipient = "";
        System.out.println("gb");
    }

    public void showIncomingMessage(String message){
        Text t = new Text(message + '\n');
        t.setTextAlignment(TextAlignment.RIGHT);
        mainViewController.userChatViews.get(mainViewController.messageRecipient).getChildren().addAll(t);
    }

}
