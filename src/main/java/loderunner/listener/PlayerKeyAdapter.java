package loderunner.listener;

import loderunner.block.Hero;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PlayerKeyAdapter extends KeyAdapter {

  public static final int DIG_LEFT = KeyEvent.VK_Z;
  public static final int DIG_RIGHT = KeyEvent.VK_X;

  private static final int NEXT_LEVEL = KeyEvent.VK_U;
  private static final int PREV_LEVEL = KeyEvent.VK_D;
  private static final int PAUSE = KeyEvent.VK_P;

  private final AtomicBoolean blocked;
  private final Hero player;

  public PlayerKeyAdapter(Hero player, AtomicBoolean blocked) {
    this.player = player;
    this.blocked = blocked;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == PAUSE) {
      onPause();
    }

    if (!this.blocked.get()) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
          if (player.canMoveD) {
            player.moveBy(0, 1);
          }
          break;
        case KeyEvent.VK_UP:
          if (player.canMoveU) {
            player.moveBy(0, -1);
          }
          break;
        case KeyEvent.VK_LEFT:
          player.isMovingRight = false;
          player.isMovingLeft = true;
          player.flip();
          if (player.canMoveL) {
            player.moveBy(-1, 0);
          }
          break;
        case KeyEvent.VK_RIGHT:
          player.isMovingRight = true;
          player.isMovingLeft = false;
          player.flip();
          if (player.canMoveR) {
            player.moveBy(1, 0);
          }
          break;
        case DIG_LEFT:
        case DIG_RIGHT:
          onDig(player, e.getKeyCode());
          break;
        case KeyEvent.VK_ESCAPE:
          System.exit(0);
          break;
        case NEXT_LEVEL:
          onLevelProgression(1);
          break;
        case PREV_LEVEL:
          onLevelProgression(-1);
          break;
        default:
          break;
      }
    }

  }

  public abstract void onPause();

  public abstract void onLevelProgression(int levelDelta);

  public abstract void onDig(Hero hero, int keyEvent);
}
