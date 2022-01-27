package loderunner.listener;

@FunctionalInterface
public interface TreasureStateListener {
  void onTreasureStateChange(int amount);
}
