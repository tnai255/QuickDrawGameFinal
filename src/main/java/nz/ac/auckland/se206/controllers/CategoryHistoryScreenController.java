package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.HBox;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.App.View;

public class CategoryHistoryScreenController {

  @FXML private ListView<String> categoryHistoryListViewOne;
  @FXML private ListView<String> categoryHistoryListViewTwo;
  @FXML private HBox historyHbox;

  private List<String> categoryHistory;

  public void initialize() {

    // gets category history and displays every time view is changed
    App.subscribeToViewChange(
        (View view) -> {
          if (view == View.CATEGORYHISTORY) {

            categoryHistory =
                new ArrayList<String>(
                    App.getProfileManager().getCurrentProfile().getCategoryHistory());

            // TODO: Find a better way to do this resizing
            // dynamically resize list hbox height (doesn't show more cells than necessary)
            historyHbox.setPrefHeight((categoryHistory.size() + 1) * 15 + 2);

            bindScrollBars();

            setCategoryHistoryLists();
          }
        });
  }

  /** Binds the scroll bars of the two lists */
  private void bindScrollBars() {
    categoryHistoryListViewOne.applyCss();
    categoryHistoryListViewTwo.applyCss();

    ScrollBar sb1 = (ScrollBar) categoryHistoryListViewOne.lookup(".scroll-bar");
    ScrollBar sb2 = (ScrollBar) categoryHistoryListViewTwo.lookup(".scroll-bar");
    sb1.valueProperty().bindBidirectional(sb2.valueProperty());
  }

  /** Displays word history by splitting list into two even sublists */
  private void setCategoryHistoryLists() {

    // split list evenly into two lists
    categoryHistoryListViewOne.setItems(
        FXCollections.observableArrayList(
            categoryHistory.subList(0, (categoryHistory.size() + 1) / 2)));

    categoryHistoryListViewTwo.setItems(
        FXCollections.observableArrayList(
            categoryHistory.subList((categoryHistory.size() + 1) / 2, categoryHistory.size())));
  }

  @FXML
  private void onPlayAgain() {
    App.setView(View.CATEGORY);
  }

  @FXML
  private void onBackToStats() {
    App.setView(View.USERSTATS);
  }
}