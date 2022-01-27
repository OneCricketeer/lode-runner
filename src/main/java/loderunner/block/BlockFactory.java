package loderunner.block;

public final class BlockFactory {

  enum BLOCK_TYPE {
    SOLID('S'),
    LADDER('L'),
    TREASURE('T'),
    BRICK('B'),
    ROPE('R'),
    ESCAPE_LADDER('E'),
    GUARD('G'),
    HERO('H'),
    ARMOR('A');

    private final char letter;

    BLOCK_TYPE(char l) {
      this.letter = l;
    }

    public char getLetter() {
      return letter;
    }

    static BLOCK_TYPE valueOf(char l) {
      for (BLOCK_TYPE b : values()) {
        if (l == b.getLetter()) {
          return b;
        }
      }
      return null;
    }
  }

  /**
   * Returns the type of block at the position based on the character
   *
   * @param x
   * @param y
   * @param blockLetter
   * @return the block type based on the character at the position
   */
  public static Block getBlock(int x, int y, char blockLetter) {
    BLOCK_TYPE t = BLOCK_TYPE.valueOf(blockLetter);
    if (t == null) {
      // use underscores to prevent reading empty spaces of trailing new lines in files
      if (blockLetter == '_') return new Air(x, y);
      throw new IllegalArgumentException(
          String.format("Unexpected block letter: %s at (%d, %d)", blockLetter, x, y));
    }
    switch (t) {
      case SOLID:
        return new Solid(x, y);
      case LADDER:
      case ESCAPE_LADDER:
        return new Ladder(x, y, t == BLOCK_TYPE.ESCAPE_LADDER);
      case ROPE:
        return new Rope(x, y);
      case TREASURE:
        return new Treasure(x, y);
      case BRICK:
        return new Brick(x, y);
      case ARMOR:
        return new Armor(x, y);
      case GUARD:
      case HERO:
      default:
        return new Air(x, y);
    }

  }
}
