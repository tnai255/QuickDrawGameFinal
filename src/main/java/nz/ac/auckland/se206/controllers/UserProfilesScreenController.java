package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.App.View;

public class UserProfilesScreenController {

  @FXML private Button newGameButton;
  @FXML private Pagination profilesPagination;
  @FXML private VBox profilesVBox;
  @FXML private Pane newUserPane;
  @FXML private TextField usernameTextField;
  @FXML private Label errorMessage;
  @FXML private ColorPicker colorPicker;

  // TODO: Each user can choose a color for their box?
  private Color[] colors =
      new Color[] {
        Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.INDIGO, Color.VIOLET
      };

  // TODO: This would be a string of usernames I need.
  final String[] username =
      new String[] {
        "this is a test 1",
        "this is a test 2",
        "this is a test 3",
        "this is a test 4",
        "this is a test 5",
        "this is a test 6",
        "this is a test 7",
      };

  /** Creates pagination */
  // TODO: this should happen every time the view is called (so it creates new user profile)
  public void initialize() {
    profilesPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
    profilesPagination.setPageFactory(
        (Integer pageIndex) -> {
          if (pageIndex >= username.length) {
            return null;
          } else {
            return createProfile(pageIndex);
          }
        });
    profilesPagination.setPageCount(username.length);
  }

  /**
   * Creates each page (profiles) from username and colors given
   *
   * @param pageIndex
   * @return
   */
  public VBox createProfile(int pageIndex) {
    VBox box = new VBox(5);
    box.setAlignment(Pos.CENTER);

    Button userButton = new Button(username[pageIndex]);
    userButton.setId(username[pageIndex]);

    userButton.getStyleClass().clear();
    String color = "-fx-background-color: " + colors[pageIndex].toString().replace("0x", "#") + ";";
    userButton.setStyle(getStyle(color));

    box.getChildren().add(userButton);

    onUserButtonClicked(userButton);

    return box;
  }

  /**
   * When profile is clicked it switches to category screen to play and sends user profile details
   * to profile manager
   *
   * @param userButton
   */
  private void onUserButtonClicked(Button userButton) {

    userButton.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {

            // TODO: send username to profile manager
            System.out.println(userButton.getId());

            // gets controller to update category
            CategoryScreenController categoryScreen =
                App.getLoader("category-screen").getController();
            categoryScreen.updateCategory();
            App.setView(View.CATEGORY);
          }
        });
  }

  /** Switches to new user pane */
  @FXML
  private void onCreateUser() {

    profilesVBox.setVisible(false);
    newUserPane.setVisible(true);
  }

  /** Checks if user input is invalid, takes the inputs and then starts playing */
  @FXML
  private void onStartGame() {

    if (!usernameTextField.getText().isBlank()
        && !colorPicker.getValue().equals(Color.TRANSPARENT)) {

      // TODO: send username and color to profile manager
      System.out.println(usernameTextField.getText() + " " + colorPicker.getValue().toString());

      // gets controller to update category
      CategoryScreenController categoryScreen = App.getLoader("category-screen").getController();
      categoryScreen.updateCategory();
      App.setView(View.CATEGORY);
    } else {
      errorMessage.setText("Please enter a valid username or color.");
    }
  }

  /**
   * Creates style of each profile button
   *
   * @param color
   * @return
   */
  private String getStyle(String color) {

    return "-fx-background-image: url('images/profile.png');"
        + "-fx-background-repeat: no-repeat;"
        + "-fx-background-position: center center;"
        + "-fx-background-size: 200 200;"
        + "-fx-background-radius: 40px 40px 0 0;"
        + "-fx-border-style: solid;"
        + "-fx-border-color: black;"
        + "-fx-border-radius: 35px 35px 0 0;"
        + "-fx-border-width: 3px;"
        + "-fx-font-family: 'Londrina Solid';"
        + "-fx-text-fill: black;"
        + "-fx-font-size: 18px;"
        + "-fx-pref-width: 200;"
        + "-fx-pref-height: 200;"
        + "-fx-alignment: bottom-center;"
        + "-fx-wrap-text: true;"
        + color
        + ";";
  }
}
