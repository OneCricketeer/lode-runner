package loderunner;

import loderunner.block.Air;
import loderunner.block.Armor;
import loderunner.block.Block;
import loderunner.block.Brick;
import loderunner.block.Hero;
import loderunner.block.Ladder;
import loderunner.block.LodeGuard;
import loderunner.block.Rope;
import loderunner.block.Solid;
import loderunner.block.Treasure;
import loderunner.listener.Drawable2D;
import loderunner.listener.HeroEventListener;
import loderunner.listener.TreasureStateListener;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and draws a level. Also contains all the blocks.
 *
 * @author timaeudg. Created Feb 2, 2012.
 */
public class Level implements Drawable2D {

  private final int levelNumber;

  private final LevelScanner scanner;
  private final Hero player;

  private final java.util.List<Rope> ropes = new ArrayList<>();
  private final java.util.List<Ladder> ladders = new ArrayList<>();
  private final java.util.List<Brick> bricks = new ArrayList<>();
  private final java.util.List<Solid> solids = new ArrayList<>();
  private final java.util.List<Treasure> treasure = new ArrayList<>();
  private final java.util.List<Air> air = new ArrayList<>();
  private final java.util.List<Armor> armor = new ArrayList<>();

  /**
   * Takes a level number to load and draws it in the given component
   *
   * @param levelNumber
   */
  public Level(int levelNumber, TreasureStateListener treasureStateListener, HeroEventListener heroEventListener) throws URISyntaxException {
    this.levelNumber = levelNumber;
    this.scanner = new LevelScanner("level" + levelNumber);
    this.player = scanner.getHero().orElseThrow(() -> new IllegalStateException("Unable to load hero"));
    distributeBlocks(this.scanner.getBlocks());
    this.player.setTreasureStateListener(treasureStateListener);
    this.player.setHeroEventListener(heroEventListener);
  }

  /**
   * Draws all the blocks within the level
   *
   * @param g
   */
  @Override
  public void drawOn(Graphics2D g) {
    for (Block block : this.scanner.getBlocks()) {
      if (block instanceof Brick) {
        ((Brick) block)
            .drawOn(g, this.player, this.getGuards());
      } else {
        block.drawOn(g);
      }
    }
  }

  private void distributeBlocks(List<Block> blocks) {
    for (Block block : blocks) {
      if (block instanceof Ladder) {
        this.ladders.add((Ladder) block);
      } else if (block instanceof Rope) {
        this.ropes.add((Rope) block);
      } else if (block instanceof Brick) {
        this.bricks.add((Brick) block);
      } else if (block instanceof Solid) {
        this.solids.add((Solid) block);
      } else if (block instanceof Treasure) {
        this.treasure.add((Treasure) block);
      } else if (block instanceof Air) {
        this.air.add((Air) block);
      } else if (block instanceof Armor) {
        this.armor.add((Armor) block);
      }
    }
  }

  public Hero getPlayer() {
    return this.player;
  }

  /**
   * Returns the value of the field called 'guards'.
   *
   * @return Returns the guards.
   */
  public java.util.List<LodeGuard> getGuards() {
    return this.scanner.getGuards();
  }

  /**
   * Returns the value of the field called 'ropes'.
   *
   * @return Returns the ropes.
   */
  public java.util.List<Rope> getRopes() {
    return this.ropes;
  }

  /**
   * Returns the value of the field called 'ladders'.
   *
   * @return Returns the ladders.
   */
  public java.util.List<Ladder> getLadders() {
    return this.ladders;
  }

  /**
   * Returns the value of the field called 'bricks'.
   *
   * @return Returns the bricks.
   */
  public java.util.List<Brick> getBricks() {
    return this.bricks;
  }

  /**
   * Returns the value of the field called 'solids'.
   *
   * @return Returns the solids.
   */
  public java.util.List<Solid> getSolids() {
    return this.solids;
  }

  /**
   * Returns the value of the field called 'treasure'.
   *
   * @return Returns the treasure.
   */
  public java.util.List<Treasure> getTreasure() {
    return this.treasure;
  }

  /**
   * Returns the number of treasures currently in the level
   *
   * @return Returns total number of treasures
   */
  public int getTreasureLimit() {
    return this.treasure.size();
  }

  public java.util.List<Armor> getArmor() {
    return this.armor;
  }

  /**
   * Extends the escape ladder once all the treasures are collected
   */
  public void extendLadder() {
    for (Ladder ladder : getLadders()) {
      ladder.extend();
    }
  }

  /**
   * Spawns a treasure.
   * Used when there is a trapped guard that dies from a brick respawn.
   *
   * @param x
   * @param y
   */
  public void spawnTreasure(double x, double y) {
    for (Treasure gold : this.treasure) {
      if (!gold.isVisible()) {
        gold.setPosition(x, y);
        gold.respawnTreasure();
        break;
      }
    }
  }

  public int getLevelNumber() {
    return levelNumber;
  }
}
