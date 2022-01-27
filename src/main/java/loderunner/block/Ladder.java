package loderunner.block;

import java.awt.*;

/**
 * A ladder block
 *
 * @author timaeudg. Created Feb 2, 2012.
 */
public class Ladder extends Block {

  private final boolean isEscapeLadder;
  private boolean extended = true;

  /**
   * Places a ladder block at x, y and sets it to a normal ladder or a hidden
   * escape ladder
   *
   * @param xPos
   * @param yPos
   * @param isEscape
   */
  public Ladder(int xPos, int yPos, boolean isEscape) {
    super(xPos, yPos);
    this.setImage(getName());
    this.isEscapeLadder = isEscape;
    if (isEscape) {
      this.collideType = 0;
      this.extended = false;
    } else {
      this.collideType = 2;
    }
  }

  @Override
  protected String getName() {
    return this.getClass().getSimpleName().toLowerCase();
  }

  /**
   * Default normal ladder constructor
   *
   * @param xPos
   * @param yPos
   */
  public Ladder(int xPos, int yPos) {
    super(xPos, yPos);
    this.setImage(getName());
    this.isEscapeLadder = false;
    this.collideType = 2;
  }

  @Override
  public void drawOn(Graphics2D g) {
    if (!this.isEscapeLadder || this.extended) {
      g.drawImage(this.pic, super.xPosition, super.yPosition, null);
    }
  }

  /**
   * Extends & makes the escape ladder visible
   */
  public void extend() {
    if (!isExitLadder()) return;
    this.extended = true;
    setCollideType(2);
  }

  /**
   * Returns true if the ladder is the escape ladder
   *
   * @return true if this ladder is the escape ladder
   */
  public boolean isExitLadder() {
    return this.isEscapeLadder;
  }

}
