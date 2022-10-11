package nz.ac.auckland.se206.util.difficulties;

import com.google.gson.annotations.SerializedName;

public enum Confidence {
  @SerializedName("easyConf")
  EASY("easy", 1),
  @SerializedName("medConf")
  MEDIUM("medium", 10),
  @SerializedName("hardConf")
  HARD("hard", 25),
  @SerializedName("mastConf")
  MASTER("master", 50);

  private final double confidenceLevel;
  private final String label;

  private Confidence(String label, int confidenceLevel) {
    this.label = label;
    this.confidenceLevel = confidenceLevel;
  }

  /**
   * Call this method when you want to get the confidence requirement at a specific confidence level
   * e.g input: Confidence.HARD output: percentage of how 'confident' the ML model must be Will
   * return a whole number for now that must be divided 100, can change this as you see fit though
   *
   * @param difficulty enum of type Confidence that you want to get the required confidence for e.g
   *     Confidence.HARD
   * @return requirement at wanted confidence level
   */
  public double getProbabilityLevel() {
    return this.confidenceLevel;
  }

  /**
   * Call this method when you want the label of the Confidence difficulty
   *
   * @param difficulty enum of type Confidence with specified level e.g Confidence.EASY
   * @return label of specified level containing the level and difficulty e.g easyConfidence
   */
  public String getLabel() {
    return this.label;
  }
}