package snakeladder.game;

import snakeladder.utility.ServicesRandom;
import java.util.ArrayList;

public class Die {
  private int nb;
  private boolean outOfNum;
  private boolean ifStartRolling;
  private int numIndex;
  private ArrayList<Integer> numList;
  private final int RANDOM_ROLL_TAG = -1;

  Die(ArrayList<Integer> numList) {
    this.outOfNum = false;
    this.numList = numList;
    numIndex = 0;
  }

  public int switchToNextNumb() {
    if (numIndex < numList.size()) {
      nb = numList.get(numIndex);
      numIndex++;
      return nb;
    } else {
      outOfNum = true;
      return RANDOM_ROLL_TAG;
    }
  }

  public int roll(NavigationPane np, int tag) {

    if (tag != 0) {
      nb = tag;
      numIndex++;
    } else {
      if (outOfNum) {
        this.nb = ServicesRandom.get().nextInt(6) + 1;
      } else {
        switchToNextNumb();
      }
    }
    np.removeActors(Face.class);
    Face newFace = new Face(np, nb);
    np.addActor(newFace, np.getDieBoardLocation());
    ifStartRolling = true;
    return this.nb;
  }

}
