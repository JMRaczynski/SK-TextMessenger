package sample;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;


public class ChatViewController {

    @FXML private Button goBackButton;
    @FXML private TextArea writeTextArea;
    @FXML private Label logoutLabel;
    @FXML public AnchorPane chatBackgroundPane;
    @FXML public ScrollPane chatScrollPane;
    @FXML public Label recipientWindowNameLabel;

    public MainViewController mainViewController;
    public LoginController loginController;
    public Scene chatViewScene;

    // obsluga wyslania wiadomosci, w tej metodzie dodajemy tez do GUI dymek z nasza wiadomoscia
    public void sendOnEnter(KeyEvent keyEvent){
        if (keyEvent.getCode() == KeyCode.ENTER) {
            String message = writeTextArea.getText();
            System.out.println(message);
            if (!message.equals(null)) {
                int mLength = message.getBytes().length + mainViewController.messageRecipient.getBytes().length + 6;
                SocketManager.sendMessage(message, "m " + mLength + " " + mainViewController.messageRecipient + " ");
                Label l = new Label("Ty: " + message);
                l.getStyleClass().add("labelsend");
                l.setWrapText(true);
                HBox hBox=new HBox();
                hBox.setMaxWidth(250);
                hBox.getChildren().add(l);
                mainViewController.userChatViews.get(mainViewController.messageRecipient).getChildren().add(hBox);
            }
            writeTextArea.clear();
            writeTextArea.requestFocus();
            keyEvent.consume();
        }
    }

    // dodanie dymka z przychodzaca wiadomoscia
    public void showIncomingMessage(String message, String messageAuthor){
        if (!mainViewController.userChatViews.containsKey(messageAuthor)) {
            mainViewController.userChatViews.put(messageAuthor, mainViewController.createVBox());
        }
        Label l = new Label(messageAuthor + ": " + message);
        l.getStyleClass().add("labelreceive");
        l.setMaxWidth(250);
        l.setWrapText(true);
        HBox hBox=new HBox();
        hBox.setAlignment(Pos.BASELINE_RIGHT);
        hBox.getChildren().add(l);
        mainViewController.userChatViews.get(messageAuthor).getChildren().add(hBox);
    }

    // powrot do menu glownego (listy aktywnych uzytkownikow
    public void goBackButtonHandler() {
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
    }

    // zmiana pola tekstowego na wiadomosc o tym, ze rozmowca sie wylogowal
    public void switchTextAreaForLoggedOutWarning() {
        logoutLabel.setVisible(true);
        writeTextArea.setVisible(false);
    }

    // analogicznie do metody wyzej, dzieki temu uzytkownik nie musi wychodzic z okna czatu aby wyslac wiadomosc do uzytkownika ktory na chwile wyszedl i zaraz wrocil
    public void switchLoggedOutWarningForTextArea() {
        logoutLabel.setVisible(false);
        writeTextArea.setVisible(true);
    }
}
