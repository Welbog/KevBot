package ca.welbog.kevbot.responder.markov;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import ca.welbog.kevbot.persist.ConnectionProvider;

public class SQLWeightedMarkov {
  private int _order = 1;
  private final int MAX_CHAIN_LENGTH = 36;
  private final int MIN_CHAIN_LENGTH = 12;
  private Random random;
  private ConnectionProvider provider = null;

  /*
   * CREATE TABLE weightedmarkov ( seed varchar(250) not null, word varchar(250)
   * not null, weight bigint unsigned not null, primary key (seed, word) );
   */

  public SQLWeightedMarkov(ConnectionProvider provider, int order) {
    _order = order;
    random = new Random();
    this.provider = provider;
  }

  private Connection getConnection() {
    return (Connection) provider.getObject();
  }

  public synchronized int size() {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM weightedmarkov;");
      // ResultSet rs = statement.executeQuery("SELECT * FROM markov;");
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
      ex.printStackTrace();
      return 0;
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return 0;
    }
  }

  public synchronized String generateSentence(Vector<String> tokens, boolean useSeedInOutput) {

    Queue<String> baseQueue = new LinkedList<String>();
    Queue<String> startQueue = new LinkedList<String>();
    for (int i = 0; i < _order; i++) {
      baseQueue.add("");
      startQueue.add("");
    }
    for (int i = tokens.size() - _order; i < tokens.size(); i++) {
      if (i >= 0) {
        baseQueue.poll();
        baseQueue.add((String) tokens.get(i));
      }
    }
    for (int i = 0; i < tokens.size(); i++) {
      startQueue.add((String) tokens.get(i));
    }

    StringBuilder result = new StringBuilder();
    if (useSeedInOutput) {
      String start = queueToToken(startQueue).trim();
      result.append(start);
    }

    for (int i = 0; i < MAX_CHAIN_LENGTH; i++) {
      // System.out.println("Iteration: " + i);
      // System.out.println("Current: " + current.getValue());
      // System.out.println("Current children: " + current.childCount());
      // System.out.println("Result: " + result.toString());
      String base = queueToToken(baseQueue);
      String next = retrieveNext(base);
      if (result.length() == 0) {
        result.append(next);
      }
      else {
        result.append(" " + next);
      }

      if (isTerminating(next) && i >= MIN_CHAIN_LENGTH) {
        break;
      }
      else if (next.equals("")) {
        break;
      }
      else {
        baseQueue.poll();
        baseQueue.add(next);
      }
    }
    return result.toString().trim();
  }

  private synchronized boolean isTerminating(String s) {
    return (s.endsWith(".") || s.endsWith("?") || s.endsWith("!"));
  }

  public synchronized String generateSentence() {
    Vector<String> v = new Vector<String>();
    for (int i = 1; i < _order; i++) {
      v.add("");
    }
    return generateSentence(v, true);
  }

  public synchronized void addSentence(String sentence) {
    if (sentence == null) {
      return;
    }
    if (sentence.equals("")) {
      return;
    }
    StringTokenizer tokenizer = new StringTokenizer(sentence);
    if (tokenizer.countTokens() <= 1) {
      return;
    }

    Queue<String> currentToken = new LinkedList<String>();
    for (int i = 0; i < _order; i++) {
      currentToken.add("");
    }

    while (tokenizer.hasMoreTokens()) {
      String seed = queueToToken(currentToken);
      String word = tokenizer.nextToken();
      writePair(seed, word);
      currentToken.poll(); // nextToken is now of length order-1
      currentToken.add(word); // nextToken is now of length order
    }
  }

  private synchronized String queueToToken(Queue<String> tokenQueue) {
    String[] tokenArray = tokenQueue.toArray(new String[0]);
    String token = tokenArray[0];
    for (int i = 1; i < tokenArray.length; i++) {
      token += " " + tokenArray[i];
    }
    return token;
  }

  /*
   * public static void main(String[] args) { SQLWeightedMarkov m1 = new
   * SQLWeightedMarkov(1); SQLWeightedMarkov m2 = new SQLWeightedMarkov(2);
   * 
   * System.out.println(m1.generateSentence());
   * 
   * Vector<String> v = new Vector<String>(); v.add("");
   * System.out.println(m2.generateSentence(v));
   * System.out.println(m2.generateSentence(v));
   * System.out.println(m2.generateSentence(v));
   * System.out.println(m2.generateSentence()); }
   */

  private synchronized void writePair(String seed, String word) {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      seed = seed.replaceAll("\\\\", "\\\\\\\\");
      word = word.replaceAll("\\\\", "\\\\\\\\");
      seed = seed.replaceAll("'", "\\\\'");
      word = word.replaceAll("'", "\\\\'");

      // Need to confirm if the pair exists first,
      // If so, increment its weight.
      // If not, insert new pair with weight = 1
      ResultSet countSet = statement
          .executeQuery("SELECT COUNT(*) FROM weightedmarkov WHERE seed = '" + seed
              + "' AND word = '" + word + "';");
      countSet.first();
      int count = countSet.getInt(1);
      countSet.close();
      if (count > 0) {
        statement.execute("UPDATE weightedmarkov SET weight = weight+1 WHERE seed = '" + seed
            + "' AND word = '" + word + "';");
      }
      else {
        statement.execute("INSERT INTO weightedmarkov (seed, word, weight) VALUES ('" + seed + "','"
            + word + "',1);");
      }
      statement.close();

    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      ex.printStackTrace();
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
  }

  private synchronized String retrieveNext(String seed) {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      seed = seed.replaceAll("\\\\", "\\\\\\\\");
      seed = seed.replaceAll("'", "\\\\'");

      ResultSet countSet = statement
          .executeQuery("SELECT SUM(weight) FROM weightedmarkov WHERE seed = '" + seed + "';");
      countSet.first();
      int count = countSet.getInt(1);
      countSet.close();

      if (count == 0) {
        statement.close();
        return "";
      }

      int target = random.nextInt(count);
      ResultSet rs = statement
          .executeQuery("SELECT word, weight FROM weightedmarkov WHERE seed = '" + seed + "';");
      // Might want to order this by weight descending.

      if (rs == null) {
        statement.close();
        return "";
      }
      if (!rs.first()) {
        rs.close();
        statement.close();
        return "";
      }
      rs.beforeFirst();
      String result = "";
      while (rs.next()) {
        long weight = rs.getLong("weight");
        if (target >= 0 && target < weight) {
          result = rs.getString("word");
          break;
        }
        target -= weight;
      }
      rs.close();
      statement.close();
      return result;
    }
    catch (SQLException ex) {
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      ex.printStackTrace();
      return "";
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return "";
    }
  }

}
