package loderunner;

import loderunner.block.Brick;
import loderunner.block.Hero;

import java.util.List;

public final class GameController {

  public static void digLeft(Hero hero, List<Brick> bricks) {
    int x = (int) (hero.getBottom().x / 20);
    int y = (int) (hero.getBottomL().y / 20) - 1;
    for (Brick brick : bricks) {
      if (brick.getPosition()[0] / 20 == x - 1
          && brick.getPosition()[1] / 20 == y + 1) {
        brick.dig();
      }
    }
  }

  public static void digRight(Hero hero, List<Brick> bricks) {
    int x = (int) (hero.getBottom().x / 20);
    int y = (int) (hero.getBottomR().y / 20) - 1;
    for (Brick brick : bricks) {
      if (brick.getPosition()[0] / 20 == x + 1
          && brick.getPosition()[1] / 20 == y + 1) {
        brick.dig();
      }
    }
  }
}
