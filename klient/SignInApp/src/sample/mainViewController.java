package sample;

import java.io.IOException;

import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class mainViewController {

    //@FXML private Pane mainViewBackground;
    @FXML private ListView listOfActive;
    @FXML private Button newMessageButton;
    @FXML public Button logoutButton;
    private ObservableList<String> userList;
    public loginController loginController;
    public Scene mainViewScene;
    public Scene loginScene;

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
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        //Parent logViewParent = loader.load();
        //Scene logScene = new Scene(logViewParent);
        Stage window = (Stage)(logoutButton).getScene().getWindow();
        try {
            listOfActive.getItems().clear();
            window.setScene(loginScene);
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
        System.out.println(chosen.get(0));
    }

}
