package loderunner.block;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import loderunner.LodeComponent;
import loderunner.listener.HeroEventListener;
import loderunner.listener.TreasureStateListener;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

/**
 * Creates a hero
 *
 * @author moorejm. Created Feb 5, 2012.
 */
@Slf4j
public class Hero extends Block implements Person {

  private static final long DELAY_MS = 12;
  private static final double STEP_SIZE = 5;
  private static final int SIZE = 20;

  @Getter
  @Setter
  private int treasureCount;
  @Getter
  private boolean isInvincible;

  @Getter
  private final Rectangle image = new Rectangle();
  private double x;
  private double y;

  // TODO: Decouple
  private LodeComponent c;

  public boolean canMoveL;
  public boolean canMoveU;
  public boolean canMoveD;
  public boolean canMoveR;
  private String direction = "right";

  public boolean isMovingRight = true;
  public boolean isMovingLeft;

  private static final int AIR = 0;
  private static final int BRICK = 1;
  private static final int LADDER = 2;
  private static final int ROPE = 3;
  private static final int TREASURE = 5;

  private AtomicBoolean running = new AtomicBoolean(false);

  @Setter
  private HeroEventListener heroEventListener;
  @Setter
  private TreasureStateListener treasureStateListener;

  /**
   * Creates a hero at x, y
   *
   * @param x
   * @param y
   */
  public Hero(int x, int y) {
    super(x, y);
    this.x = x - SIZE / 2;
    this.y = y - SIZE / 2;
    this.setImage("standing");
  }

  public void setGraphicsComponent(LodeComponent c) {
    this.c = c;
  }

  @Override
  public void setThreadRunningState(boolean b) {
    this.running.set(b);
  }

  /**
   * Sets the thread for this hero
   */
  @Override
  public void run() {
    while (!running.get()) {
      try {
        if (!this.c.getPaused().get()) {
          updateCollsion();
          checkForTreasure();
          checkForArmor();
          updatePosition();
        }
        Thread.sleep(DELAY_MS);
      } catch (InterruptedException e) {
        log.warn("Interrupted hero thread", e);
        break;
      }
    }
  }

  /**
   * Moves the hero by dx and dy
   */
  private void move(double dx, double dy) {
    this.x += dx;
    this.y += dy;
  }

  /**
   * Causes the hero to fall and stop once he reaches the bottom of the
   * component or a block with a collide type of 1
   */
  private void fall() {
    setImage("hanging");

    double velY = 1.5;

    this.y += velY;

    double height = this.c.getHeight();

    if (this.getBottom().y >= height) {
      this.y = height - 2 * SIZE;
      this.canMoveD = false;
    }
  }

  /**
   * Moves the rectangle by a given amount and checks for the bounds of the
   * frame
   *
   * @param dx
   * @param dy
   */
  @Override
  public void moveBy(int dx, int dy) {
    if (this.image.getMaxX() < this.c.getWidth() || (dx < 0 || dy != 0)) {
      if (this.image.getMaxY() < this.c.getHeight()
          || (dy < 0 || dx != 0)) {
        if (this.image.getMinX() > 0 || (dx > 0 || dy != 0)) {
          if (this.image.getMinY() > 0 || (dy > 0 || dx != 0)) {
            if (this.getTop().y < 3 * STEP_SIZE) {
              // If climbed to top of screen (via ladder)
              if (this.heroEventListener != null) {
                this.heroEventListener.onLevelProgression(1);
                return;
              }
            }
            move(STEP_SIZE * dx, STEP_SIZE * dy);
          }
        }
      }
    }
  }

  /**
   * Updates the position
   */
  public void updatePosition() {
    if ((this.bottomCollidesWith() == AIR && this.topCollidesWith() == AIR)
        || bottomCollidesWith() == ROPE || topCollidesWith() == BRICK) {
      this.fall();
    }
  }

  private void updateCollsion() {
    if (topCollidesWith() == ROPE && leftCollidesWith() == ROPE
        && rightCollidesWith() == ROPE) {
      this.canMoveU = false;
      this.canMoveD = true;
      setImage("hanging");
    }
    if (leftCollidesWith() == BRICK) {
      this.canMoveL = false;
    } else {
      this.canMoveL = true;
    }

    if (rightCollidesWith() == BRICK) {
      this.canMoveR = false;
    } else {
      this.canMoveR = true;
    }

    if (bottomCollidesWith() == AIR) {
      this.canMoveD = true;
    }

    if (topCollidesWith() == LADDER || bottomCollidesWith() == LADDER) {
      if (bottomCollidesWith() == BRICK) {
        this.canMoveD = false;
      } else {
        this.canMoveD = true;
      }
      this.canMoveU = true;
      this.canMoveL = true;
      this.canMoveR = true;

    } else {
      this.canMoveU = false;
    }

    if (rightCollidesWith() == BRICK) {
      this.canMoveR = false;
    }

    if (leftCollidesWith() == BRICK) {
      this.canMoveL = false;
    }

    // Override
    if (bottomCollidesWith() == BRICK) {
      this.canMoveD = false;
      setImage("standing");
    } else if (bottomCollidesWith() == LADDER) {
      setImage("climbing");
    } else {
      this.canMoveD = true;
    }

    if (topCollidesWith() == AIR && bottomCollidesWith() == AIR
        && leftCollidesWith() == AIR && rightCollidesWith() == AIR) {
      this.canMoveL = false;
      this.canMoveR = false;
      this.canMoveD = false;
    }

    if (topCollidesWith() == BRICK) {
      this.canMoveU = false;
      this.canMoveL = false;
      this.canMoveR = false;
    }

    if (topCollidesWith() == ROPE && leftCollidesWith() == ROPE
        && rightCollidesWith() == ROPE) {
      this.canMoveU = false;
      this.canMoveD = true;
    }

    if (topCollidesWith() == AIR && bottomCollidesWith() == AIR
        || bottomCollidesWith() == ROPE) {
      this.canMoveL = false;
      this.canMoveR = false;
    }

    if (topCollidesWith() == AIR && leftCollidesWith() == AIR
        && rightCollidesWith() == AIR && bottomCollidesWith() == BRICK) {
      setImage("standing");
    }

    if (topCollidesWith() == AIR && bottomCollidesWith() == AIR) {
      this.setImage("hanging");
    }

    if (topCollidesWith() == ROPE
        || (topCollidesWith() == ROPE && bottomCollidesWith() == BRICK)) {
      this.setImage("hanging");
    }

  }

  /**
   * Checks to see if the hero is colliding with a treasure
   */
  private void checkForTreasure() {
    for (Treasure gold : this.c.getLevel().getTreasure()) {
      if (gold.mesh.intersects(this.image) && gold.isVisible()) {
        log.info("Picking up a treasure!");
        gold.pickup();
        this.treasureCount++;
        this.treasureStateListener.onTreasureStateChange(this.treasureCount);
        // don't break; there could be multiple treasure blocks on the same space
      }
    }

  }

  private void checkForArmor() {
    for (Armor armor : this.c.getLevel().getArmor()) {
      if (armor.mesh.intersects(this.image)) {
        if (armor.isVisible()) {
          this.isInvincible = true;
        }
        armor.pickup();
        break;
      }
    }

  }

  /************* Graphics *************/
  protected Map<String, BufferedImage> images = new HashMap<>();

  @Override
  public void setImage(String blockType) {
    final ClassLoader classLoader = Hero.class.getClassLoader();
    try {
        String key = String.format("%s-%s", this.direction, blockType);
        if (!this.images.containsKey(key)) {
          this.images.put(key, ImageIO.read(classLoader.getResourceAsStream(String.format("images/sonic/%s/%s.gif", this.direction, blockType))));
        }
        this.pic = images.get(key);
    } catch (IOException exception) {
      log.error("Unable to load image for blockType={}", blockType, exception);
    }
  }

  /**
   * Flip the image of the hero when he switches direction
   */
  public void flip() {
    if (this.isMovingLeft) {
      this.direction = "left";
    } else if (this.isMovingRight) {
      this.direction = "right";
    }
  }

  @Override
  public void drawOn(Graphics2D g) {
    this.image.setFrame(this.x, this.y, SIZE + 5, 2 * SIZE);
    g.drawImage(this.pic, (int) this.x, (int) this.y, 25, 40, null);
  }

  /********** Collision Detection ***************/

  private Point2D.Double getTop() {
    double y = this.image.getMinY();
    double x = this.image.getCenterX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getTopL() {
    double y = this.image.getMinY();
    double x = this.image.getMinX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getTopLD() {
    double y = this.image.getMinY() + this.image.height / 4;
    double x = this.image.getMinX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getTopR() {
    double y = this.image.getMinY();
    double x = this.image.getMaxX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getTopRD() {
    double y = this.image.getMinY() + this.image.height / 4;
    double x = this.image.getMaxX();
    return new Point2D.Double(x, y);
  }

  public Point2D.Double getBottom() {
    double y = this.image.getMaxY();
    double x = this.image.getCenterX();
    return new Point2D.Double(x, y);
  }

  public Point2D.Double getBottomR() {
    double y = this.image.getMaxY();
    double x = this.image.getMaxX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getBottomRU() {
    double y = this.image.getMaxY() - this.image.height / 4;
    double x = this.image.getMaxX();
    return new Point2D.Double(x, y);
  }

  public Point2D.Double getBottomL() {
    double y = this.image.getMaxY();
    double x = this.image.getMinX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getBottomLU() {
    double y = this.image.getMaxY() - this.image.height / 4;
    double x = this.image.getMinX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getLeft() {
    double y = this.image.getCenterY();
    double x = this.image.getMinX() - this.STEP_SIZE;
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getRight() {
    double y = this.image.getCenterY();
    double x = this.image.getMaxX() + this.STEP_SIZE;
    return new Point2D.Double(x, y);
  }

  private int bottomCollidesWith() {
    for (Brick brick : this.c.getLevel().getBricks()) {
      if (brick.mesh.contains(this.getBottom())
          && (brick.mesh.contains(this.getBottomL()) || brick.mesh
          .contains(this.getBottomR()))) {
        return brick.getCollideType();
      }
    }

    for (Ladder ladder : this.c.getLevel().getLadders()) {
      if (ladder.mesh.contains(this.getBottom())) {
        return ladder.getCollideType();
      }
    }

    for (Solid solid : this.c.getLevel().getSolids()) {
      if (solid.mesh.contains(this.getBottom())) {
        return solid.getCollideType();
      }
    }

    for (Rope rope : this.c.getLevel().getRopes()) {
      if (rope.mesh.contains(this.getBottom())) {
        return rope.getCollideType();
      }
    }

    for (Treasure treasure : this.c.getLevel().getTreasure()) {
      if (treasure.mesh.contains(this.getBottom())
          && (treasure.mesh.contains(this.getBottomL()) || treasure.mesh
          .contains(this.getBottomR()))) {
        return treasure.getCollideType();
      }
    }

    return 0;
  }

  private int topCollidesWith() {
    for (Brick brick : this.c.getLevel().getBricks()) {
      if (brick.mesh.contains(this.getTop())
          && (brick.mesh.contains(this.getTopL()) || brick.mesh
          .contains(this.getTopR()))) {
        return brick.getCollideType();
      }
    }

    for (Ladder ladder : this.c.getLevel().getLadders()) {
      if (ladder.mesh.contains(this.getTop())) {
        return ladder.getCollideType();
      }
    }

    for (Solid solid : this.c.getLevel().getSolids()) {
      if (solid.mesh.contains(this.getTop())) {
        return solid.getCollideType();
      }
    }

    for (Rope rope : this.c.getLevel().getRopes()) {
      if (rope.mesh.contains(this.getTop())) {
        return rope.getCollideType();
      }
    }

    for (Treasure treasure : this.c.getLevel().getTreasure()) {
      if (treasure.mesh.contains(this.getTop())) {
        return treasure.getCollideType();
      }
    }

    return 0;
  }

  private int leftCollidesWith() {
    for (Brick brick : this.c.getLevel().getBricks()) {
      if (brick.mesh.contains(getBottomL())
          && brick.mesh.contains(getBottomLU())
          || (brick.mesh.contains(getBottomLU()) && brick.mesh
          .contains(getLeft()))
          || (brick.mesh.contains(getLeft()) && brick.mesh
          .contains(getTopLD()))
          || (brick.mesh.contains(getTopLD()) && brick.mesh
          .contains(getTopL()))) {
        return brick.getCollideType();
      }
    }

    for (Ladder ladder : this.c.getLevel().getLadders()) {
      if (ladder.mesh.contains(this.getLeft())) {
        return ladder.getCollideType();
      }
    }

    for (Solid solid : this.c.getLevel().getSolids()) {
      if (solid.mesh.contains(getBottomL())
          && solid.mesh.contains(getBottomLU())
          || (solid.mesh.contains(getBottomLU()) && solid.mesh
          .contains(getLeft()))
          || (solid.mesh.contains(getLeft()) && solid.mesh
          .contains(getTopLD()))
          || (solid.mesh.contains(getTopLD()) && solid.mesh
          .contains(getTopL()))) {
        return solid.getCollideType();
      }
    }

    for (Rope rope : this.c.getLevel().getRopes()) {
      if (rope.mesh.contains(this.getLeft())) {
        return rope.getCollideType();
      }
    }

    for (Treasure treasure : this.c.getLevel().getTreasure()) {
      if (treasure.mesh.contains(this.getLeft())
          || treasure.mesh.intersects(this.image.getBounds2D())) {
        return treasure.getCollideType();
      }
    }

    return 0;
  }

  private int rightCollidesWith() {
    for (Brick brick : this.c.getLevel().getBricks()) {
      if (brick.mesh.contains(getBottomR())
          && brick.mesh.contains(getBottomRU())
          || (brick.mesh.contains(getBottomRU()) && brick.mesh
          .contains(getRight()))
          || (brick.mesh.contains(getRight()) && brick.mesh
          .contains(getTopRD()))
          || (brick.mesh.contains(getTopRD()) && brick.mesh
          .contains(getTopR()))) {
        return brick.getCollideType();
      }
    }

    for (Ladder ladder : this.c.getLevel().getLadders()) {
      if (ladder.mesh.contains(this.getRight())) {
        return ladder.getCollideType();
      }
    }

    for (Solid solid : this.c.getLevel().getSolids()) {
      if (solid.mesh.contains(getBottomR())
          && solid.mesh.contains(getBottomRU())
          || (solid.mesh.contains(getBottomRU()) && solid.mesh
          .contains(getRight()))
          || (solid.mesh.contains(getRight()) && solid.mesh
          .contains(getTopRD()))
          || (solid.mesh.contains(getTopRD()) && solid.mesh
          .contains(getTopR()))) {
        return solid.getCollideType();
      }
    }

    for (Rope rope : this.c.getLevel().getRopes()) {
      if (rope.mesh.contains(this.getRight())) {
        return rope.getCollideType();
      }
    }

    for (Treasure treasure : this.c.getLevel().getTreasure()) {
      if (treasure.mesh.intersects(this.image)) {
        return treasure.getCollideType();
      }
    }

    return 0;
  }

  /**
   * Causes the hero to "die" by resetting all state
   */
  @Override
  public void die() {
    log.info("Oh, he dead.");
    this.treasureCount = 0;
    this.treasureStateListener.onTreasureStateChange(this.treasureCount);
    this.isInvincible = false;
    this.setThreadRunningState(false);
    // hero could die from a brick, too, but we don't care about the enemy, so passing null works
    heroEventListener.onEnemyCollision(this, null);
  }

  public Point2D.Double getLocation() {
    return new Point2D.Double(this.x, this.y);
  }
}
