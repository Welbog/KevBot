package ca.welbog.kevbot.persist;
public interface DoubleStorage {

  /**
   * Close the DoubleFile object and write their data to file.
   */
  public abstract void close();

  /**
   * Write the DoubleFile's data to file.
   */
  public abstract void write();

  /**
   * Check to see if this DoubleFile contains a String in its searchable list.
   * @param s The String to look for.
   * @return True if s.equalsIgnoreCase() a String in the DoubleFile.
   */
  public abstract boolean exists(String s);

  /**
   * Add a pair of Strings to the DoubleFile.
   * @param m The searchable String (message) to add.
   * @param r The indexed String (reply) to add.
   */
  public abstract void add(String m, String r);

  /**
   * Append a reply to the end of a message's reply.
   * @param m The searchable String (message) to add.
   * @param r The indexed String (reply) to append.
   */
  public abstract void append(String m, String r);

  /**
   * Delete a pair of Strings from the DoubleFile.
   * @param s The searchable String (message) to delete.
   */
  public abstract void delete(String s);

  /**
   * Change the reply to a given message.
   * @param m The searchable String (message) to change.
   * @param r The indexed String (reply) to add.
   * @return The position of m.
   */
  public abstract long addOver(String m, String r);

  /**
   * Delete all pairs from the DoubleFile.
   */
  public abstract void deleteAll();

  /**
   * Get a reply from a given message.
   * @param m The searchable String (message) to look for.
   * @return The indexed String (reply) corresponding to the given message, or "" if it does not exist.
   */
  public abstract String getReply(String m);

  /**
   * Find the closest searchable String (message) in the DoubleFile to a String.
   * @param m The searchable String (message) to look for.
   * @return The closest searchable String (message) to m.
   */
  public abstract String getMessage(String m);

  /**
   * Get a message/reply string by index.
   * @param i The index of the string to look for.
   * @return The message/reply pair at the given index, or "" if the index is invalid.
   */
  public abstract String getBoth(long i);

  /**
   * Get the number of String pairs being stored in the DoubleFile.
   * @return The number of String pairs being stored in the DoubleFile.
   */
  public abstract int getSize();
  
  public abstract String getRandom();

}