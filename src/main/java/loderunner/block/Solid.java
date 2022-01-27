package loderunner.block;

import java.awt.*;

/**
 * A solid brick that cannot be dug
 *
 * @author timaeudg. Created Feb 2, 2012.
 */
public class Solid extends Block {

  /**
   * Places a solid block at (xPos, yPos)
   *
   * @param xPos
   * @param yPos
   */
  public Solid(int xPos, int yPos) {
    super(xPos, yPos);
    setImage(getName());
    this.collideType = 1;
  }

  @Override
  protected String getName() {
    return this.getClass().getSimpleName().toLowerCase();
  }

  @Override
  public void drawOn(Graphics2D g) {
    g.drawImage(this.pic, super.xPosition, super.yPosition, null);
  }

}
