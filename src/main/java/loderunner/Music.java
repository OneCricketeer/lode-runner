package loderunner;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Plays sounds
 *
 * @author moorejm. Created Feb 15, 2012.
 */
public class Music {

  private final int BUFFER_SIZE = 128000;
  private File soundFile;
  private AudioInputStream audioStream;
  private AudioFormat audioFormat;
  private SourceDataLine sourceLine;

  /**
   * @param filename the name of the file that is going to be played
   */
  public Music(String filename) {

    final ClassLoader classLoader = getClass().getClassLoader();
    try {
      this.soundFile = new File(classLoader.getResource("sounds/" + filename).toURI());
      this.audioStream = AudioSystem.getAudioInputStream(this.soundFile);
      this.audioFormat = this.audioStream.getFormat();
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, this.audioFormat);
      this.sourceLine = (SourceDataLine) AudioSystem.getLine(info);
      this.sourceLine.open(this.audioFormat);
    } catch (LineUnavailableException e) {
      e.printStackTrace();
      // System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      // System.exit(1);
    }

  }

  /**
   * Plays the sound file specified in the constructor
   */
  public void play() {
    this.sourceLine.start();

    int nBytesRead = 0;
    byte[] abData = new byte[this.BUFFER_SIZE];
    while (nBytesRead != -1) {
      try {
        nBytesRead = this.audioStream.read(abData, 0, abData.length);
        if (nBytesRead >= 0) {
          @SuppressWarnings("unused")
          int nBytesWritten = this.sourceLine.write(abData, 0, nBytesRead);
        }
      } catch (IOException e) {
        // e.printStackTrace();
      }

    }

    this.sourceLine.drain();
    this.sourceLine.close();

  }
}