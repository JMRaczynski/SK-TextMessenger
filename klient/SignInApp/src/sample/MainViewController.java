package sample;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainViewController {

    @FXML private ListView listOfActive;
    @FXML private ListView incomingListView;
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
    private Boolean incomingVisibilityState = false;
    public Map<String, VBox> userChatViews;

    public void initializeUnreadAuthorsList() {
        unreadMessagesAuthors =  FXCollections.<String>observableArrayList();
    }

    public void initializeUserMap() {
        userChatViews = new HashMap<>();
    }

    public void showActiveUsers(String[] users)
    {
        userList = FXCollections.<String>observableArrayList();
        for (int i = 2; i < users.length; i++) {
            userList.add(users[i]);
        }
        listOfActive.getItems().addAll(userList);
        listOfActive.getSelectionModel().getSelectedItem();
    }

    // wyswietlanie i chowanie listy uzytkownikow, od ktorych mamy nieprzeczytane wiadomosci
    public void newMessageButtonHandler(ActionEvent event){
        if(!incomingVisibilityState) {
            incomingListView.setItems(unreadMessagesAuthors);
            incomingTitledPane.setVisible(true);
            incomingVisibilityState = true;
            System.out.println("lista");
        }
        else{
            incomingTitledPane.setVisible(false);
            incomingVisibilityState = false;
            System.out.println("schowana lista");
        }
    }

    // zmiana sceny na okno czatu po wybraniu uzytkownika, od ktorego mamy nieprzeczytana wiadomosc
    public void incomingListHandler(MouseEvent mouseEvent){
        if (mouseEvent.isPrimaryButtonDown() && mouseEvent.getClickCount() == 2) {
            System.out.println(incomingListView.getSelectionModel().getSelectedItem());
        }
        switchToChatView(incomingListView);
        incomingTitledPane.setVisible(false);
        incomingVisibilityState = false;
    }

    public void logoutButtonHandler(ActionEvent event) {
        SocketManager.sendMessage("sampletext", "o");
        Stage window = (Stage)(logoutButton).getScene().getWindow();
        try {
            listOfActive.getItems().clear();
            window.setScene(loginController.loginScene);
            window.setTitle("Talkie App");
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void openChatButtonHandler(ActionEvent event){
        switchToChatView(listOfActive);
    }

    public void switchToChatView(ListView list) {
        if (list.getSelectionModel().getSelectedItems().size() == 0) {
            return;
        }
        ObservableList<String> chosen = list.getSelectionModel().getSelectedItems();
        messageRecipient = chosen.get(0);
        Stage window = (Stage)openChatButton.getScene().getWindow();
        if (!userChatViews.containsKey(messageRecipient)) {
            userChatViews.put(messageRecipient, createVBox());
        }
        try {
            window.setScene(chatViewController.chatViewScene);
            chatViewController.chatScrollPane.setContent(userChatViews.get(messageRecipient));
            chatViewController.recipientWindowNameLabel.setText("Rozmowa z " + messageRecipient);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if (userList.contains(messageRecipient)) {
            chatViewController.switchLoggedOutWarningForTextArea();
        }
        else {
            chatViewController.switchTextAreaForLoggedOutWarning();
        }
        boolean wasAuthorRemoved;
        wasAuthorRemoved = removeAuthorFromListOfUnreadAuthorsIfNeeded(messageRecipient);
        if (wasAuthorRemoved) {
            System.out.println("UPDATE!");
            updateNewMessagesLabel();
        }
    }

    // utworzenie kontenera na dymki z wiadomościami w konkretnej konwersacji
    public VBox createVBox(){
        System.out.println("Nowy pane dla " + messageRecipient);
        VBox userBox = new VBox();
        userBox.setPrefWidth(chatViewController.chatScrollPane.getPrefWidth()-20);
        userBox.setMinHeight(chatViewController.chatScrollPane.getPrefHeight()-20);
        userBox.getStyleClass().add("vbox");
        userBox.setSpacing(10);
        chatViewController.chatScrollPane.vvalueProperty().bind(userBox.heightProperty());
        return userBox;
    }

    public void updateNewMessagesLabel() {
        newMessageLabel.setText(newMessageLabels[Math.min(3, unreadMessagesAuthors.size())]);
    }

    public void addUserToListOfActiveUsers(String username) {
        userList.add(username);
        listOfActive.getItems().add(userList.get(userList.size() - 1));
    }

    public void removeUserFromListOfActiveUsers(String username) {
        listOfActive.getItems().remove(username);
        userList.remove(username);
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
}
