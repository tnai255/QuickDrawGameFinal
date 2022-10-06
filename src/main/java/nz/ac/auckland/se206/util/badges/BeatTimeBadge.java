package nz.ac.auckland.se206.util.badges;

import nz.ac.auckland.se206.GameLogicManager.GameEndInfo;

public class BeatTimeBadge extends Badge {

  public BeatTimeBadge(GameEndInfo gameInfo) {
    super(gameInfo);
    this.name = "beatTime";
    this.description = "Beat your previous time for this word";
  }

  @Override
  public Boolean isEarned() {

    // TODO: implement logic
    return null;
  }
}
