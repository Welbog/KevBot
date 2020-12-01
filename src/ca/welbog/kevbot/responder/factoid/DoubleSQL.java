package ca.welbog.kevbot.responder.factoid;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import ca.welbog.kevbot.persist.ConnectionProvider;

/**
 * Needs some table definitions! Probably just go with name/value pairs for
 * now...
 * 
 * Message id INT NOT NULL PK AUTO INCREMENT cleanmessage TEXT NOT NULL UNIQUE,
 * full-text index - the message used to simplify matching, also patterns for
 * parameterized queries. message TEXT NOT NULL - the actual message createuser
 * varchar(255) NULL createdate datetime NULL deleteuser varchar(255) NULL
 * deletedate datetime NULL deleted BIT NOT NULL DEFAULT 0 regex BIT NOT NULL
 * DEFAULT 0 CREATE TABLE Message (id INT UNSIGNED NOT NULL PRIMARY KEY
 * AUTO_INCREMENT, cleanmessage TEXT NOT NULL, message TEXT NOT NULL,
 * FULLTEXT(cleanmessage)) type = MyISAM; ALTER TABLE Message ADD createuser
 * VARCHAR(255) NULL, ADD createdate DATETIME NULL; ALTER TABLE Message ADD
 * deleted TINYINT NOT NULL DEFAULT 0; ALTER TABLE Message ADD deleteuser
 * VARCHAR(255) NULL, ADD deletedate DATETIME NULL; ALTER TABLE Message ADD
 * regex TINYINT NOT NULL DEFAULT 0; CREATE TABLE Message (id INT UNSIGNED NOT
 * NULL PRIMARY KEY AUTO_INCREMENT, cleanmessage TEXT NOT NULL, message TEXT NOT
 * NULL, createuser VARCHAR(255) NULL, createdate DATETIME NULL, deleted BIT NOT
 * NULL, deleteuser VARCHAR(255) NULL, deletedate DATETIME NULL) type = MyISAM;
 * 
 * Reply MessageId INT NOT NULL PK id INT NOT NULL AUTO INCREMENT PK reply TEXT
 * NOT NULL CREATE TABLE Reply (messageid INT UNSIGNED NOT NULL, id INT UNSIGNED
 * NOT NULL AUTO_INCREMENT, reply TEXT NOT NULL, PRIMARY KEY (messageid,id));
 * 
 * 
 * @author Inferno
 *
 */

public class DoubleSQL {
  private Random random;
  private ConnectionProvider provider;

  private static final String TOKEN_REGEX = "\\$[a-z_]+";

  public DoubleSQL(ConnectionProvider provider) {
    random = new Random();
    this.provider = provider;
  }

  private Connection getConnection() {
    return (Connection) provider.getObject();
  }

  private synchronized void add(String m, String r, String user) {
    try {
      user = sanitize(user);
      purgeIfDeleted(m);
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      boolean isParameterized = isParameterized(m);
      String clean = cleanString(m, isParameterized);
      if (!isParameterized || !isStupid(clean)) {
        m = m.replaceAll("\\\\", "\\\\\\\\");
        r = r.replaceAll("\\\\", "\\\\\\\\");
        clean = clean.replaceAll("\\\\", "\\\\\\\\");
        m = m.replaceAll("'", "\\\\'");
        r = r.replaceAll("'", "\\\\'");
        clean = clean.replaceAll("'", "\\\\'");
        String SQLStatement1 = ""
            + "INSERT INTO Message (cleanmessage, message, regex, createuser, createdate) "
            + "VALUES ('" + clean + "','" + m + "', '" + (isParameterized ? "1" : "0") + "', '"
            + user + "', now())" + ";";
        String SQLStatement2 = "" + "INSERT INTO Reply (messageid, reply) "
            + "VALUES (LAST_INSERT_ID(),'" + r + "')" + ";";
        statement.execute(SQLStatement1);
        statement.execute(SQLStatement2);
        statement.close();
      }
      else {
        System.out.println("Stupid string added: '" + m + "'");
      }
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
  }
  /*
   * public long addOver(String m, String r, String user) { try { user =
   * sanitize(user); long id = getActiveID(m); if (id >= 0) { Connection conn =
   * getConnection(); Statement statement = conn.createStatement(); m =
   * m.replaceAll("\\\\", "\\\\\\\\"); r = r.replaceAll("\\\\", "\\\\\\\\"); m =
   * m.replaceAll("'", "\\\\'"); r = r.replaceAll("'", "\\\\'"); String
   * SQLStatement1 = "" + "UPDATE Message " +
   * "SET message = '"+m+"', createuser = '"+user+"', createdate = now() " +
   * "WHERE id = " + id + ";"; String SQLStatement2 = "" + "DELETE FROM Reply "
   * + "WHERE messageid = " + id + ";"; String SQLStatement3 = "" +
   * "INSERT INTO Reply (messageid, reply) " + "VALUES ("+id+",'"+r+"')" + ";";
   * statement.execute(SQLStatement1); statement.execute(SQLStatement2);
   * statement.execute(SQLStatement3); statement.close(); return id; } else {
   * add(m,r,user); return getID(m, true); } } catch (SQLException ex) { //
   * handle any errors System.out.println("SQLException: " + ex.getMessage());
   * System.out.println("SQLState: " + ex.getSQLState());
   * System.out.println("VendorError: " + ex.getErrorCode()); } catch (Exception
   * e) { System.out.println("Exception: " + e.getMessage()); } return -1; }
   */

  public synchronized void append(String m, String r, String user) {
    System.out.println("append(" + m + ", " + r + ", " + user + ")");
    try {
      user = sanitize(user);
      Connection conn = getConnection();
      long id = getActiveID(m, true);
      if (id >= 0) {
        Statement statement = conn.createStatement();
        m = m.replaceAll("\\\\", "\\\\\\\\");
        r = r.replaceAll("\\\\", "\\\\\\\\");
        m = m.replaceAll("'", "\\\\'");
        r = r.replaceAll("'", "\\\\'");
        String SQLStatement1 = "" + "INSERT INTO Reply (messageid, reply) " + "VALUES (" + id + ",'"
            + r + "')" + ";";
        statement.execute(SQLStatement1);
        statement.close();
      }
      else {
        add(m, r, user);
      }
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
  }

  public synchronized void close() {
    try {
      Connection conn = getConnection();
      conn.close();
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
  }

  private synchronized void purgeIfDeleted(String m) {
    long id = getExactMatchID(m, false);
    if (id >= 0) {
      if (isDeleted(id)) {
        purge(id);
      }
    }
  }

  private synchronized void purge(long id) {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      String SQLStatement1 = "" + "DELETE FROM Message WHERE id = " + id + ";";
      String SQLStatement2 = "" + "DELETE FROM Reply WHERE messageid = " + id + ";";
      statement.execute(SQLStatement1);
      statement.execute(SQLStatement2);
      statement.close();
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }

  }

  public synchronized boolean undelete(String s) {
    try {
      Connection conn = getConnection();
      long id = getExactMatchID(s, false);
      if (isDeleted(id)) {
        Statement statement = conn.createStatement();
        statement.execute("" + "UPDATE Message SET deleted = 0 WHERE id = " + id + ";");
        statement.close();
        return true;
      }
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    return false;
  }

  public synchronized boolean delete(String s, String user) {
    try {
      user = sanitize(user);
      Connection conn = getConnection();
      long id = getActiveID(s, true);
      if (id >= 0) {
        String SQLStatement1 = "";
        if (isParameterized(id)) { // Hard delete parameterized factoids because
                                   // they get in the way
          purge(id);
          return true;
        }
        else { // Soft delete normal factoids
          Statement statement = conn.createStatement();
          SQLStatement1 = "" + "UPDATE Message SET deleted = 1, deleteuser = '" + user
              + "', deletedate = now() WHERE id = " + id + ";";
          statement.execute(SQLStatement1);
          statement.close();
          return true;
        }
      }
      else {
        return false;
      }
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
    return false;
  }

  public synchronized void deleteAll() {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      String SQLStatement1 = "" + "TRUNCATE TABLE Message;";
      String SQLStatement2 = "" + "TRUNCATE TABLE Reply;";
      statement.execute(SQLStatement1);
      statement.execute(SQLStatement2);
      statement.close();
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
  }

  public synchronized boolean exists(String s) {
    return getActiveID(s, true) >= 0;
  }

  public synchronized String getRandom() {
    return getDetails(getRandomID());
  }

  public synchronized String getBoth(long id) {
    try {
      Connection conn = getConnection();
      if (id >= -1) {
        Statement statement = conn.createStatement();
        ResultSet rs1 = statement
            .executeQuery("SELECT message FROM Message WHERE id = " + id + ";");
        rs1.first();
        String x = rs1.getString(1);
        rs1.close();

        // This result set has to be defined after the first result set is dealt
        // with because result sets are retarded.
        ResultSet rs2 = statement
            .executeQuery("SELECT reply FROM Reply WHERE messageid = " + id + ";");
        if (rs2 == null) {
          statement.close();
          return "";
        }
        if (!rs2.first()) {
          rs2.close();
          statement.close();
          return "";
        }
        String result = null;
        rs2.beforeFirst();
        while (rs2.next()) {
          if (result == null) {
            result = rs2.getString(1);
          }
          else {
            result += " | " + rs2.getString(1);
          }
        }
        rs2.close();
        statement.close();
        return x + " " + result;
      }
      return "";
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return "";
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return "";
    }
  }

  public synchronized String getMessage(String m) {
    try {
      Connection conn = getConnection();
      long id = getActiveID(m, false);
      if (id >= -1) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT message FROM Message WHERE id = " + id + ";");
        if (rs == null) {
          statement.close();
          return "";
        }
        if (!rs.first()) {
          rs.close();
          statement.close();
          return "";
        }
        String result = rs.getString(1);
        rs.close();
        statement.close();
        return result;
      }
      return "";
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return "";
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return "";
    }
  }

  public synchronized String getSingleReply(String m) {

    try {
      Connection conn = getConnection();
      long id = getActiveID(m, false);
      if (id == -1) {
        return "";
      }
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(
          "SELECT reply FROM Reply WHERE messageid = " + id + " ORDER BY rand() LIMIT 1;");
      if (rs == null) {
        statement.close();
        return "";
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return "";
      }
      String result = null;
      rs.beforeFirst();
      rs.next();
      result = rs.getString(1);
      rs.close();
      statement.close();

      // Replace tokens in the reply with their values parsed out of the
      // message.
      if (isParameterized(id)) {
        Map<String, String> tokenMap = generateTokenMap(id, m);
        result = applyTokenMap(tokenMap, result);
      }

      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return "";
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return "";
    }
  }

  public synchronized String getFullReply(String m) {
    try {
      Connection conn = getConnection();
      long id = getActiveID(m, false);
      if (id == -1) {
        return "";
      }
      Statement statement = conn.createStatement();
      ResultSet rs = statement
          .executeQuery("SELECT reply FROM Reply WHERE messageid = " + id + ";");
      if (rs == null) {
        statement.close();
        return "";
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return "";
      }
      String result = null;
      rs.beforeFirst();
      while (rs.next()) {
        if (result == null) {
          result = rs.getString(1);
        }
        else {
          result += " | " + rs.getString(1);
        }
      }
      rs.close();
      statement.close();

      // Replace tokens in the reply with their values parsed out of the
      // message.
      if (isParameterized(id)) {
        Map<String, String> tokenMap = generateTokenMap(id, m);
        result = applyTokenMap(tokenMap, result);
      }

      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return "";
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return "";
    }
  }

  private synchronized Map<String, String> generateTokenMap(long id, String message) {
    try {
      Connection conn = getConnection();
      if (id >= -1) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT message FROM Message WHERE id = " + id + ";");
        if (rs == null) {
          statement.close();
          return null;
        }
        if (!rs.first()) {
          rs.close();
          statement.close();
          return null;
        }
        String stored = rs.getString("message");
        rs.close();
        statement.close();

        Map<String, String> tokenMap = new HashMap<String, String>();
        StringTokenizer storedTokens = new StringTokenizer(stored);
        StringTokenizer messageTokens = new StringTokenizer(message);
        while (storedTokens.hasMoreTokens() && messageTokens.hasMoreTokens()) {
          String storedToken = storedTokens.nextToken();
          String messageToken = messageTokens.nextToken();
          messageToken = cleanString(messageToken, false);
          if (storedToken.matches("^" + TOKEN_REGEX + "$")) {
            String parameter = storedToken.substring(1);
            tokenMap.put(parameter, messageToken);
          }
        }
        return tokenMap;

      }
      return null;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return null;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return null;
    }
  }

  private synchronized String applyTokenMap(Map<String, String> tokenMap, String string) {
    if (tokenMap == null) {
      return string;
    }
    // For each token name, find its corresponding token and replace it.
    for (Map.Entry<String, String> entry : tokenMap.entrySet()) {
      string = string.replaceAll("\\$" + entry.getKey(), entry.getValue());
    }
    return string;
  }

  public synchronized int getSize() {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM Message WHERE deleted = 0;");
      if (rs == null) {
        statement.close();
        return 0;
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return 0;
      }
      int result = rs.getInt(1);
      rs.close();
      statement.close();
      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return 0;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return 0;
    }
  }

  public synchronized void write() {
    // This one doesn't do anything for now.
  }

  private synchronized long getRandomID() {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();

      ResultSet countSet = statement
          .executeQuery("SELECT COUNT(*) FROM Message WHERE deleted = 0;");
      countSet.first();
      int count = countSet.getInt(1);
      countSet.close();

      if (count == 0) {
        return -1;
      }

      int target = random.nextInt(count);

      ResultSet rs = statement.executeQuery("" + "SELECT id " + "FROM Message "
          + "WHERE deleted = 0 " + "LIMIT " + target + ",1" + ";");

      if (rs == null) {
        statement.close();
        return -1;
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return -1;
      }
      int result = rs.getInt(1);
      statement.close();
      rs.close();
      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return -1;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return -1;
    }
  }

  public synchronized String getDetails(String m) {
    long id = getID(m, false);
    if (id < 0) {
      return "";
    }
    return getDetails(id);
  }

  public synchronized String deleteSpecific(String m, String r) {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      long id = getActiveID(m, false);
      if (id < 0) {
        return "Sorry, " + m + " doesn't exist in the database.";
      }

      ResultSet rs = statement
          .executeQuery("" + "SELECT count(id) FROM Reply " + "WHERE messageid = " + id + ";");
      if (rs == null) {
        statement.close();
        return "";
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return "";
      }
      int replyCount = rs.getInt(1);
      rs.close();
      statement.close();
      if (replyCount == 1) {
        return "Sorry, " + m + " has only one reply. Use 'forget' instead to delete it.";
      }

      statement = conn.createStatement();
      int numberDeleted = statement.executeUpdate("" + "DELETE FROM Reply " + "WHERE reply = '"
          + sanitize(r) + "' " + "AND messageid = " + id + " " + "LIMIT 1;");
      statement.close();
      if (numberDeleted == 1) {
        return "Successfully deleted " + m + " " + r;
      }
      else {
        return "Unable to delete " + m + " " + r;
      }
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return "";
    }
  }

  private synchronized String getDetails(long id) {
    try {
      String result = "";
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      ResultSet rs = statement
          .executeQuery("" + "SELECT createuser, createdate, deleteuser, deletedate, deleted "
              + "FROM Message " + "WHERE id = " + id + ";");

      if (rs == null) {
        statement.close();
        return "";
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return "";
      }

      String createdBy = rs.getString(1);
      createdBy = rs.wasNull() ? "Unknown" : createdBy;

      String createdOn = (rs.getTimestamp(2) == null) ? "Unknown" : rs.getTimestamp(2).toString();
      createdOn = rs.wasNull() ? "Unknown" : createdOn;
      createdOn = createdOn.replace(".0", "");

      String deletedBy = rs.getString(3);
      deletedBy = rs.wasNull() ? "Unknown" : deletedBy;

      String deletedOn = (rs.getTimestamp(4) == null) ? "Unknown" : rs.getTimestamp(4).toString();
      deletedOn = rs.wasNull() ? "Unknown" : deletedOn;
      deletedOn = deletedOn.replace(".0", "");

      Boolean deleted = rs.getBoolean(5);
      rs.close();
      statement.close();

      if (deleted) {
        result = "Factoid " + id + " (Created by " + createdBy + " on " + createdOn
            + ", deleted by " + deletedBy + " on " + deletedOn + "): ";
      }
      else {
        result = "Factoid " + id + " (Created by " + createdBy + " on " + createdOn + "): ";
      }
      result += getBoth(id);
      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return "";
    }
  }

  private synchronized String sanitize(String s) {
    return s.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
  }

  private synchronized boolean isDeleted(long id) {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      ResultSet rs = statement
          .executeQuery("" + "SELECT deleted " + "FROM Message " + "WHERE id = " + id + ";");

      if (rs == null) {
        statement.close();
        return false;
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return false;
      }
      boolean result = rs.getBoolean(1);
      rs.close();
      statement.close();
      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return false;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return false;
    }
  }

  private synchronized long getID(String m, boolean activeOnly) {
    // This is the most important function
    // I need to figure out how to get this to emulate KevBot.
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      m = cleanString(m, false);
      m = m.replaceAll("\\\\", "\\\\\\\\");
      m = m.replaceAll("'", "\\\\'");
      // System.out.println("\tgetID: " + m);
      ResultSet rs = statement
          .executeQuery("" + "SELECT id " + "FROM Message " + "WHERE cleanmessage = '" + m + "' "
              + "AND regex = 0 " + (activeOnly ? "AND deleted = 0 " : "") + ";");
      boolean nonregexmatch = true;
      if (rs == null) {
        nonregexmatch = false;
        statement.close();
      }
      else {
        if (!rs.first()) {
          rs.close();
          statement.close();
          nonregexmatch = false;
        }
      }
      if (!nonregexmatch) {
        statement = conn.createStatement();
        rs = statement.executeQuery("" + "SELECT id " + "FROM Message " + "WHERE '" + m
            + "' RLIKE cleanmessage " + "AND regex = 1 " + (activeOnly ? "AND deleted = 0 " : "")
            + "ORDER BY deleted ASC " + ";");
        if (rs == null) {
          statement.close();
          return -1;
        }
        else {
          if (!rs.first()) {
            rs.close();
            statement.close();
            return -1;
          }
        }
      }
      long result = rs.getLong(1);
      rs.close();
      statement.close();
      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return -1;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return -1;
    }
  }

  /**
   * Get exact match ID for a given message string. This method checks
   * parameterized factoids first, then defers to the normal matching algorithm.
   * This is done for edits, to force edits to parameterized factoids to use the
   * same variable names, and to allow new normal factoids to "clobber" existing
   * parameterized factoids if desired.
   * 
   * This method should be used in place of getID whenever the value being used
   * is going to be edited as opposed to just read-only operations.
   * 
   * @param m
   * @param activeOnly
   * @return
   */
  private synchronized long getExactMatchID(String message, boolean activeOnly) {
    try {
      Connection conn = getConnection();
      String cleanedNormalString = sanitize(cleanString(message, false)).trim();
      String cleanedParameterString = sanitize(cleanString(message, true)).trim();
      // cleanString has its parameters converted to regex form, but we will
      // match it with = instead of RLIKE to make sure that we match on
      // signature in the case that parameters have different names.
      // In other words, "fight $dog" should match "fight $cat", because they
      // have the same signature.
      boolean foundExactParameterMatch = true;
      // System.out.println("\tgetID: " + m);

      // Parameterized factoids
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery("" + "SELECT id " + "FROM Message " + "WHERE '"
          + cleanedParameterString + "' = cleanmessage " + "AND regex = 1 "
          + (activeOnly ? "AND deleted = 0 " : "") + "ORDER BY deleted ASC " + ";");
      if (rs == null) {
        foundExactParameterMatch = false;
        statement.close();
      }
      else {
        if (!rs.first()) {
          rs.close();
          statement.close();
          foundExactParameterMatch = false;
        }
      }
      if (!foundExactParameterMatch) {

        // Normal factoids (match these against the clean string normally, not
        // against the exact string)
        statement = conn.createStatement();
        rs = statement.executeQuery(
            "" + "SELECT id " + "FROM Message " + "WHERE cleanmessage = '" + cleanedNormalString
                + "' " + "AND regex = 0 " + (activeOnly ? "AND deleted = 0 " : "") + ";");
        if (rs == null) {
          statement.close();
          return -1;
        }
        else {
          if (!rs.first()) {
            rs.close();
            statement.close();
            return -1;
          }
        }
      }
      long result = rs.getLong(1);
      rs.close();
      statement.close();
      return result;

    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return -1;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return -1;
    }
  }

  private synchronized long getActiveID(String m, boolean forEdit) {
    if (forEdit) {
      return getExactMatchID(m, true);
    }
    else {
      return getID(m, true);
    }
  }

  private synchronized static String cleanString(String s, boolean replaceParameters) {
    String strup = new String(s);
    if (strup.equals("")) {
      return "";
    }
    if (replaceParameters) {
      strup = strup.replaceAll(TOKEN_REGEX, "([^ ]+)");
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
    if (replaceParameters) {
      return "^" + strup.trim() + "$";
    }
    return strup.trim();
  }

  private synchronized static boolean isParameterized(String s) {
    if (s == null) {
      return false;
    }
    s = s.trim();
    if (s.equals("")) {
      return false;
    }
    return s.matches(".*" + TOKEN_REGEX + ".*");
  }

  // Returns true if a message contains ONLY parameters. To be called after
  // cleanString(s,true) has been called on it
  private synchronized static boolean isStupid(String s) {
    return s.replaceAll(Pattern.quote("([^ ]+)"), "").trim().matches("^\\^ *\\$$");
  }

  public synchronized boolean isParameterized(long id) {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement();
      ResultSet rs = statement
          .executeQuery("" + "SELECT regex " + "FROM Message " + "WHERE id = " + id + ";");

      if (rs == null) {
        statement.close();
        return false;
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return false;
      }
      boolean result = rs.getBoolean("regex");
      rs.close();
      statement.close();
      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return false;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return false;
    }
  }

  public static void main(String[] args) throws Exception {
    DoubleSQL doug = new DoubleSQL(new ConnectionProvider());
    System.out.println(cleanString("bob $*", true));
    System.out.println(cleanString("bob $*", false));
    System.out.println(cleanString("bob $dog", true));
    System.out.println(cleanString("bob $dog", true));
    System.out.println(cleanString("$bob $dog", true));
    System.out.println(cleanString("$bob $dog ", true));
    System.out.println(cleanString("$bob $dog 5 ", true));
    System.out.println(isParameterized("bob $dog"));
    System.out.println(isStupid(cleanString("bob $dog", true)));
    System.out.println(isStupid(cleanString("$bob $dog", true)));
    System.out.println(isStupid(cleanString("$bob $dog ", true)));
    System.out.println(isStupid(cleanString("$bob $dog 5 ", true)));
    System.out.println(cleanString("bob $dog", false));
    System.out.println(cleanString("bob $dog cat", true));
    System.out.println(cleanString("bob $dog cat", false));
    System.out.println(cleanString("bob $dog $cat", true));
    System.out.println(cleanString("bob $dog $cat", false));
    System.out.println(cleanString("bob $dog $cat cow", true));
    System.out.println(cleanString("bob $dog $cat cow", false));
    System.out.println(cleanString("bob $dog rat $cat cow", true));
    System.out.println(cleanString("bob $dog rat $cat cow", false));
    System.out.println(cleanString("$bob $dog rat $cat cow", true));
    System.out.println(cleanString("$bob $dog rat $cat cow", false));
    /* doug.deleteAll(); */
    /*
     * DoubleFile original = new
     * DoubleFile("D:\\Files\\Eclipse\\Workspace\\KevBot\\check.txt",
     * "D:\\Files\\Eclipse\\Workspace\\KevBot\\replies.txt");
     * System.out.println("The original RAHL size: " + original.getSize());
     * 
     * for (long i = 101664; i < original.getSize(); i++) { String m =
     * original.getMessage(i); String bigr = original.getReply(i);
     * 
     * String[] rs = bigr.split(Pattern.quote(" | ")); for (String r : rs) { if
     * (doug.exists(m)) { System.out.println("Collision: " + m); doug.append(m,
     * r); } else { doug.add(m, r); } } }
     * 
     * 
     * 
     * 
     * System.out.println("The new DoubleSQL size: " + doug.getSize()); //
     * 103686, 102525 original.close();
     */
    /*
     * System.out.println(doug.getReply("ddd!?"));
     * System.out.println(doug.getDetails("ddd"));
     * doug.delete("Test test test test", "Nick");
     * doug.add("Test test test test", "Test test test test!","Inferno");
     * System.out.println(doug.exists("Test test test test"));
     * System.out.println(doug.getDetails("Test test test test"));
     * System.out.println(doug.getReply("Test test test test"));
     * doug.delete("Test test test test", "Inferno");
     * System.out.println(doug.exists("Test test test test"));
     * System.out.println(doug.getDetails("Test test test test"));
     * System.out.println(doug.getReply("Test test test test"));
     * doug.add("Test test test test", "Test test test test?","Inferno");
     * System.out.println(doug.exists("Test test test test"));
     * System.out.println(doug.getDetails("Test test test test"));
     * System.out.println(doug.getReply("Test test test test"));
     * doug.delete("Test test test test", "Nick");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.add("Test test test test", "Test test test test!","Inferno");
     * doug.undelete("Test test test test");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.addOver("Test test test test", "OH NO!", "Keigo");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.delete("Test test test test", "Nick");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.addOver("Test test test test", "OH MAYBE!", "Keigo");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.append("Test test test test", "OH YES!", "Kai");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.delete("Test test test test", "Nick");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.append("Test test test test", "OH LASER!", "Inferno");
     * System.out.println(doug.getDetails("Test test test test"));
     * doug.append("Test test test test", "OH LASER?", "Inferno");
     * System.out.println(doug.getDetails("Test test test test"));
     * System.out.println(doug.
     * getDetails("I know this doesn't exists because it has is in it."));
     */
    System.out.println(doug.getRandom());

    doug.close();

    // Read in the old reply files and populate the SQL tables with them! It's
    // like a laser.
  }

}
