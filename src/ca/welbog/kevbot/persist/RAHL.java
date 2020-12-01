package ca.welbog.kevbot.persist;

import java.util.Arrays;
import java.util.List;

//package kevbot;

/* 
Copyright 2004

This file is part of KevBot.

Use KevBot at your own risk.
*/

/**
 * RAHL is a sorted random access array list with binary search algorithms.
 * <p>
 * It's methods allow it to store strings in case-insensitive alphabetical
 * order, and it is equally good at reading randomized and presorted data.
 *
 * @version 1.0.0
 * @see Kevbot
 */
public class RAHL {
  /**
   * An array to store our String objects.
   */
  private String[] lines;
  /**
   * An int to store the size of the used space in lines.
   */
  private int size;

  /**
   * Constructor makes an empty RAHL.
   */
  public RAHL() {
    lines = new String[2]; // Initialize the array
    size = 0; // Make sure no one thinks it's holding anything.
  }

  /**
   * Add a String to the RAHL, store it in alphabetical order.
   * 
   * @param s
   *          The String to add.
   * @return The location of the String in the RAHL.
   */
  public synchronized int add(String s) {
    if (size != 0) {
      if (strupSearch(s, lines[size - 1]) > 0) { // If it goes at the end, let's
                                                 // not do anything special
        insertAt(size);
        lines[size - 1] = s;
        return size - 1;
      } // Otherwise, we need to run a binary placement algorithm.
      return putIn(s, 0, size);
    }
    else { // (size == 0), base case
      lines[0] = s;
      size = 1;
      return 0;
    }
  }

  /**
   * Get the number of Strings being stored.
   * 
   * @return The number of Strings being stored.
   */
  public synchronized int getSize() {
    return size;
  }

  /**
   * Do the important of finding the right place to put the String and place it
   * there. Implements a binary search.
   * 
   * @param s
   *          The String to add.
   * @param low
   *          The lower bound of where s needs to be added.
   * @param high
   *          The upper bound of where s needs to be added.
   */
  private int putIn(String s, int low, int high) {
    int guess = (int) Math.floor((low + high) / 2); // find the middle
    if (low > high) { // We've found a good spot!
      insertAt(low);
      lines[low] = s;
      return low;
    }
    if (guess >= size) { // We've found a good spot!
      insertAt(size);
      lines[size - 1] = s;
      return size - 1;
    }
    if (strupSearch(s, lines[guess]) == 0) { // It's already here.
      lines[guess] = s; // So replace the string, just in case.
      return guess;
    }
    if (strupSearch(s, lines[guess]) > 0) { // We haven't found a good spot.
      return putIn(s, guess + 1, high); // Continue in upper half.
    }
    else {
      return putIn(s, low, guess - 1); // Continue in lower half.
    }
  }

  /**
   * Helper method determines whether two strings are "the same."
   * 
   * @param s
   *          A String to compare.
   * @param t
   *          A String to compare.
   */
  private boolean matx0r(String s, String t) {
    return (strupSearch(s, t) == 0);
  }

  private String strup(String s) {
    String strup = new String(s);
    if (strup.equals("")) {
      return "";
    }
    while (new Character(strup.charAt(strup.length() - 1)).toString()
        .matches("[`~!@#$%^&*_=1|?/,. ]")) {
      try {
        strup = strup.substring(0, strup.length() - 1);
        if (strup.equals("")) {
          return "";
        }
      }
      catch (Exception e) {
        return "";
      }
    }
    return strup.trim();
  }

  private int strupSearch(String s, String t) {
    String ss = new String(s);
    String tt = new String(t);
    ss = strup(ss);
    tt = strup(tt);
    return ss.compareToIgnoreCase(tt);
  }

  /**
   * Check to see if a String is present. Implements a binary search.
   * 
   * @param s
   *          The String to check.
   * @param low
   *          The starting point.
   * @param high
   *          The ending point.
   * @return True if s.equalsIgnoreCase() a String contained within the RAHL.
   */
  public synchronized boolean exists(String s, int low, int high) {
    // s = s.trim();
    if (size == 0) {
      return false;
    }
    if (strupSearch(s, lines[size - 1]) > 0) { // If the value is bigger than
                                               // the last value, return false
                                               // immediately.
      return false;
    }
    if (size == 0) {
      return false;
    } // Base case
    int guess = (low + high) / 2; // Find the middle
    if (guess >= size) {
      return false;
    } // We've gone to far!
    if (low > high) { // You maniacs! You blew it up!
      return false;
    }
    if (matx0r(s, lines[guess])) {
      return true;
    } // w00t
    if (strupSearch(s, lines[guess]) > 0) { // Let's narrow the search.
      return exists(s, guess + 1, high); // Search in upper half.
    }
    else {
      return exists(s, low, guess - 1); // Search in lower half.
    }
  }

  /**
   * Get the index of a given String contained in the RAHL. For use with
   * RAHL.get(int i). Implements a binary search.
   * 
   * @param s
   *          The String to look for.
   * @param low
   *          The starting point.
   * @param high
   *          The ending point.
   * @return The index corresponding to the given String, or -1 if it doesn't
   *         match.
   */
  public synchronized int getIndex(String s, int low, int high) {
    // s = s.trim();
    if (size == 0) {
      return -1;
    } // Oh crap!
    int guess = (low + high) / 2; // Find the middle.
    if (guess >= size) {
      return -1;
    } // OH FUCK!
    if (low > high) { // SHIT!
      return -1;
    }
    if (matx0r(s, lines[guess])) {
      return guess;
    } // w00t
    if (strupSearch(s, lines[guess]) > 0) { // RECURSION IS RECURSION
      return getIndex(s, guess + 1, high); // Upper half
    }
    else {
      return getIndex(s, low, guess - 1); // Lower half
    }
  }

  /**
   * This function doesn't do anything right now. returns the closest String in
   * the RAHL that matches the String provided and return that. Implements a
   * binary search.
   * 
   * @param s
   *          The String to look for.
   * @param low
   *          The starting point.
   * @param high
   *          The ending point.
   * @return The closest String in the RAHL to s.
   */
  public synchronized String getRealString(String s, int low, int high) {
    if (size == 0) {
      return s;
    } // Oops.
    int guess = (low + high) / 2; // Find the middle.
    if (guess >= size) {
      return s;
    } // We overshot!
    // if (s.substring(0,
    // Math.min(lines[guess].length(),s.length())).equalsIgnoreCase(lines[guess]))
    // { return lines[guess]; }
    if (matx0r(s, lines[guess])) {
      return lines[guess];
    } // w00t.
    if (low > high) { // DAMN!
      return s;
    }
    if (strupSearch(s, lines[guess]) > 0) { // There's more work to do!
      return getRealString(s, guess + 1, high); // HIGH HALF!
    }
    else {
      return getRealString(s, low, guess - 1); // low half.
    }
  }

  /**
   * Delete a String from the RAHL. If it is not present, we don't care.
   * Implements a binary search.
   * 
   * @param s
   *          The String to de1337.
   * @param low
   *          The starting point.
   * @param high
   *          The !starting point.
   */
  public synchronized void delete(String s, int low, int high) {
    int guess = (low + high) / 2; // Let's make an educated guess.
    if (guess >= size) {
      return;
    } // DAMN
    if (matx0r(s, lines[guess])) { // w00t.
      collapse(guess); // Get rid of the element at guess.
      size--; // Lower the size.
      return; // Stop.
    } // End brace.
    if (strupSearch(s, lines[guess]) > 0) { // Keep going!
      delete(s, guess + 1, high); // HIGH!
    }
    else {
      delete(s, low, guess - 1); // LOW!
    }
  }

  /**
   * Get a String based on its location in the RAHL.
   * 
   * @param i
   *          The index of the String you want.
   * @return The String at index i.
   */
  public synchronized String get(int i) {
    return lines[i];
  }

  public synchronized List<String> getAll() {
    return Arrays.asList(lines);
  }

  /**
   * Helper method collapses the array around an index.
   * 
   * @param x
   *          The index to collapse around.
   */
  private void collapse(int x) {
    for (int i = x + 1; i < size; i++) {
      lines[i - 1] = lines[i];
    }
    lines[size - 1] = "";
  }

  /**
   * Helper method makes a free space at an index.
   * 
   * @param x
   *          The index at which a String will be inserted.
   */
  private void insertAt(int x) {
    // System.out.println("size: " + size + " lines.length: " + lines.length + "
    // x: " + x);
    if (size >= lines.length - 1) {
      extend();
    }
    // System.out.print("Before: ");
    // for (int i = 0; i < size; i++) {
    // System.out.print(lines[i] + ":");
    // }
    // System.out.println();
    for (int i = size; i > x; i--) {
      lines[i] = lines[i - 1];
    }
    // System.out.print("After: ");
    lines[x] = "";
    size++;
    // for (int i = 0; i < size; i++) {
    // System.out.print(lines[i] + ":");
    // }
    // System.out.println();
  }

  /**
   * Helper method makes the array itself bigger.
   */
  private void extend() {
    String[] temp = new String[lines.length * 2];
    for (int i = 0; i < size; i++) {
      temp[i] = lines[i];
    }
    lines = temp;
  }
}