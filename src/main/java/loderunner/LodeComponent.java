package loderunner;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import loderunner.block.Brick;
import loderunner.block.Hero;
import loderunner.block.LodeGuard;
import loderunner.listener.HeroEventListener;
import loderunner.listener.LevelChangeListener;
import loderunner.listener.PlayerKeyAdapter;
import loderunner.listener.TreasureStateListener;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

/**
 * Creates a component that contains a level with blocks and a hero
 */
@Slf4j
public class LodeComponent extends JComponent implements HeroEventListener, TreasureStateListener {

  private static final Dimension SIZE = new Dimension(800, 600);
  /**
   * Sets the color for the air blocks
   */
  public static final Color BACKGROUND_COLOR = Color.BLACK;

  @Getter
  private final AtomicBoolean paused = new AtomicBoolean(false);
  private final Repainter painter;

  private transient Thread heroThread;

  private transient Level levelToLode;

  private final java.util.List<Thread> guardThreads = new ArrayList<>();

  @Setter
  private transient TreasureStateListener treasureStateListener;
  @Setter
  private transient LevelChangeListener levelChangeListener;

  private transient KeyListener keyListener;

  /**
   * Creates the loderunner game running at a given fps
   *
   * @param fps       initial refresh rate in frames per second
   * @throws URISyntaxException
   */
  public LodeComponent(int fps, int levelNum, LevelChangeListener levelChangeListener, TreasureStateListener treasureStateListener) throws URISyntaxException {
    setPreferredSize(SIZE);
    setBackground(BACKGROUND_COLOR);

    this.levelChangeListener = levelChangeListener;
    this.treasureStateListener = treasureStateListener;

    goToLevel(levelNum);

    // Creates thread to update animation
    painter = new Repainter(this, fps);
    Thread repainterThread = new Thread(painter);
    repainterThread.start();

    // Setup key bindings
    setFocusable(true);
  }

  private KeyListener getPlayerKeyAdapter(final Level level) {
    return new PlayerKeyAdapter(level.getPlayer(), this.paused) {
      @Override
      public void onPause() {
        final boolean isPaused = LodeComponent.this.paused.get();
        log.info("keyEvent=pause ; paused={}", isPaused);
        if (isPaused) {
          log.info("Resuming");
          LodeComponent.this.paused.set(false);
          LodeComponent.this.painter.setPaused(false);
        } else {
          log.info("Pausing");
          LodeComponent.this.paused.set(true);
          LodeComponent.this.painter.setPaused(true);
        }
      }

      @Override
      public void onLevelProgression(int levelDelta) {
        LodeComponent.this.onLevelProgression(levelDelta);
      }

      @Override
      public void onDig(Hero hero, int keyEvent) {
        final List<Brick> bricks = level.getBricks();
        if (keyEvent == PlayerKeyAdapter.DIG_LEFT) {
          GameController.digLeft(hero, bricks);
        } else if (keyEvent == PlayerKeyAdapter.DIG_RIGHT) {
          GameController.digRight(hero, bricks);
        }
      }
    };
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (this.paused.get()) {
      return;
    }
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;

    Rectangle2D background = new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight());
    g2.setColor(BACKGROUND_COLOR);
    g2.fill(background);

    getLevel().drawOn(g2);
    for (LodeGuard guard : getLevel().getGuards()) {
      guard.drawOn(g2);
    }
    getLevel().getPlayer().drawOn(g2);
  }

  /**
   * Places the hero in the level
   *
   * @param hero
   */
  public void startHeroThread(Hero hero) {
    hero.setGraphicsComponent(this);
    if (this.heroThread != null) {
      hero.setThreadRunningState(false);
      this.heroThread.interrupt();
    }
    this.heroThread = new Thread(hero);
    this.heroThread.start();
  }

  /**
   * Creates guards in the level and starts a thread for each
   *
   * @param hero
   * @param guards
   */
  public void startGuardThreads(final Hero hero, java.util.List<LodeGuard> guards) {
    stopGuards();
    if (guards == null) {
      return;
    }
    for (LodeGuard guard : guards) {
      guard.setGraphicsComponent(this);
      guard.setThreadRunningState(true);
      guard.setHeroEventListener(hero.getImage(), new HeroEventListener() {
        @Override
        public void onEnemyCollision(Hero h, LodeGuard g) {
          log.info("Collision with hero(invincible={}) and guard(hasTreasure={})", hero.isInvincible(), g.hasTreasure());
          if (hero.isInvincible()) {
            if (g.hasTreasure()) {
              int tc = hero.getTreasureCount() + 1;
              hero.setTreasureCount(tc);
              onTreasureStateChange(tc);
            }
            g.die();
          } else {
            hero.die();
          }
        }

        @Override
        public void onLevelProgression(int levelDelta) {
          // guards do not progress the level
          // guard collision is handled by the Hero's own listener
        }
      });
      Thread t = new Thread(guard);
      this.guardThreads.add(t);
      t.start();
    }
  }

  /**
   * Stops all the thread for the guards when the player dies/goes to a new
   * level
   *
   * @since The program became really slow
   */
  public void stopGuards() {
    for (LodeGuard guard : getLevel().getGuards()) {
      guard.setThreadRunningState(false);
    }

    for (Thread guardThread : this.guardThreads) {
      guardThread.interrupt();
    }
  }

  @Override
  public void onEnemyCollision(Hero h, LodeGuard g) {
    log.info("Collided with enemy. Back to the basement.");
    goToLevel(-1);
  }

  @Override
  public void onLevelProgression(int levelDelta) {
    int newLevel = getLevel().getLevelNumber() + levelDelta;
    log.info("Progressing to level={}", newLevel);
    goToLevel(newLevel);
  }

  private void goToLevel(int newLevel) {
    try {
      Level l = new Level(newLevel, this, this);
      setLevel(l);

      if (this.keyListener != null) {
        removeKeyListener(this.keyListener);
      }
      this.keyListener = getPlayerKeyAdapter(getLevel());
      addKeyListener(this.keyListener);
    } catch (URISyntaxException e) {
      log.error("Error loading level {}", newLevel, e);
      System.exit(1);
    }
  }

  @Override
  public void onTreasureStateChange(int amount) {
    log.info("Now have {} treasures", amount);
    if (amount >= this.levelToLode.getTreasureLimit()) {
      log.info("Got all treasures! Extending ladder.");
      this.levelToLode.extendLadder();
    }
    if (this.treasureStateListener != null) {
      log.info("Updating treasures");
      this.treasureStateListener.onTreasureStateChange(amount);
    }
  }

  private class Repainter implements Runnable {

    private final int fps;
    private final Component component;
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean painting = new AtomicBoolean(true);

    public Repainter(Component c, int fps) {
      this.component = c;
      this.fps = fps;
    }

    @Override
    public void run() {
      while (this.painting.get()) {
        try {
          Thread.sleep(1000 / this.fps);
        } catch (InterruptedException e) {
          e.printStackTrace();
          break;
        }
        if (!this.paused.get()) {
          this.component.repaint();
        }
      }
    }

    public void stop() {
      this.painting.set(false);
    }

    public void setPaused(boolean state) {
      this.paused.set(state);
    }

  }

  public Level getLevel() {
    return this.levelToLode;
  }

  private void setLevel(Level levelToLode) {
    this.levelToLode = levelToLode;

    startHeroThread(this.levelToLode.getPlayer());
    startGuardThreads(this.levelToLode.getPlayer(), getLevel().getGuards());

    if (levelChangeListener != null) {
      levelChangeListener.onLevelChange(levelToLode.getLevelNumber(), levelToLode.getTreasureLimit());
    }
  }
}
