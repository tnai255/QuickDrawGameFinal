package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.App.View;
import nz.ac.auckland.se206.QuickDrawGameManager;

public class NewUserScreenController {

  @FXML private Button newGameButton;
  @FXML private Button backButton;
  @FXML private Pagination profilesPagination;
  @FXML private VBox profilesVbox;
  @FXML private Pane newUserPane;
  @FXML private TextField usernameTextField;
  @FXML private ColorPicker colorPicker;

  /** Initializes the new user screen controller. */
  public void initialize() {

    // Reset input field every time screen is entered and allows a back button to profile screen if
    // profiles exist
    App.subscribeToViewChange(
        (View view) -> {
          if (view == View.NEWUSER) {
            // doesn't store previous user values
            usernameTextField.clear();
            colorPicker.setValue(Color.TRANSPARENT);

            // if there are existing users allow user to go back to user profiles screen by making
            // button visible
            if (!QuickDrawGameManager.getProfileManager().getProfiles().isEmpty()) {
              backButton.setVisible(true);
            }
          }
        });
  }

  /**
   * When start game is clicked (on new user pane) it switches to category screen after checking if
   * inputs are valid.
   */
  @FXML
  private void onCreateProfile() {

    // TODO: Better way to check if color picker has been selected or not
    // checks if username and colour picker has been selected
    // and creates profile (must return true to ensure it does not contain duplicate)
    // and checks is username is no longer than 20 characters
    if (!usernameTextField.getText().isBlank()
        && !colorPicker.getValue().equals(Color.TRANSPARENT)
        && QuickDrawGameManager.getProfileManager()
            .createProfile(usernameTextField.getText(), colorPicker.getValue().toString())
        && (usernameTextField.getText().length() <= 20)) {

      // changes view to user profiles
      App.setView(View.USERPROFILES);

    } else {
      // shows an alert pop up if username is blank or spaces and/or color hasn't been chosen
      Alert errorAlert = new Alert(AlertType.ERROR);
      DialogPane dialogPane = errorAlert.getDialogPane();
      dialogPane
          .getStylesheets()
          .add(getClass().getResource("/css/application.css").toExternalForm());

      // alerts the user if they are making a profile with invalid properties
      errorAlert.setTitle("Invalid Input");
      errorAlert.setContentText("Please enter a valid username or color.");
      errorAlert.showAndWait();
    }
  }

  /** Navigate user back to UserProfilesScreen FXML */
  @FXML
  private void onBackToProfile() {
    App.setView(View.USERPROFILES);
  }
}
