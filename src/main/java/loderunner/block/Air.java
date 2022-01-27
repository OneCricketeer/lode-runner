package loderunner.block;

import loderunner.LodeComponent;

/**
 * An empty "air" block through which the hero can fall
 *
 * @author timaeudg. Created Feb 2, 2012.
 */
public class Air extends Block {

  /**
   * Places an air block on the level of the same color as the components
   * background
   *
   * @param xPos
   * @param yPos
   */
  public Air(int xPos, int yPos) {
    super(xPos, yPos);
    this.blockColor = LodeComponent.BACKGROUND_COLOR;
    this.collideType = 0;
  }

}
