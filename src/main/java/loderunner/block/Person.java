package loderunner.block;

import loderunner.LodeComponent;
import loderunner.listener.Drawable2D;

/**
 * A person interface
 *
 * @author moorejm. Created Feb 11, 2012.
 */
public interface Person extends Runnable, Drawable2D {

  /**
   * Move the person dx and dy in the respective direction
   *
   * @param dx
   * @param dy
   */
  void moveBy(int dx, int dy);

  /**
   * Kill the person
   */
  void die();

  /**
   * Set the graphics component to interact with.
   * TODO: Design this better.
   */
  void setGraphicsComponent(LodeComponent c);

  void setThreadRunningState(boolean b);
}
