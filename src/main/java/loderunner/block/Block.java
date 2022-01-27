package loderunner.block;

import loderunner.listener.Drawable2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * An abstract block
 *
 * @author timaeudg. Created Feb 2, 2012.
 */
public abstract class Block implements Drawable2D {

  protected int xPosition;
  protected int yPosition;
  protected int collideType;
  protected Shape mesh;
  protected Color blockColor;
  protected static final int BLOCK_SIZE = 20;
  protected BufferedImage pic;
  protected String type;

  /**
   * Initializes the blocks at their positions in the window and forms a
   * rectangle around them
   *
   * @param xPos
   * @param yPos
   */
  public Block(int xPos, int yPos) {
    this.xPosition = xPos * BLOCK_SIZE;
    this.yPosition = yPos * BLOCK_SIZE;
    this.mesh = new Rectangle2D.Double(this.xPosition, this.yPosition,
                                        BLOCK_SIZE + 2, BLOCK_SIZE + 2);
  }

  /**
   * Defines the draw method for the blocks
   *
   * @param g
   */
  @Override
  public void drawOn(Graphics2D g) {
    g.drawImage(this.pic,
                this.xPosition, this.yPosition, BLOCK_SIZE, BLOCK_SIZE,
                null);
  }

  /**
   * Returns the collide type of the block
   *
   * @return a number represent the collide type
   */
  public int getCollideType() {
    return this.collideType;
  }

  /**
   * Returns a 2D integer array for the location of the block in [x][y]
   *
   * @return a [x][y] integer array
   */
  public int[] getPosition() {
    int[] pos = new int[2];
    pos[0] = this.xPosition;
    pos[1] = this.yPosition;
    return pos;
  }

  /**
   * Sets the field called 'image' to the given value.
   *
   * @param blockType
   */
  public void setImage(String blockType) {
    final ClassLoader classLoader = getClass().getClassLoader();
    try {
      if (this.pic == null) {
        this.pic = ImageIO.read(classLoader.getResourceAsStream(String.format("images/blocks/%s.gif", blockType)));
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Sets the blocks collide type
   *
   * @param collide
   */
  protected void setCollideType(int collide) {
    this.collideType = collide;
  }

  /**
   * Returns a string for the type of block
   *
   * @return the name for the type of block
   */
  protected String getName() {
    return this.type;
  }

}
