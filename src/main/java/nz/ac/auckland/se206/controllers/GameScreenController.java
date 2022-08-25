package nz.ac.auckland.se206.controllers;

import ai.djl.ModelException;
import ai.djl.modality.Classifications.Classification;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.App.View;
import nz.ac.auckland.se206.controllers.CanvasManager.DrawMode;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.util.CategoryGenerator;
import nz.ac.auckland.se206.util.CategoryGenerator.Difficulty;
import nz.ac.auckland.se206.util.CountdownTimer;
import nz.ac.auckland.se206.util.PredictionManager;
import nz.ac.auckland.se206.util.PredictionManager.ClassificationListener;
import nz.ac.auckland.se206.util.PredictionManager.SnapshotProvider;

/**
 * This is the controller of the canvas. You are free to modify this class and the corresponding
 * FXML file as you see fit. For example, you might no longer need the "Predict" button because the
 * DL model should be automatically queried in the background every second.
 *
 * <p>!! IMPORTANT !!
 *
 * <p>Although we added the scale of the image, you need to be careful when changing the size of the
 * drawable canvas and the brush size. If you make the brush too big or too small with respect to
 * the canvas size, the ML model will not work correctly. So be careful. If you make some changes in
 * the canvas and brush sizes, make sure that the prediction works fine.
 */
public class GameScreenController {

  @FXML private Canvas canvas;
  @FXML private Pane canvasContainerPane;

  @FXML private Button pencilButton;
  @FXML private Button eraserButton;
  @FXML private Button clearButton;

  @FXML private Button returnHomeButton;
  @FXML private Button gameActionButton;
  @FXML private Button downloadImageButton;

  @FXML private Label timeRemainingLabel;
  @FXML private Label whatToDrawLabel;

  @FXML private VBox guessLabelContainer;

  private Label[] guessLabels = new Label[10];

  private enum GameState {
    ENDED,
    READY,
    PLAYING,
  }

  private GameState gameState = GameState.READY;

  // TODO: Extract these into a setting page
  // TODO: Make timer seconds more readable
  private int gameLengthSeconds = 30;
  private int numTopGuessNeededToWin = 3;

  private String categoryToGuess;

  private CanvasManager canvasManager;
  private PredictionManager predictionManager;
  private CountdownTimer countdownTimer;
  private CategoryGenerator categoryGenerator;
  private boolean playerDidWin = false;
  private TextToSpeech textToSpeech;

  /**
   * JavaFX calls this method once the GUI elements are loaded. In our case we create a listener for
   * the drawing, and we load the ML model.
   *
   * @throws ModelException If there is an error in reading the input/output of the DL model.
   * @throws IOException If the model cannot be found on the file system.
   */
  public void initialize() throws ModelException, IOException {

    // Get guess labels
    int labelIndex = 0;
    for (Node child : guessLabelContainer.getChildren()) {
      if (child instanceof Label) {
        guessLabels[labelIndex] = (Label) child;
        labelIndex++;
      }
    }

    textToSpeech = new TextToSpeech();

    // TODO: Add possiblility to register multiple functions rather than set this to be the only
    // one. Something like App.getStage().registerOnClose(() -> {...})
    App.getStage()
        .setOnCloseRequest(
            (e) -> {
              System.out.println("Terminating application");
              textToSpeech.terminate();
            });

    categoryGenerator = new CategoryGenerator("category_difficulty.csv");

    canvasManager = new CanvasManager(canvas);

    // Add a border to make the canvas visible
    canvasContainerPane.setBorder(
        new Border(
            new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

    // This is necessary to tell the app that the view has been switched.
    // The on view change method wll run every time the app switches views.
    App.subscribeToViewChange((View v) -> onViewChanged(v));

    ////////////////////////////// TIMER SECTION //////////////////////////////

    // This object handles the main timer
    countdownTimer = new CountdownTimer();

    // Update the game controlelrs time label when the time updates and completes
    countdownTimer.setOnChange(
        (int secondsRemaining) -> {
          Platform.runLater(
              () -> {
                updateTimerLabel(secondsRemaining);
              });
        });

    countdownTimer.setOnComplete(
        () -> {
          Platform.runLater(
              () -> {
                onTimerComplete();
              });
        });

    ////////////////////////////// END TIMER SECTION //////////////////////////////

    ////////////////////////////// PREDICTION MANAGER SECTION //////////////////////////////

    // This provides a method which passes the snapshot from the canvas to the prediction manager
    final SnapshotProvider snapshotProvider =
        new SnapshotProvider() {
          @Override
          public BufferedImage getCurrentSnapshot() {

            // This is used to run the get current snapshot on the javafx
            // thread and then once the task is complete, we can use the returned result
            final FutureTask<BufferedImage> futureTask =
                new FutureTask<BufferedImage>(
                    new Callable<BufferedImage>() {

                      @Override
                      public BufferedImage call() throws Exception {
                        return canvasManager.getCurrentSnapshot();
                      }
                    });

            // Run our task
            Platform.runLater(futureTask);
            try {
              // Return the task result
              return futureTask.get();
            } catch (InterruptedException | ExecutionException e) {
              return null; // TODO: Make sure this is safe
            }
          }
        };

    // This creates an anyonymous class with method which listens for updates from the snapshot
    // provider
    // and sends the result to the game controllers update guesses function
    final ClassificationListener classificationListener =
        new ClassificationListener() {
          @Override
          public void classificationReceived(List<Classification> classificationList) {
            Platform.runLater(
                () -> {
                  onGuessChange(classificationList);
                });
          }
        };

    // The prediction manager takes care of everything to do with guessing the drawing
    predictionManager =
        new PredictionManager(100, guessLabels.length, snapshotProvider, classificationListener);

    ////////////////////////////// END PREDICTION MANAGER SECTION //////////////////////////////
  }

  /**
   * This method updates the guess labels with the top guesses
   *
   * @param classificationList the list of top guesses from the model with percentage likelihood
   */
  private void onGuessChange(List<Classification> classificationList) {
    int range = Math.min(classificationList.size(), guessLabels.length);

    for (int i = 0; i < range; i++) {
      String classification = classificationList.get(i).getClassName().replace('_', ' ');

      if (i < numTopGuessNeededToWin) {
        if (classification.equals(categoryToGuess)) {
          playerDidWin = true;
          setGameState(GameState.ENDED);
        }
      }

      // getClassName does not give the name of a java class but the label of the classification
      String guessText = classificationList.get(i).getClassName().replace('_', ' ');
      guessLabels[i].setText((i + 1) + ": " + guessText);
    }
  }

  /**
   * This function is called every time the view is changed
   *
   * @param currentView
   */
  private void onViewChanged(View currentView) {
    if (currentView == View.HOME) {
      // When the view is changed to home, we stop the timer and stop the prediction
      predictionManager.stopListening();
      countdownTimer.cancelCountdown();

    } else if (currentView == View.GAME) {
      // When the view changes to game, we start a new game and clear the canvas

      canvasManager.clear();
      setGameState(GameState.READY);
    }
  }

  /**
   * Sets the state of the game regardless of the previous state.
   *
   * @param newGameState the new game state.
   */
  private void setGameState(GameState newGameState) {

    // TODO: Clean this functionality as this may be prone to complexity and bugs

    gameState = newGameState;
    if (newGameState == GameState.ENDED) {
      // End the game and display the results
      gameActionButton.setText("New Category");
      updateTimerLabel(0);
      if (playerDidWin) {
        whatToDrawLabel.setText("You got it! :)");
        textToSpeech.speakAsync("You got it");
      } else {
        textToSpeech.speakAsync("Sorry, you ran out of time");
      }

      canvasManager.setDrawingEnabled(false);
      setCanvasButtonsDisabled(true);

      countdownTimer.cancelCountdown();
      predictionManager.stopListening();
    }
    if (newGameState == GameState.READY) {
      // Create a new category so the player is ready

      gameActionButton.setText("Start Game");
      updateTimerLabel(gameLengthSeconds);

      setCanvasButtonsDisabled(true);
      canvasManager.clear();
      canvasManager.setDrawingEnabled(false);

      generateNewCategoryAndUpdateLabel();
      textToSpeech.speakAsync("Draw a " + categoryToGuess);

      countdownTimer.cancelCountdown();
      predictionManager.stopListening();
    }
    if (newGameState == GameState.PLAYING) {
      // Start a new round
      playerDidWin = false;

      gameActionButton.setText("Give Up");

      setCanvasButtonsDisabled(false);
      canvasManager.setDrawingEnabled(true);
      canvasManager.clear();

      countdownTimer.startCountdown(gameLengthSeconds);
      predictionManager.startListening();
    }
  }

  /** This function takes the current game state and progresses to the next natural game state */
  private void progressGame() {
    // Self explanatory
    if (gameState == GameState.READY) {
      setGameState(GameState.PLAYING);
    } else if (gameState == GameState.PLAYING) {
      setGameState(GameState.ENDED);
    } else if (gameState == GameState.ENDED) {
      setGameState(GameState.READY);
    }
  }

  /** This function sets a new category to guess and updates the label. */
  private void generateNewCategoryAndUpdateLabel() {
    categoryToGuess = categoryGenerator.generateCategory(Difficulty.EASY);
    whatToDrawLabel.setText("To draw: " + categoryToGuess);
  }

  /**
   * This function sets the timer label to the time based on the number of seconds given
   *
   * @param numberSeconds the number of seconds remaining on the timer
   */
  void updateTimerLabel(int numberSeconds) {
    int seconds = (numberSeconds % 60);
    int minutes = (numberSeconds / 60);

    // Update the time label with the new minutes and seconds.
    timeRemainingLabel.setText(String.format("%2d:%2d", minutes, seconds).replace(' ', '0'));
  }

  /** This function is called when the timer reaches 0 */
  private void onTimerComplete() {
    // End the game
    setGameState(GameState.ENDED);
  }

  private void setCanvasButtonsDisabled(boolean disabled) {
    pencilButton.setDisable(disabled);
    eraserButton.setDisable(disabled);
    clearButton.setDisable(disabled);
  }

  // The following methods are associated with button presses

  @FXML
  private void onPencilSelected() {
    canvasManager.setDrawMode(DrawMode.DRAWING);
  }

  @FXML
  private void onEraserSelected() {
    canvasManager.setDrawMode(DrawMode.ERASING);
  }

  @FXML
  private void onClearCanvas() {
    canvasManager.clearOnlyIfDrawingEnabled();
  }

  @FXML
  private void onReturnHome() {
    App.setView(View.HOME);
  }

  @FXML
  private void onProgressGame() {
    progressGame();
  }

  @FXML
  private void onDownloadImage() {
    try {
      canvasManager.saveCurrentSnapshotOnFile();
    } catch (IOException e) {
      System.out.println("Failed to download");
    }
  }
}
