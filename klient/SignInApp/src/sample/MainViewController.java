package sample;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.reflect.Array;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.stage.Stage;

public class MainViewController {

    //@FXML private Pane mainViewBackground;
    @FXML private ListView listOfActive;
    @FXML private ListView incomingListView;
    @FXML private Button newMessageButton;
    @FXML private Button logoutButton;
    @FXML private Button openChatButton;
    @FXML private Label newMessageLabel;
    @FXML private TitledPane incomingTitledPane;

    private ObservableList<String> userList;
    public LoginController loginController;
    public ChatViewController chatViewController;
    public Scene mainViewScene;
    public String messageRecipient = "";
    private String[] newMessageLabels = {"Brak nowych wiadomości", "1 nowa wiadomość", "2 nowe wiadomości", "2+ nowe wiadomości"};
    private ObservableList<String> unreadMessagesAuthors;
    //public Scene loginScene;

    public void showActiveUsers(String[] users) throws IOException
    {
        userList = FXCollections.<String>observableArrayList();
        for (int i = 1; i < users.length; i++) {
            userList.add(users[i]);
        }
        listOfActive.setStyle("-fx-font-size: 18px;");
        listOfActive.getItems().addAll(userList);
        listOfActive.getSelectionModel().getSelectedItem();
        newMessageButton.setBackground(Background.EMPTY);
        newMessageButton.setStyle("-fx-background-image: url('http://icons.iconarchive.com/icons/google/noto-emoji-objects/512/62891-envelope-with-arrow-icon.png')");

    }

    public void logoutButtonHandler(ActionEvent event) throws IOException {

        SocketManager.sendMessage("sampletext", "o");
        Stage window = (Stage)(logoutButton).getScene().getWindow();
        try {
            listOfActive.getItems().clear();
            window.setScene(loginController.loginScene);
            window.setTitle("Talkie App");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //window.show();
    }

    public void newMessageButtonHandler(ActionEvent event){
        incomingListView.setItems(unreadMessagesAuthors);
        incomingTitledPane.setVisible(true);
        System.out.println("lista");
    }

    public void incomingListHandler(MouseEvent mouseEvent){
        if (mouseEvent.isPrimaryButtonDown() && mouseEvent.getClickCount() == 2) {
            System.out.println(incomingListView.getSelectionModel().getSelectedItem());
        }
        incomingTitledPane.setVisible(false);
        switchToChatView();
    }

    public void addUserToList(String username) {
        userList.add(username);
        listOfActive.getItems().add(userList.get(userList.size() - 1));
    }

    public void removeUserFromList(String username) {
        listOfActive.getItems().remove(username);
        userList.remove(username);
    }

    public void switchToChatView(){
        ObservableList<String> chosen = listOfActive.getSelectionModel().getSelectedItems();
        Stage window = (Stage)openChatButton.getScene().getWindow();
        try {
            window.setScene(chatViewController.chatViewScene);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        messageRecipient = chosen.get(0);
        boolean wasAuthorRemoved;
        wasAuthorRemoved = removeAuthorFromListOfUnreadAuthorsIfNeeded(messageRecipient);
        if (wasAuthorRemoved) {
            System.out.println("UPDATE!");
            updateNewMessagesLabel();
        }
    }

    public void initializeUnreadAuthorsList() {
        unreadMessagesAuthors =  FXCollections.<String>observableArrayList();
    }

    public boolean addAuthorToListOfUnreadAuthorsIfNeeded(String senderNickname) {
        if (!messageRecipient.equals(senderNickname) && !unreadMessagesAuthors.contains(senderNickname)) {
            unreadMessagesAuthors.add(senderNickname);
            return true;
        }
        return false;
    }

    public boolean removeAuthorFromListOfUnreadAuthorsIfNeeded(String senderNickname) {
        if (unreadMessagesAuthors.contains(senderNickname)) {
            unreadMessagesAuthors.remove(senderNickname);
            return true;
        }
        return false;
    }

    public void updateNewMessagesLabel() {
        newMessageLabel.setText(newMessageLabels[Math.min(3, unreadMessagesAuthors.size())]);
    }
}
