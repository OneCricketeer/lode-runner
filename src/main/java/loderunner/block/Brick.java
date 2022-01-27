package loderunner.block;

import java.awt.*;

/**
 * A brick that can be broken by the hero and fallen through when dug It also
 * respawns after a set time interval
 *
 * @author timaeudg. Created Feb 2, 2012.
 */
public class Brick extends Block {

  private boolean isDug;
  private static final int DIG_DELAY = 320;
  private int digCounter;

  /**
   * Sets the brick's x and y position
   *
   * @param xPos
   * @param yPos
   */
  public Brick(int xPos, int yPos) {
    super(xPos, yPos);
    super.setImage(getName());
    this.collideType = 1;
  }

  /**
   * Digs the brick that calls this method and makes it able to be fallen
   * through
   */
  public void dig() {
    this.isDug = true;
    this.collideType = 0;
  }

  @Override
  protected String getName() {
    return this.getClass().getSimpleName().toLowerCase();
  }

  /**
   * Resets the broken brick back to solid
   *
   * @param g
   */
  private void respawn(Graphics2D g) {
    this.isDug = false;
    this.collideType = 1;
    this.digCounter = 0;

    drawOn(g);
  }

  @Override
  public void drawOn(Graphics2D g) {
    if (!this.isDug) {
      g.drawImage(this.pic, super.xPosition, super.yPosition, null);
    } else {
      this.collideType = 0;
      this.digCounter++;
      if (this.digCounter == DIG_DELAY) {
        respawn(g);
      }
    }
  }

  /**
   * Whenever the brick respawns and is drawn over a hero or guard, then they
   * die
   *
   * @param g
   * @param hero
   * @param guards
   */
  public void drawOn(Graphics2D g, Hero hero, java.util.List<LodeGuard> guards) {
    if (!this.isDug) {
      g.drawImage(this.pic, super.xPosition, super.yPosition, null);
      // this.COLLIDE_TYPE = 1;
    } else {
      if (this.collideType != 1) {
        this.collideType = 0;
      }
      this.digCounter++;
      if (this.digCounter == DIG_DELAY) {
        for (LodeGuard guard : guards) {
          if (guard == null) {
            continue;
          }
          if (this.mesh.intersects(guard.image)) {
            guard.die();
          }
        }
        if (this.mesh.intersects(hero.getImage())) {
          hero.die();
        }
        respawn(g);
      }
    }
  }

}
