package loderunner.block;

import java.awt.*;

/**
 * Holy armor that kills the guards if touched by them.
 * Grants invincibility to the hero when put on.
 *
 * @author moorejm.
 * Created Feb 21, 2012.
 */
public class Armor extends Block {

  private boolean isVisible = true;

  public Armor(int x, int y) {
    super(x, y);
    this.setImage(getName());
    this.collideType = 9;
  }

  @Override
  protected String getName() {
    return this.getClass().getSimpleName().toLowerCase();
  }


  @Override
  public void drawOn(Graphics2D g) {
    if (this.isVisible) {
      g.drawImage(this.pic, super.xPosition, super.yPosition, null);
//      g.fill(this.block);
    }

  }

  public void pickup() {
    this.isVisible = false;
    this.collideType = 0;
  }

  public boolean isVisible() {
    return this.isVisible;
  }

}
