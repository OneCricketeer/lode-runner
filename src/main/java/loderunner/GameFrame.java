package loderunner;

import lombok.extern.slf4j.Slf4j;

import loderunner.listener.LevelChangeListener;
import loderunner.listener.TreasureStateListener;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import javax.swing.*;

/**
 * Makes the frame to play the game on and initializes it
 */
@Slf4j
public class GameFrame extends JFrame implements LevelChangeListener, TreasureStateListener {

  private static final int FPS = 24;
  private static final Music BGM = new Music("sonic0106.wav");

  private JPanel instructionsPanel;
  private final JLabel levelLabel = new JLabel();
  private final JLabel treasuresLabel = new JLabel("", SwingConstants.RIGHT);

  private int levelNum;
  private LodeComponent gameComponent;

  public static void main(String[] args) {
    int level = args.length == 1 ? Integer.parseInt(args[0]) : 0;
    final GameFrame game = new GameFrame(level);
    game.initGUI();
    // display and play music
    game.setVisible(true);
    BGM.play();
  }

  /**
   * Creates a default frame
   */
  public GameFrame() {
    this(0);
  }

  /**
   * Creates the frame starting at a given level
   *
   * @param level
   */
  public GameFrame(int level) {
    super();
    this.levelNum = level;
  }

  /**
   * Creates the frame for a game of lode runner and loads a given level
   */
  public void initGUI() {
    setTitle("Lode Runner");
    setLocation(200, 200);
    setResizable(false);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    initializeLevel();
    pack();
  }

  /**
   * Creates the specified level in the frame
   */
  private void initializeLevel() {
    try {
      this.gameComponent = new LodeComponent(FPS, this.levelNum, null, null);
      add(this.gameComponent);
    } catch (URISyntaxException e) {
      log.error("Unable to load main game component", e);
      System.exit(1);
    }

    this.instructionsPanel = createGameInfoPanel();
    add(this.instructionsPanel, BorderLayout.PAGE_END);

    this.gameComponent.setLevelChangeListener(this);
    this.gameComponent.setTreasureStateListener(this);

    onLevelChange(this.levelNum, this.gameComponent.getLevel().getTreasureLimit());
  }

  private JPanel createGameInfoPanel() {
    final JPanel p = new JPanel(new GridLayout(1, 3));
    p.setBackground(Color.black);

    JLabel instructionsLabel = new JLabel("(Arrows) Move (P) Pause (Z/X) Dig (Esc) Exit",
                                          SwingConstants.CENTER);

    Stream.of(this.levelLabel, instructionsLabel, this.treasuresLabel).forEach(l -> {
      l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
      l.setForeground(Color.WHITE);
      p.add(l);
    });

    return p;
  }


  /**
   * Sets the Level label to indicate the level the player is in
   *
   * @param levelNum the level the player is currently playing
   */
  public void setLevelLabelValue(int levelNum) {
    this.levelLabel.setText("Level: " + levelNum);
  }

  /**
   * Sets the Treasures label with the current amount and the limit of the level
   *
   * @param acquired
   * @param limit
   */
  public void setTreasureLabelValues(int acquired, Integer limit) {
    int currentLimit = this.gameComponent.getLevel().getTreasureLimit();
    this.treasuresLabel
        .setText(String.format("Treasures: %d / %d", acquired, limit == null ? currentLimit : limit));
  }

  @Override
  public void onLevelChange(int level, int treasureLimit) {
    log.info("Changing to level={} with treasure-limit={}", level, treasureLimit);
    this.levelNum = level;
    setLevelLabelValue(level);
    setTreasureLabelValues(0, treasureLimit);
    validate();
  }

  @Override
  public void onTreasureStateChange(int amount) {
    setTreasureLabelValues(amount, null);
  }
}
