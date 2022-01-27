package loderunner.block;

import lombok.extern.slf4j.Slf4j;

import loderunner.Level;
import loderunner.LodeComponent;
import loderunner.listener.HeroEventListener;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

/**
 * A guard that can pick up treaure and attempts to stop the hero
 *
 * @author timaeudg. Created Feb 3, 2012.
 */
@Slf4j
public class LodeGuard extends Block implements Person {

  private static final long DELAY_MS = 12;
  private static final double STEP_SIZE = 0.5;
  private static final int SIZE = 20;

  private boolean hasTreasure = false;

  private LodeComponent c;

  public Rectangle image = new Rectangle();
  private double x;
  private double y;

  public AtomicBoolean useless = new AtomicBoolean(false);

  public boolean canMoveL;
  public boolean canMoveU;
  public boolean canMoveD;
  public boolean canMoveR;
  private boolean canFall = true;
  private double velY;

  private static final int AIR = 0;
  private static final int BRICK = 1;
  private static final int LADDER = 2;
  private static final int ROPE = 3;
  private static final int TREASURE = 5;
  private static final int ARMOR = 9;

  private HeroEventListener heroEventListener;
  private Rectangle heroBoundary;

  /**
   * Creates a guard at (xPos, yPos)
   *
   * @param xPos
   * @param yPos
   */
  public LodeGuard(int xPos, int yPos) {
    super(xPos, yPos);
    setImage("standing");
    this.x = xPos - SIZE / 2;
    this.y = yPos - SIZE / 2;
    this.blockColor = Color.BLUE;
    this.velY = 0;
  }

  @Override
  public void setGraphicsComponent(LodeComponent c) {
    this.c = c;
  }

  @Override
  public void setThreadRunningState(boolean b) {
    this.useless.set(!b);
  }

  public void setHeroEventListener(Rectangle heroBoundary, HeroEventListener heroEventListener) {
    this.heroBoundary = heroBoundary;
    this.heroEventListener = heroEventListener;
  }

  public boolean hasTreasure() {
    return hasTreasure;
  }

  @Override
  public void run() {
    while (!this.useless.get()) {
      try {
        if (!this.c.getPaused().get()) {
          updatePosition();
          updateCollsion();
        }
        Thread.sleep(DELAY_MS);
      } catch (InterruptedException e) {
        log.warn("Interrupting guard thread", e);
        break;
      }
    }
  }


  protected Map<String, BufferedImage> images = new HashMap<>();

  @Override
  public void setImage(String blockType) {
    ClassLoader classLoader = getClass().getClassLoader();
    try {
      String key = blockType;
      if (!this.images.containsKey(key)) {
        this.images.put(key, ImageIO.read(classLoader.getResourceAsStream(String.format("images/guard/%s.gif", blockType))));
      }
      this.pic = this.images.get(key);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public void drawOn(Graphics2D g) {
    this.image.setFrame(this.x, this.y, SIZE, SIZE);
    g.drawImage(this.pic, (int) this.x, (int) this.y, 20, 20, null);
  }

  @Override
  public void moveBy(int dx, int dy) {
    if (!(this.image.getMaxX() >= this.c.getWidth()) || (dx < 0 || dy != 0)) {
      if (!(this.image.getMaxY() >= this.c.getHeight())
          || (dy < 0 || dx != 0)) {
        if (!(this.image.getMinX() <= 0) || (dx > 0 || dy != 0)) {
          if (!(this.image.getMinY() <= 0) || (dy > 0 || dx != 0)) {
            // collect treasure
            final Level level = this.c.getLevel();
            for (Treasure gold : level.getTreasure()) {
              if (gold.mesh.intersects(this.image)) {
                if (!this.hasTreasure && gold.isVisible()) {
                  this.hasTreasure = true;
                  gold.pickup();
                  break;
                }
              }
            }
            // run into poisonous armor
            for (Armor armor : level.getArmor()) {
              if (armor.mesh.intersects(this.image) && armor.isVisible()) {
                die();
                level.spawnTreasure(this.image.getMinX(), this.image.getCenterY());
                break;
              }
            }
          }
        }
      }
    }
    move(this.STEP_SIZE * dx, this.STEP_SIZE * dy);
  }

  private void move(double dx, double dy) {
    this.x += dx;
    this.y += dy;
  }

  private void fall() {
    if (this.canFall) {
      this.velY = 0.4;
      this.y += this.velY;
    }
    double height = this.c.getHeight();

    if (this.getBottom().y >= height) {
      this.y = height - SIZE;
      this.canMoveD = false;
    }

    // Check for spawned bricks while falling
    for (Brick brick : this.c.getLevel().getBricks()) {
      if (brick.mesh.intersects(new Rectangle(this.image.x,
                                              this.image.y, BLOCK_SIZE - 2, BLOCK_SIZE - 2))
          && brick.getCollideType() == 0) {

        this.y = ((Rectangle2D) brick.mesh).getY();
        this.x = ((Rectangle2D) brick.mesh).getX();
        this.canMoveD = false;
        this.useless.set(true);
        brick.setCollideType(1);
        this.canFall = false;
        if (this.hasTreasure) {
          this.c.getLevel().spawnTreasure(this.image.getMinX(), this.image.getCenterY());
          this.hasTreasure = false;
        }
      }
    }

  }

  /**
   * Moves the guard based on the location of the hero
   */
  public void updatePosition() {
    if (bottomCollidesWith() == LADDER
        && this.y - SIZE < this.c.getLevel().getPlayer().getLocation().y) {
      if (this.canMoveD) {
        this.moveBy(0, 1);
      }
    } else if (topCollidesWith() == LADDER
               || (bottomCollidesWith() == LADDER && topCollidesWith() == AIR)
                  && this.y - SIZE > this.c.getLevel().getPlayer().getLocation().y) {
      if (this.canMoveU) {
        this.moveBy(0, -1);
      }
    }

    if (rightCollidesWith() == ROPE
        && this.x < this.c.getLevel().getPlayer().getLocation().x) {
      if (this.canMoveR) {
        this.moveBy(1, 0);
      }
    }
    if (leftCollidesWith() == ROPE
        && this.x > this.c.getLevel().getPlayer().getLocation().x) {
      if (this.canMoveL) {
        this.moveBy(1, 0);
      }
    }

    if (topCollidesWith() != LADDER || bottomCollidesWith() != LADDER) {
      if (this.x == this.c.getLevel().getPlayer().getLocation().x) {
        this.moveBy(0, 0);

      } else if (this.x < this.c.getLevel().getPlayer().getLocation().x) {
        if (this.canMoveR) {
          this.moveBy(1, 0);
        }
      } else {
        if (this.canMoveL) {
          this.moveBy(-1, 0);
        }
      }
    }

    if ((topCollidesWith() == ROPE)
        && this.y - SIZE < this.c.getLevel().getPlayer().getLocation().y) {
      if (this.canMoveD) {
        this.moveBy(0, 1);
      }
    }

    if (!this.useless.get()) {
      this.killPlayer();
    }

    if ((this.bottomCollidesWith() == AIR && this.topCollidesWith() == AIR)
        || bottomCollidesWith() == ROPE || topCollidesWith() == BRICK) {
      this.fall();
    }
  }

  private void updateCollsion() {

    if (topCollidesWith() == AIR && bottomCollidesWith() == BRICK) {
      this.canMoveU = false;
    }

    if (topCollidesWith() == ROPE && leftCollidesWith() == ROPE
        && rightCollidesWith() == ROPE) {
      this.canMoveU = false;
      this.canMoveD = true;
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
      this.canMoveU = true;
      this.canMoveL = true;
      this.canMoveR = true;
      this.canMoveD = true;
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
      // setImage("standing");
    } else if (bottomCollidesWith() == LADDER) {
      // this.setImage("standing");
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
      // this.setImage("hanging");
    }

    if (topCollidesWith() == AIR && bottomCollidesWith() == AIR
        || bottomCollidesWith() == ROPE) {
      this.canMoveL = false;
      this.canMoveR = false;
    }

  }

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

  private Point2D.Double getBottom() {
    double y = this.image.getMaxY();
    double x = this.image.getCenterX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getBottomR() {
    double y = this.image.getMaxY();
    double x = this.image.getMaxX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getBottomRU() {
    double y = this.image.getMaxY() - this.image.height / 4;
    double x = this.image.getMaxX();
    return new Point2D.Double(x, y);
  }

  private Point2D.Double getBottomL() {
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

  @Override
  public void die() {
    int xspawn = (int) (Math.random() * this.c.getWidth());
    this.x = xspawn;
    this.y = 0;
    this.hasTreasure = false;
    this.canFall = true;
    this.useless.set(false);
  }

  /**
   * If the guard touches the hero, then the hero dies
   */
  public void killPlayer() {
    Rectangle hitbox = new Rectangle(this.image.x - 2, this.image.y - 2,
                                     this.image.width - 2, this.image.height - 2);
    if (hitbox.intersects(this.heroBoundary) && !this.useless.get()) {
      heroEventListener.onEnemyCollision(null, this);
    }
  }
}
