package loderunner.listener;

import loderunner.block.Hero;
import loderunner.block.LodeGuard;

public interface HeroEventListener {
  void onEnemyCollision(Hero h, LodeGuard g);
  void onLevelProgression(int levelDelta);
}
