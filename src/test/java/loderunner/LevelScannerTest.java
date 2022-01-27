package loderunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import loderunner.block.Hero;

import java.util.Optional;
import java.util.stream.Stream;

class LevelScannerTest {

  static Stream<Arguments> levels() {
    return Stream.of(
        Arguments.of(-1, 0),
        Arguments.of(0, 0),
        Arguments.of(1, 5),
        Arguments.of(2, 6)
    );
  }

  @ParameterizedTest
  @MethodSource(value = "levels")
  void test(int level, int guards) {
    // given
    LevelScanner lsc = new LevelScanner(String.format("level%d", level));
    // then
    final Optional<Hero> hero = lsc.getHero();
    assertTrue(hero.isPresent());
    assertEquals(guards, lsc.getGuards().size());
    assertEquals(30*40, lsc.getBlocks().size());
  }

}
