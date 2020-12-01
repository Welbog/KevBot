package ca.welbog.kevbot.persist;
//package kevbot;

/* 
Copyright 2004

This file is part of KevBot.

Use KevBot at your own risk.
*/

/**
 * KarmaFile is an object designed to use a RAHL to store Strings and ints.
 * <p>
 * Its special methods allow it to store and retrieve ints based on searchable
 * Strings.
 * 
 * @version 1.0.0
 * @see Kevbot
 * @see DoubleFile
 */
public class KarmaFile extends DoubleFile {

  /**
   * Creates a new instance of KarmaFile
   * 
   * @param m
   *          The file to store the karma words.
   * @param r
   *          The file to store the karma.
   */
  public KarmaFile(String m, String r) {
    super(m, r);
  }

  /**
   * Get the karma associated with a String.
   * 
   * @param s
   *          The String to search for.
   * @return The karma associated with s, or 0 if it does not exist.
   */
  public synchronized int getKarma(String s) {
    if (s.equals("")) {
      return 0;
    }
    String r = super.getReply(s);
    if (r.equals("")) {
      return 0;
    }
    return Integer.parseInt(r);
  }

  /**
   * Get the closest String to a String in KarmaFile.
   * 
   * @param s
   *          The String to search for.
   * @return The closest String in KarmaFile to s.
   */
  public synchronized String getRealLine(String s) {
    return super.getMessage(s);
  }

  /**
   * Change the karma associated with a String by a given int.
   * 
   * @param s
   *          The String whose karma is to be changed.
   * @param x
   *          The amount the karma is to be changed.
   */
  public synchronized void changeKarma(String s, int x) {
    if (s.equals("")) {
      return;
    } // If we're given nothing, who cares?
    String old = super.getReply(s);
    if (old.equalsIgnoreCase("")) { // If it wasn't there before, make it.
    }
    else { // Otherwise add to the initial one.
      int i = Integer.parseInt(old);
      x += i;
    }
    super.add(s, "" + x);
  }
}
