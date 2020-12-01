package ca.welbog.kevbot.persist;

import java.util.LinkedList;
import java.util.List;

//package kevbot;
/* 
Copyright 2004

This file is part of KevBot.

Use KevBot at your own risk.
*/

//import becker.io.*;

/**
 * SingleFile is an object that handles file IO using a RAHL object.
 * <p>
 * SingleFile stores an alphabetically case-insensitive list of Strings and can
 * read/write this list to file. It also acts as the interface between RAHL and
 * KevBot, for queries/adds/deletes.
 *
 * @version 1.0.0
 * @see Kevbot
 * @see RAHL
 */
public class SingleFile {
  private String filename; // The file to read/write.
  private RAHL lines; // The RAHL that stores the data.

  /**
   * Constructor creates a File for reading and writing a given file.
   * 
   * @param f
   *          The filename to be edited.
   */
  public SingleFile(String f) {
    filename = f; // If you don't know what this does...
    lines = new RAHL(); // LEMONS!
    fillLines(); // Read the file or create it if it does not exist.
  }

  /**
   * Read the file or create it if it does not exist.
   */
  private void fillLines() {
    try { // The file exists.
      TextInput in = new TextInput(filename); // Open the IO stream.
      while (!in.eofIsAvailable()) { // Continue until the file is all read.
        lines.add(in.getLine()); // Read in the line and store it in the RAHL.
      }
      in.close(); // Close the IO steam.
    }
    catch (Exception e) { // The file !exists.
      try {
        TextOutput out = new TextOutput(filename); // Open the IO stream,
                                                   // creating the file.
        out.close(); // Close the IO steam.
      }
      catch (Exception p) {
        return;
      }
    }
  }

  /**
   * Close the File, write it to file.
   */
  public synchronized void close() {
    try {
      TextOutput out = new TextOutput(filename); // Open the stream!
      int i = 0;
      while (i < lines.getSize()) { // Write it all!
        out.println(lines.get(i));
        i++;
      }
      out.close(); // Close the stream!
    }
    catch (Exception e) {
      return;
    }
  }

  /**
   * Write the File to file.
   */
  public synchronized void write() {
    try {
      TextOutput out = new TextOutput(filename); // Open the stream!
      int i = 0;
      while (i < lines.getSize()) { // Write it all!
        out.println(lines.get(i));
        i++;
      }
      out.close(); // Close the stream!
    }
    catch (Exception e) {
      return;
    }
  }

  public synchronized int size() {
    return lines.getSize();
  }

  /**
   * Check to see if a String is contained in the File.
   * 
   * @param s
   *          The String to look for.
   * @return True if s.equalsIgnoreCase() a String in the File.
   */
  public synchronized boolean exists(String s) {
    return lines.exists(s, 0, lines.getSize());
  }

  /**
   * Add a String to the File.
   * 
   * @param s
   *          The String to add.
   */
  public synchronized void add(String s) {
    lines.add(s);
  }

  /**
   * Delete a String from the File.
   * 
   * @param s
   *          The String to delete.
   */
  public synchronized void delete(String s) {
    lines.delete(s, 0, lines.getSize());
  }

  /**
   * Delete all String from the File.
   */
  public synchronized void deleteAll() {
    lines = new RAHL();
  }

  /**
   * Get the String from a given index in the File.
   * 
   * @param i
   *          The index of the String you want.
   * @return The String at index i, or "" if i is out of range.
   */
  public synchronized String getLine(int i) {
    if ((i >= lines.getSize()) || (i < 0)) {
      return "";
    }
    return lines.get(i);
  }

  public synchronized List<String> getAll() {
    List<String> list = new LinkedList<String>();
    for (String l : lines.getAll()) {
      if (l != null) {
        list.add(l);
      }
    }
    return list;
  }

  public synchronized void addAll(List<String> list) {
    for (String s : list) {
      this.add(s);
    }
  }

  /**
   * Get the closest String in the File to a String.
   * 
   * @param s
   *          The String to look for.
   * @return The closest String in the File to s.
   */
  public synchronized String getRealLine(String s) {
    return lines.getRealString(s, 0, lines.getSize());
  }
}