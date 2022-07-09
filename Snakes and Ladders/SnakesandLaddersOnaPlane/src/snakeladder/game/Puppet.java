package snakeladder.game;

import ch.aplu.jgamegrid.*;
import java.awt.Point;

public class Puppet extends Actor
{
  private GamePane gamePane;
  private int cellIndex = 0;
  private int nbSteps;
  private Connection currentCon = null;
  private int y;
  private int dy;
  private boolean isAuto;
  private String puppetName;
  private boolean isNotLowest = true;
  private boolean isCollide = false;

  Puppet(GamePane gp, String puppetImage)
  {
    super(puppetImage);
    this.gamePane = gp;
  }

  public boolean isAuto() {
    return isAuto;
  }

  public void setAuto(boolean auto) {
    isAuto = auto;
  }

  public String getPuppetName() {
    return puppetName;
  }

  public void setPuppetName(String puppetName) {
    this.puppetName = puppetName;
  }

  void go(int nbSteps)
  {
    if (cellIndex == 100)  // after game over
    {
      cellIndex = 0;
      setLocation(gamePane.startLocation);
    }
    this.nbSteps = nbSteps;
    // Check if lowest
    if(nbSteps == gamePane.getNp().getNumberOfDie()) {
      isNotLowest = false;
    }else{
      isNotLowest = true;
    }
    setActEnabled(true);
  }

  void resetToStartingPoint() {
    cellIndex = 0;
    setLocation(gamePane.startLocation);
    setActEnabled(true);
  }

  int getCellIndex() {
    return cellIndex;
  }

  private void moveToNextCell()
  {
    int tens = cellIndex / 10;
    int ones = cellIndex - tens * 10;
    if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
    {
      if (ones == 0 && cellIndex > 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() + 1, getY()));
    }
    else     // Cells starting left 20, 40, .. 100
    {
      if (ones == 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() - 1, getY()));
    }
    cellIndex++;
  }

  private void moveToPrevCell() {

    int tens = (cellIndex - 1) / 10;
    int ones = cellIndex - tens * 10;
    if (cellIndex == 1) {
      setLocation(new Location(getX() - 1, getY()));
      cellIndex = 0;
    } else {
      if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
      {
        if (ones == 1 && cellIndex > 0) {
          setLocation(new Location(getX(), getY() + 1));
        } else
          setLocation(new Location(getX() - 1, getY()));
      } else     // Cells starting left 20, 40, .. 100
      {
        if (ones == 1)
          setLocation(new Location(getX(), getY() + 1));
        else
          setLocation(new Location(getX() + 1, getY()));
      }
      cellIndex--;
    }

  }

  // Decide use toggle mode or not in auto mode
  public void reverseConnectionStrategy(){
    if (gamePane.getNp().isAuto()) {
      for (int i = 0; i < gamePane.getNumberOfPlayers(); i++) {
        if (gamePane.getPuppet() != gamePane.getPuppets().get(i)) {
          int totalSnakes = 0;
          int totalLadders = 0;
          for (int j = gamePane.getNp().getNumberOfDie(); j <= gamePane.getNp().getNumberOfDie() * 6; j++) {
            Connection temp = gamePane.getConnectionAt(gamePane.cellToLocation(gamePane.getPuppets().get(i).getCellIndex() + j));
            if (temp != null) {
              if (temp instanceof Snake) {
                totalSnakes++;
              } else {
                totalLadders++;
              }
            }
          }
          if (totalLadders >= totalSnakes) {
            gamePane.reverseAllConnections();
            if (gamePane.getNp().getToggleCheck().isChecked()) {
              gamePane.getNp().getToggleCheck().setChecked(false);
            } else {
              gamePane.getNp().getToggleCheck().setChecked(true);
            }
          }
        }
      }
    }
  }

  public void act()
  {
    if ((cellIndex / 10) % 2 == 0)
    {
      if (isHorzMirror())
        setHorzMirror(false);
    }
    else
    {
      if (!isHorzMirror())
        setHorzMirror(true);
    }

    // Animation: Move on connection
    if (currentCon != null ) {
      if( !(isNotLowest)  && (currentCon instanceof Snake)){
        System.out.println("isNotLowest");
        setActEnabled(false);
        gamePane.getNp().prepareRoll(cellIndex);
        currentCon = null;
        isNotLowest = true;
      }else{
        int x = gamePane.x(y, currentCon);
        setPixelLocation(new Point(x, y));
        y += dy;

        // Check end of connection
        if ((dy > 0 && (y - gamePane.toPoint(currentCon.locEnd).y) > 0)
                || (dy < 0 && (y - gamePane.toPoint(currentCon.locEnd).y) < 0)) {
          gamePane.setSimulationPeriod(100);
          setActEnabled(false);
          setLocation(currentCon.locEnd);
          cellIndex = currentCon.cellEnd;
          setLocationOffset(new Point(0, 0));
          currentCon = null;
          gamePane.getNp().prepareRoll(cellIndex);
        }
      }
      return;
    }

    // Normal movement
    if (nbSteps > 0 || nbSteps < 0)
    {
      if(nbSteps > 0) {
        gamePane.setSimulationPeriod(100);
        moveToNextCell();

        if (cellIndex == 100)  // Game over
        {
          setActEnabled(false);
          gamePane.getNp().prepareRoll(cellIndex);
          return;
        }

        nbSteps--;
      }

      else if(nbSteps < 0){
        moveToPrevCell();
        gamePane.switchToNextPuppet();
        nbSteps++;
      }

      if (nbSteps == 0)
      {
        // Check collide
        isCollide = false;
        for (int i = 0; i < gamePane.getNumberOfPlayers(); i++) {
          if (gamePane.getPuppet() != gamePane.getPuppets().get(i)) {
            if (gamePane.getPuppets().get(i).cellIndex > 0 && gamePane.getPuppet().cellIndex > 0) {
              if (gamePane.getPuppet().cellIndex == gamePane.getPuppets().get(i).cellIndex) {
                gamePane.getPuppets().get(i).setActEnabled(true);
                gamePane.getPuppets().get(i).go(-1);
                isCollide = true;
              }
            }
          }
        }

        // Check if on connection start
        if ((currentCon = gamePane.getConnectionAt(getLocation())) != null)
        {
          gamePane.setSimulationPeriod(30);
          y = gamePane.toPoint(currentCon.locStart).y;
          if (currentCon.locEnd.y > currentCon.locStart.y)
            dy = gamePane.animationStep;
          else
            dy = -gamePane.animationStep;
          if (currentCon instanceof Snake && isNotLowest)
          {
            gamePane.rollStatsTracker[gamePane.getCurrentPuppetIndex()][gamePane.getNp().getNumberOfDie()*6+1]+=1;
            gamePane.getNp().showStatus("Digesting...");
            gamePane.getNp().playSound(GGSound.MMM);
          }
          else if(currentCon instanceof Ladder)
          {
            gamePane.rollStatsTracker[gamePane.getCurrentPuppetIndex()][0]+=1;
            gamePane.getNp().showStatus("Climbing...");
            gamePane.getNp().playSound(GGSound.BOING);
          }
          if (!isCollide) {
            reverseConnectionStrategy();
          }
        }
        else {
          if (!isCollide) {
            reverseConnectionStrategy();
          }
          setActEnabled(false);
          gamePane.getNp().prepareRoll(cellIndex);
        }
      }
    }
  }

}
