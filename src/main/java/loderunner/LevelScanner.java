package loderunner;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import loderunner.block.Block;
import loderunner.block.BlockFactory;
import loderunner.block.Hero;
import loderunner.block.LodeGuard;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Scans a level file and returns a list of blocks contained within the level
 *
 * @author moorejm. Created Feb 5, 2012.
 */
@Slf4j
public class LevelScanner {

  private final InputStream levelStream;
  @Getter
  private final java.util.List<Block> blocks = new ArrayList<>();

  private Hero hero;
  @Getter
  private final List<LodeGuard> guards = new ArrayList<>();

  public LevelScanner(String levelFileName) {
    final ClassLoader classLoader = getClass().getClassLoader();
    this.levelStream = classLoader.getResourceAsStream(String.format("levels/%s.lvl", levelFileName));
    if (levelStream == null) {
      log.error("Unable to find lvl file for level={}", levelFileName);
      System.exit(1);
    }

    loadLevel();
  }

  /**
   * Reads the level file and adds the appropriate blocks to an arraylist
   * based on the characters read from the file.
   */
  protected void loadLevel() {
    this.blocks.clear();

    this.hero = null;
    this.guards.clear();

    try (Scanner in = new Scanner(this.levelStream).useDelimiter("\\s*")) {
      int col = 0;
      int row = 0;
      while (in.hasNext()) {
        char c = in.next().charAt(0);
        if (col <= 40) {
          if (col == 40) {
            if (row < 30) {
              row++;
            }
            col = 0;
          }
        }

        if (c == 'H') {
          this.hero = new Hero(col * 20, row * 20 - 40);
        }
        if (c == 'G') {
          final LodeGuard lg = new LodeGuard(col * 20, row * 20);
          this.guards.add(lg);
        }
        this.blocks.add(BlockFactory.getBlock(col, row, c));
        col++;

      }
    }

    log.info("Loaded {} guards", this.guards.size());
  }

  public Optional<Hero> getHero() {
    return Optional.ofNullable(this.hero);
  }

}
