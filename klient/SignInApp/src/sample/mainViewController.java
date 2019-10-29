package sample;

import java.io.IOException;

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


    public void showActiveUsers(ActionEvent event) throws IOException
    {
        ObservableList<String> userList = FXCollections.<String>observableArrayList("Apple", "Banana", "Orange", "Mango", "Grape", "Coconut",
                "Vanilla", "Pineapple", "Pear", "Lemon");
        listOfActive.setStyle("-fx-font-size: 18px;");
        listOfActive.getItems().addAll(userList);
        listOfActive.getSelectionModel().getSelectedItem();
        newMessageButton.setBackground(Background.EMPTY);
        newMessageButton.setStyle("-fx-background-image: url('http://icons.iconarchive.com/icons/google/noto-emoji-objects/512/62891-envelope-with-arrow-icon.png')");

    }

}
