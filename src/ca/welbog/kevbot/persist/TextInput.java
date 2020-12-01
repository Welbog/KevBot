package ca.welbog.kevbot.persist;
//package kevbot;

/* 
Copyright 2004

Use KevBot at your own risk.
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * TextInput is designed to read text from a file.
 * <p>
 * Its special methods allow it to read Strings from files.
 * 
 * @version 1.0.0
 * @see Kevbot
 * @see TextOutput
 */
@SuppressWarnings("all")
public class TextInput {
  private String filename = "";
  BufferedReader in;
  String currLine;
  StringTokenizer str;

  /**
   * Creates a new instance of TextInput
   * 
   * @param file
   *          The name of the file from which to read.
   */
  public TextInput(String file) throws IOException {
    in = new BufferedReader(new FileReader(file));
    currLine = in.readLine();
    if (currLine.equals("")) {
      throw new IOException();
    }
    str = new StringTokenizer(currLine);
  }

  /**
   * End of file is available.
   * 
   * @return true if there is nothing left to read from the file.
   */
  public synchronized boolean eofIsAvailable() throws IOException {
    in.mark(2);
    boolean omfg = (in.read() < 0) && (currLine == null);
    in.reset();
    return omfg;
  }

  /**
   * End of line is available.
   * 
   * @return true if there is nothing left to read from the current line.
   */
  public synchronized boolean eolIsAvailable() {
    return currLine.equals("");
  }

  /**
   * Get the next word from this object. A word is a String surrounded by
   * whitespace.
   * 
   * @return The next word in the stream <br>
   *         IOException if there's no word to read.
   */
  public synchronized String getWord() throws IOException {
    return this.readWord();
  }

  /**
   * Read the next word from this object. This method is the same as getWord().
   */
  public synchronized String readWord() throws IOException {
    if (str.hasMoreTokens()) {
      String temp = str.nextToken();
      currLine = "";
      while (str.hasMoreTokens()) {
        currLine = currLine + str.nextToken() + " ";
      }
      currLine = currLine.trim();
      str = new StringTokenizer(currLine);
      return temp;
    }
    else {
      throw new IOException();
    }
  }

  /**
   * Close the input stream.
   */
  public synchronized void close() throws IOException {
    in.close();
  }

  /**
   * Get the next line from this object. A line is a String starting from the
   * last word or line read, until the next newline character.
   * 
   * @return The next line in the stream <br>
   *         IOException if there's no line to read.
   */
  public synchronized String getLine() throws IOException {
    return this.readLine();
  }

  /**
   * Get the next line from this object. This method is the same as getLine().
   */
  public synchronized String readLine() throws IOException {
    String temp = currLine;
    currLine = in.readLine();
    if (currLine != null) {
      str = new StringTokenizer(currLine);
    }
    else {
      str = null;
    }
    return temp;
  }
}
