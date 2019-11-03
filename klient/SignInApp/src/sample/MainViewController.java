package sample;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class MainViewController {

    //@FXML private Pane mainViewBackground;
    @FXML private ListView listOfActive;
    @FXML private ListView incomingListView;
    //@FXML private Button newMessageButton;
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

    public void showActiveUsers(String[] users) throws IOException
    {
        userList = FXCollections.<String>observableArrayList();
        for (int i = 1; i < users.length; i++) {
            userList.add(users[i]);
        }
        //listOfActive.setStyle("-fx-font-size: 18px;");
        listOfActive.getItems().addAll(userList);
        listOfActive.getSelectionModel().getSelectedItem();
    }

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

    public void incomingListHandler(MouseEvent mouseEvent){
        if (mouseEvent.isPrimaryButtonDown() && mouseEvent.getClickCount() == 2) {
            System.out.println(incomingListView.getSelectionModel().getSelectedItem());
        }
        incomingTitledPane.setVisible(false);
        switchToChatView(incomingListView);
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
    }

    public void openChatButtonHandler(ActionEvent event){
        switchToChatView(listOfActive);
    }

    public void switchToChatView(ListView list){
        ObservableList<String> chosen = list.getSelectionModel().getSelectedItems();
        messageRecipient = chosen.get(0);
        Stage window = (Stage)openChatButton.getScene().getWindow();
        //userChatViews.putIfAbsent(messageRecipient, createVBox());
        if (!userChatViews.containsKey(messageRecipient)) {
            userChatViews.put(messageRecipient, createVBox());
        }
        try {
            window.setScene(chatViewController.chatViewScene);
            chatViewController.chatScrollPane.setContent(userChatViews.get(messageRecipient));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        boolean wasAuthorRemoved;
        wasAuthorRemoved = removeAuthorFromListOfUnreadAuthorsIfNeeded(messageRecipient);
        if (wasAuthorRemoved) {
            System.out.println("UPDATE!");
            updateNewMessagesLabel();
        }
    }

    public VBox createVBox(){
        System.out.println("Nowy pane dla " + messageRecipient); // <- ten output jest bardzo mylacy <ROBI ZAMĘT JAK JA PIER****>, gdyz ta funkcja jest wywolywana tez przy otrzymaniu wiadomosci. Wtedy messageRecipient jest pusty, co pokazuje komunikat. Mimo to VBox tworzony jest poprawnie - dla nadawcy.
        VBox userBox = new VBox();
        userBox.setPrefWidth(chatViewController.chatScrollPane.getPrefWidth()-20);
        //userBox.setPrefHeight(chatViewController.chatScrollPane.getPrefHeight()-20); <- to psuło scrollowanie
        userBox.setMinHeight(chatViewController.chatScrollPane.getPrefHeight()-20);
        userBox.getStyleClass().add("root");
        userBox.setSpacing(10);
        //chatViewController.chatScrollPane.setContent(userBox); <- to był błąd, powód j.w. PONADTO śmiem twierdzić, że usunięcie tej linii wcale nie psuje programu, gdyż w funkcji switchToChatView (jest ona wyżej, w tym kontrolerze) też ustawiasz ten pane jako content scrollpane'a
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
