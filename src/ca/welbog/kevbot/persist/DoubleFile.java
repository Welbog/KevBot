package ca.welbog.kevbot.persist;
//package kevbot;

//import becker.io.*;

/* 
Copyright 2004

This file is part of KevBot.

Use KevBot at your own risk.
*/

/**
 * DoubleFile allows access to DoubleRAHL information with a file IO interface.
 * <p>
 * It is used to read, write and query all information relating to a DoubleRAHL.
 * 
 * @version 1.0.0
 * @see Kevbot
 * @see DoubleRAHL
 */
public class DoubleFile implements DoubleStorage {
  String messageFile; // The file in which we store the messages for which to
                      // look. (sorted file)
  String replyFile; // The file in which we store the replies to the messages.
                    // (indexed file)
  DoubleRAHL replies; // The DoubleRAHL corresponding to the database of
                      // messages and replies.

  /**
   * Constructor builds a DoubleFile and fills it with the information in the
   * filenames provided.
   * 
   * @param g
   *          The file from/into which messages will be read/stroed
   *          (searchable).
   * @param r
   *          The file from/into which replies will be read/stored (indexed).
   */
  public DoubleFile(String g, String r) {
    messageFile = g;
    replyFile = r;
    replies = new DoubleRAHL(); // Create a DoubleRAHL to store our Strings.
    fillLines(); // Fill the files if they exist, create the files if they do
                 // not.
  }

  /**
   * Read all Strings from the files if they exist, create the files if they do
   * not.
   */
  private void fillLines() {
    TextInput min;
    TextInput rin;
    try { // The files exist
      min = new TextInput(messageFile);
      rin = new TextInput(replyFile);
    }
    catch (Exception e) { // The files do not exist.
      try {
        TextOutput mout = new TextOutput(messageFile); // Create the files.
        TextOutput rout = new TextOutput(replyFile);
        mout.close(); // Close the files.
        rout.close();
        return;
      }
      catch (Exception r) {
        return;
      }
    }
    boolean failed;
    try {
      while (!min.eofIsAvailable()) { // Read until the file it is used up.
        String mint = "";
        String rint = "";
        failed = false;
        try {
          mint = min.getLine();
        }
        catch (Exception e) {
          while (!min.eolIsAvailable()) {
            mint += min.getWord() + " ";
          }
          min.getLine();
        }
        try {
          rint = rin.getLine();
        }
        catch (Exception e) {
          while (!rin.eolIsAvailable()) {
            rint += rin.getWord() + " ";
          }
          rin.getLine();
        }
        if (!failed) {
          mint = mint.trim();
          rint = rint.trim();
          // System.err.println("DoubleFile::fillLines()::replies.add(\""+strup(mint)+"\",\""+rint+"\");");
          replies.add(mint, rint);// Fill the DoubleRAHL.
        }
      }
      min.close(); // Close the files.
      rin.close();
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return;
    }
  }

  public String getRandom() {
    return "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#close()
   */
  public synchronized void close() {
    try {
      TextOutput mout = new TextOutput(messageFile); // Open the IO stream
      TextOutput rout = new TextOutput(replyFile);
      int i = 0;
      while (i < replies.getSize()) { // Continue until there's nothing left.
        mout.println(replies.getMessage(i)); // Write to file.
        rout.println(replies.getReply(i));
        i++;
      }
      mout.close(); // Close the IO stream.
      rout.close();
    }
    catch (Exception e) {
      return;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#write()
   */
  public synchronized void write() {
    try {
      TextOutput mout = new TextOutput(messageFile); // Open the IO stream
      TextOutput rout = new TextOutput(replyFile);
      int i = 0;
      while (i < replies.getSize()) { // Continue until there's nothing left.
        mout.println(replies.getMessage(i)); // Write to file.
        rout.println(replies.getReply(i));
        i++;
      }
      mout.close(); // Close the IO stream.
      rout.close();
    }
    catch (Exception e) {
      return;
    }
  }

  @SuppressWarnings("all")
  private String strup(String s) {
    String strup = new String(s);
    if (strup.equals("")) {
      return "";
    }
    while (new Character(strup.charAt(strup.length() - 1)).toString()
        .matches("[`~!@#$%^&*_=1|?/,. ]")) {
      try {
        strup = strup.substring(0, strup.length() - 1);
      }
      catch (Exception e) {
        return "";
      }
    }
    return strup.trim();
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#exists(java.lang.String)
   */
  public synchronized boolean exists(String s) {
    return replies.exists(s.trim(), 0, replies.getSize());
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#add(java.lang.String, java.lang.String)
   */
  public synchronized void add(String m, String r) {
    replies.add(m.trim(), r.trim());
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#append(java.lang.String, java.lang.String)
   */
  public synchronized void append(String m, String r) {
    // pre: m exists in replies.
    replies.append(m.trim(), r.trim());
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#delete(java.lang.String)
   */
  public synchronized void delete(String s) {
    replies.delete(s.trim());
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#addOver(java.lang.String, java.lang.String)
   */
  public synchronized long addOver(String m, String r) {
    return replies.addOver(m.trim(), r);
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#deleteAll()
   */
  public synchronized void deleteAll() {
    replies = new DoubleRAHL();
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#getReply(java.lang.String)
   */
  public synchronized String getReply(String m) {
    return replies.getReply(m);
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#getMessage(java.lang.String)
   */
  public synchronized String getMessage(String m) {
    return replies.getRealMessage(m);
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#getBoth(int)
   */
  public synchronized String getBoth(long id) {
    int i = (int) id;
    if ((i < 0) || (i >= replies.getSize())) {
      return "";
    }
    return replies.getMessage(i) + " " + replies.getReply(i);
  }

  public synchronized String getMessage(long id) {
    int i = (int) id;
    return replies.getMessage(i);
  }

  public synchronized String getReply(long id) {
    int i = (int) id;
    return replies.getReply(i);
  }

  /*
   * (non-Javadoc)
   * 
   * @see DoubleStorage#getSize()
   */
  public synchronized int getSize() {
    return replies.getSize();
  }
}
