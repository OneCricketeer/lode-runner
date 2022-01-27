package loderunner.listener;

@FunctionalInterface
public interface LevelChangeListener {
  void onLevelChange(int level, int treasureLimit);
}
