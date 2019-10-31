package sample;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.stage.Stage;

public class MainViewController {

    //@FXML private Pane mainViewBackground;
    @FXML private ListView listOfActive;
    @FXML private Button newMessageButton;
    @FXML private Button logoutButton;
    @FXML private Button openChatButton;
    private ObservableList<String> userList;
    public LoginController loginController;
    public ChatViewController chatViewController;
    public Scene mainViewScene;
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
        System.out.println(chosen.get(0));

    }

}
