package loderunner.block;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A treasure block that can be "picked up"
 *
 * @author timaeudg. Created Feb 2, 2012.
 */

public class Treasure extends Block {

  private boolean isVisible;

  /**
   * Places a visible treasure block at (xPos, yPos)
   *
   * @param xPos
   * @param yPos
   */
  public Treasure(int xPos, int yPos) {
    super(xPos, yPos);
    this.setImage(getName());
    this.xPosition = xPos;
    this.yPosition = yPos;
    this.collideType = 5;
    this.isVisible = true;
  }

  @Override
  protected String getName() {
    return this.getClass().getSimpleName().toLowerCase();
  }

  @Override
  public void drawOn(Graphics2D g) {
    if (this.isVisible || this.collideType == 5) {
      g.drawImage(this.pic,
                  this.xPosition * BLOCK_SIZE, this.yPosition * BLOCK_SIZE,
                  BLOCK_SIZE, BLOCK_SIZE,
                  null);
    }

  }

  /**
   * "Picks up" a treasure by no longer drawing it and
   */
  public void pickup() {
    this.isVisible = false;
    this.collideType = 0;
  }

  @Override
  public int[] getPosition() {
    int[] pos = new int[2];
    pos[0] = this.xPosition;
    pos[1] = this.yPosition;
    return pos;
  }

  /**
   * Used to set the position of the treasure after a guard has been trapped
   *
   * @param x
   * @param y
   */
  public void setPosition(double x, double y) {
    this.xPosition = (int) x / BLOCK_SIZE;
    this.yPosition = (int) y / BLOCK_SIZE;
    this.mesh = new Rectangle2D.Double((int) x, (int) y,
                                       BLOCK_SIZE, BLOCK_SIZE);
  }

  /**
   * Returns whether the treasure is visible
   *
   * @return If the treasure is visible
   */
  public boolean isVisible() {
    return this.isVisible;
  }

  /**
   * Respawns the treasure after a guard has been trapped
   */
  public void respawnTreasure() {
    this.isVisible = true;
  }

}
