package loderunner.block;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A "rope" that the player is able to only travel sideways on as well as fall
 * down off of
 *
 * @author timaeudg. Created Feb 3, 2012.
 */
public class Rope extends Block {

  /**
   * Places a rope in the level
   *
   * @param xPos
   * @param yPos
   */
  public Rope(int xPos, int yPos) {
    super(xPos, yPos);
    this.setImage(getName());
    this.collideType = 3;
    this.mesh = new Rectangle2D.Double(this.xPosition, this.yPosition,
                                       Block.BLOCK_SIZE, Block.BLOCK_SIZE / 6.0);
  }

  @Override
  protected String getName() {
    return this.getClass().getSimpleName().toLowerCase();
  }

  @Override
  public void drawOn(Graphics2D g) {
    super.drawOn(g);
  }

}
