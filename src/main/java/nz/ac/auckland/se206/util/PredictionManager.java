package nz.ac.auckland.se206.util;

import ai.djl.ModelException;
import ai.djl.modality.Classifications.Classification;
import ai.djl.translate.TranslateException;
import com.opencsv.exceptions.CsvException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ml.DoodlePrediction;

public class PredictionManager {

  private DataSource<BufferedImage> imageSource;
  private EventListener<List<Classification>> predictionListener;

  private final DoodlePrediction model;
  private final Thread pollResultThread;

  private long pollInterval;

  private boolean isMakingPredictions = false;

  private Map<CategoryType, Set<String>> categories;

  /**
   * The manager uses the snapshot provider to get images and then runs a prediction on those
   * images. When the prediction is complete, the manager will send the results to the prediction
   * listener
   *
   * @param pollInterval how often the manager will send queires to the server to get predictions in
   *     milliseconds. The poll interval is bound below by 10ms.
   * @param imageSource a class which implements the {@link DataSouce} interface
   * @param predictionListener a class which implements the {@link EventListener} interface
   * @throws IOException If there is an error in reading the input/output of the DL model.
   * @throws ModelException If the model cannot be found on the file system.
   */
  public PredictionManager(long pollInterval, int numTopGuesses)
      throws IOException, ModelException {

    try {

      Map<CategoryType, List<String>> loaded =
          new CsvKeyValuePairLoader<CategoryType, String>(
                  (keyString) -> {
                    if (keyString.equals("E")) {
                      return CategoryType.EASY;
                    }
                    if (keyString.equals("M")) {
                      return CategoryType.MEDIUM;
                    }
                    if (keyString.equals("H")) {
                      return CategoryType.HARD;
                    }
                    return null;
                  },
                  (v) -> v)
              .loadCategoriesFromFile(App.getResourcePath("category_difficulty.csv"), true);

      categories = new HashMap<CategoryType, Set<String>>();
      categories.put(CategoryType.EASY, new HashSet<String>(loaded.get(CategoryType.EASY)));
      categories.put(CategoryType.MEDIUM, new HashSet<String>(loaded.get(CategoryType.MEDIUM)));
      categories.put(CategoryType.HARD, new HashSet<String>(loaded.get(CategoryType.HARD)));

    } catch (CsvException e) {
      App.expect("Category CSV is in the resource folder and is not empty", e);
    }

    model = new DoodlePrediction();

    pollResultThread =
        new Thread() {
          {
            setDaemon(true);
          }

          @Override
          public void run() {
            while (true) {

              // TODO: Memoize the input image so we are not making unnecessary queires

              if (isMakingPredictions) {
                try {
                  if (predictionListener != null && imageSource != null) {
                    BufferedImage snapshot = imageSource.getData();
                    if (snapshot != null) {
                      predictionListener.update(model.getPredictions(snapshot, numTopGuesses));
                    }
                  }
                } catch (TranslateException error) {
                  System.out.println("Prediction manager failed prediction: " + error.getMessage());
                }
              }

              try {
                // We add a small delay so as to not hog the cpu
                Thread.sleep(Math.max(pollInterval, 10));
              } catch (InterruptedException e) {
                System.out.println(
                    "Thread - " + Thread.currentThread().getName() + " was interrupted");
              }
            }
          }
        };

    pollResultThread.start();
  }

  /**
   * Sets the image source which will be used to make predictions. If you input null, nothing will
   * happen
   *
   * @param imageSource a class which has an image providing function
   */
  public void setImageSource(DataSource<BufferedImage> imageSource) {
    if (imageSource != null) {
      this.imageSource = imageSource;
    }
  }

  /**
   * Sets the class which listens out for recent predictions in the model. If you input null,
   * nothing will happen.
   *
   * @param predictionListener A class which implements the EventListener Interface
   */
  public void setPredictionListener(EventListener<List<Classification>> predictionListener) {
    if (predictionListener != null) {
      this.predictionListener = predictionListener;
    }
  }

  public long getPredictionPollInterval() {
    return pollInterval;
  }

  /**
   * Sets the poll interval. The model is bound below by 10 so setting this below 10 will
   * effectively set the poll interval to 10ms
   *
   * @param pollInterval the poll interval in milliseconds
   */
  public void setPredictionPollInterval(long pollInterval) {
    this.pollInterval = pollInterval;
  }

  /** Start making predictions on the model and sending the results to the prediction listener */
  public void startMakingPredictions() {
    isMakingPredictions = true;
  }

  public void stopMakingPredictions() {
    isMakingPredictions = false;
  }

  public boolean isMakingPredictions() {
    return isMakingPredictions;
  }

  /**
   * This generates a new random category, updates the category for the class and returns the value
   * of the new category. It will not use any values in the provided set
   *
   * @param categoryFilter
   * @return
   */
  public Category getNewRandomCategory(
      Set<String> categoryFilter, boolean includeEasy, boolean includeMedium, boolean includeHard)
      throws FilterTooStrictException {

    List<String> possibleCategories = new ArrayList<String>();

    if (includeEasy) {
      possibleCategories.addAll(categories.get(CategoryType.EASY));
    }
    if (includeMedium) {
      possibleCategories.addAll(categories.get(CategoryType.MEDIUM));
    }
    if (includeHard) {
      possibleCategories.addAll(categories.get(CategoryType.HARD));
    }

    // Removes all the items which are also in the filter set (set subtraction)
    possibleCategories =
        possibleCategories.stream()
            .filter((category) -> !categoryFilter.contains(category))
            .collect(Collectors.toList());

    if (possibleCategories.isEmpty()) {
      throw new FilterTooStrictException("The filter filtered out all categories");
    }

    // Get random index from remaining items
    int randomIndexFromList = ThreadLocalRandom.current().nextInt(possibleCategories.size());

    String category = possibleCategories.get(randomIndexFromList);

    CategoryType categoryType = CategoryType.EASY;

    if (categories.get(CategoryType.EASY).contains(category)) {
      categoryType = CategoryType.EASY;
    } else if (categories.get(CategoryType.MEDIUM).contains(category)) {
      categoryType = CategoryType.MEDIUM;
    } else if (categories.get(CategoryType.HARD).contains(category)) {
      categoryType = CategoryType.HARD;
    }

    return new Category(category, categoryType);
  }
}
