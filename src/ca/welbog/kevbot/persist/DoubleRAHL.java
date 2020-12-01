package ca.welbog.kevbot.persist;
//package kevbot;

/* 
Copyright 2004

This file is part of KevBot.

Use KevBot at your own risk.
*/

/**
 * DoubleRAHL is essentially a hash table for Strings.
 * <p>
 * It stores a RAHL of Strings and uses the indices of the stored Strings to
 * index another array of Strings. The Strings stored in the RAHL are called
 * "searchable Strings" and are also called messages. The Strings stored in the
 * array are called "indexed Strings" and are also called replies.
 * <p>
 * DoubleRAHL's special functions allow KevBot to store and retrieve
 * message/reply pairs using binary search algorithms.
 * 
 * @version 1.0.0
 * @see Kevbot
 * @see RAHL
 */
public class DoubleRAHL {
  String[] replies; // The array of indexed Strings.
  int size; // The size of this array.
  RAHL thomas; // The RAHL of searchable Strings.
  // Author's note: This RAHL is called thomas for reasons beyond your mortal
  // understanding.

  /**
   * Constructor creates a new empty DoubleRAHL.
   */
  public DoubleRAHL() {
    replies = new String[2];
    thomas = new RAHL();
    size = 0;
  }

  /**
   * Add a message and a reply to the DoubleRAHL. This method implements a
   * binary search algorithm.
   * 
   * @param m
   *          The searchable String (message) to be added.
   * @param reply
   *          The indexed String (reply) to be added.
   */
  public synchronized void add(String m, String reply) {
    // System.err.println("DoubleRAHL::add("+m+"," +reply+ ");");
    if ((thomas.getSize() != 0) && (thomas.exists(m, 0, thomas.getSize()))) { // If
                                                                              // there's
                                                                              // something
                                                                              // there
                                                                              // and
                                                                              // m
                                                                              // exists.
      int x = thomas.add(m); // Add m to the RAHL
      // thomas.getIndex(m, 0, thomas.getSize()); // Find where m went.
      replies[x] = reply; // Stick reply in the appropriate place.
    }
    else { // Otherwise...
      int x = thomas.add(m); // Add m to the RAHL
      // int x = thomas.getIndex(m, 0, thomas.getSize()); // .tnew m erehw dniF
      insertAt(x); // Make room.
      replies[x] = reply; // Stick reply in the appropriate place.
    }
  }

  /**
   * Append a reply to a message.
   * 
   * @param m
   *          The searchable String (message) to be appended to.
   * @param reply
   *          The indexed String (reply) to be appended.
   */
  public synchronized void append(String m, String reply) {
    // Pre: m exists in the DoubleRAHL
    int x = thomas.getIndex(m, 0, thomas.getSize());
    replies[x] += " " + reply;
  }

  /**
   * Change the reply to a given message.
   * 
   * @param m
   *          The searchable String (message) to be changed.
   * @param r
   *          The indexed String (reply) to be added.
   * @return The posisition of m.
   */
  public synchronized int addOver(String m, String r) {
    // System.err.println("DoubleRAHL::addOver(\""+m+"\",\""+r+"\");");
    int loc = thomas.getIndex(m, 0, thomas.getSize());
    // System.err.print("DoubleRAHL::addOver::loc="+loc+", ");
    if (loc < 0) {
      loc = thomas.add(m);
      insertAt(loc);
    }
    else {
      thomas.add(m);
    }
    // System.err.println(""+ loc +";");
    replies[loc] = r;
    return loc;
  }

  /**
   * Get the number of elements in the DoubleRAHL.
   * 
   * @return The number of elements in the DoubleRAHL.
   */
  public synchronized int getSize() {
    return thomas.getSize();
  }

  /**
   * Find out if s is contained in the DoubleRAHL. This implements a binary
   * search algortihm.
   * 
   * @param s
   *          The searchable String (message) to search for.
   * @param low
   *          The starting index.
   * @param high
   *          The ending index.
   */
  public synchronized boolean exists(String s, int low, int high) {
    return thomas.exists(s, low, high);
  }

  /**
   * Find the index of a String in the DoubleRAHL. This implements a binary
   * search algortihm.
   * 
   * @param s
   *          The searchable String (message) to search for.
   * @param low
   *          The starting index.
   * @param high
   *          The ending index.
   */
  public synchronized int getIndex(String s, int low, int high) {
    return thomas.getIndex(s, low, high);
  }

  /**
   * Remove a String from the DoubleRAHL. This implements a binary search
   * algortihm.
   * 
   * @param s
   *          The searchable String (message) to search for.
   */
  public synchronized void delete(String s) {
    if (getSize() == 0) {
      return;
    } // If there's nothing there, we can't do anything.
    int x = thomas.getIndex(s, 0, thomas.getSize()); // Find where this thing
                                                     // is.
    if (x < 0) { // It's -1 if it doesn't exist.
    }
    else { // Delete the bastard.
      thomas.delete(s, 0, thomas.getSize());
      collapse(x);
      size--;
    }
  }

  /**
   * Get a reply by index.
   * 
   * @param i
   *          The index of the reply wanted.
   * @return The reply at index i.
   */
  public synchronized String getReply(int i) {
    return replies[i];
  }

  /**
   * Get a reply by message. Given a searchable String, return the corresponding
   * indexed String. This method implements a binary search alorithm.
   * 
   * @param m
   *          The searchable String (message) to look for.
   * @return The corresponding indexed String (reply) to m.
   */
  public synchronized String getReply(String m) {
    if (getSize() == 0) {
      return "";
    }
    int x = thomas.getIndex(m, 0, thomas.getSize());// THE MEEK!!! indeed /*NOT
                                                    // THE MEKE!!!*/
    if (x >= 0) {
      return replies[x];
    }
    return "";
  }

  /**
   * Get a message by index.
   * 
   * @param i
   *          The index of the message wanted.
   * @return The message at index i.
   */
  public synchronized String getMessage(int i) {
    return thomas.get(i);
  }

  /**
   * Find the closest searchable String in the DoubleRAHL.
   * 
   * @param s
   *          The searchable String to match.
   * @return The closest searchable String in the DoubleRAHL to s.
   */
  public synchronized String getRealMessage(String s) {
    return thomas.getRealString(s, 0, thomas.getSize());
  }

  /**
   * Helper method removes an element from the array of replies.
   * 
   * @param x
   *          The index that is to be destroyed.
   */
  private void collapse(int x) {
    for (int i = x + 1; i < size; i++) {
      replies[i - 1] = replies[i];
    }
    replies[size - 1] = "";
  }

  /**
   * Helper method does the opposite of collapse(int x): it opens a space at a
   * given index.
   * 
   * @param x
   *          The index into which you want to insert an element.
   */
  private void insertAt(int x) {
    // System.out.println("size: " + thomas.getSize() + " lines.length: " +
    // replies.length + " x: " + x);
    if (thomas.getSize() >= replies.length - 1) {
      extend();
    }
    // System.out.print("Before: ");
    // for (int i = 0; i < size; i++) {
    // System.out.print(lines[i] + ":");
    // }
    // System.out.println();
    for (int i = size; i > x; i--) {
      replies[i] = replies[i - 1];
    }
    // System.out.print("After: ");
    replies[x] = "";
    size++;
    // for (int i = 0; i < size; i++) {
    // System.out.print(lines[i] + ":");
    // }
    // System.out.println();
  }

  /**
   * Double the length of the replies array.
   */
  private void extend() {
    String[] temp = new String[replies.length * 2];
    for (int i = 0; i < thomas.getSize() - 1; i++) {
      temp[i] = replies[i];
    }
    replies = temp;
  }
}
