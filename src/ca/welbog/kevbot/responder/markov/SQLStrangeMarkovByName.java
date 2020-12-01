package ca.welbog.kevbot.responder.markov;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import ca.welbog.kevbot.persist.ConnectionProvider;

public class SQLStrangeMarkovByName {

  /*
   * CREATE TABLE weightedmarkovbyuser ( user varchar(40) not null, seed
   * varchar(230) not null, word varchar(230) not null, weight bigint unsigned
   * not null, primary key (user, seed, word) ); CREATE INDEX
   * weightedmarkovbyuser_user ON weightedmarkovbyuser (user);
   */

  /*
   * Get the total strange weight of a given user/seed: SELECT w.user, w.seed,
   * SUM(w.weight / g.weight) AS total FROM weightedmarkovbyuser w INNER JOIN
   * weightedmarkov g ON g.seed = w.seed AND g.word = w.word WHERE w.user =
   * 'Inferno' AND w.seed = ' ' ;
   */

  /*
   * Get the strange weights of a given user/seed: SELECT w.user, w.seed,
   * w.word, (w.weight / g.weight) AS weight FROM weightedmarkovbyuser w INNER
   * JOIN weightedmarkov g ON g.seed = w.seed AND g.word = w.word WHERE w.user =
   * 'Inferno' AND w.seed = ' ' ;
   */

  private static final int MIN_CHAIN_LENGTH = 12;
  private static final int MAX_CHAIN_LENGTH = 25;
  private final Random random = new Random();
  private ConnectionProvider provider = null;

  public SQLStrangeMarkovByName(ConnectionProvider provider) {
    this.provider = provider;
  }

  private Connection getConnection() {
    return (Connection) provider.getObject();
  }

  public synchronized String generateSentence(int order, String user, Vector<String> tokens) {
    Queue<String> baseQueue = new LinkedList<String>();
    Queue<String> startQueue = new LinkedList<String>();
    for (int i = 0; i < order; i++) {
      baseQueue.add("");
      startQueue.add("");
    }
    for (int i = tokens.size() - order; i < tokens.size(); i++) {
      if (i >= 0) {
        baseQueue.poll();
        baseQueue.add((String) tokens.get(i));
      }
    }
    for (int i = 0; i < tokens.size(); i++) {
      startQueue.add((String) tokens.get(i));
    }
    String start = queueToToken(startQueue).trim();

    StringBuilder result = new StringBuilder(start);

    for (int i = 0; i < MAX_CHAIN_LENGTH; i++) {
      // System.out.println("Iteration: " + i);
      // System.out.println("Current: " + current.getValue());
      // System.out.println("Current children: " + current.childCount());
      // System.out.println("Result: " + result.toString());
      String base = queueToToken(baseQueue);
      String next = retrieveNext(user, base);
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

  public synchronized String generateSentence(int order, String user) {
    Vector<String> v = new Vector<String>();
    for (int i = 1; i < order; i++) {
      v.add("");
    }
    return generateSentence(order, user, v);
  }

  private synchronized String queueToToken(Queue<String> tokenQueue) {
    String[] tokenArray = tokenQueue.toArray(new String[0]);
    String token = tokenArray[0];
    for (int i = 1; i < tokenArray.length; i++) {
      token += " " + tokenArray[i];
    }
    return token;
  }

  private synchronized boolean isTerminating(String s) {
    return (s.endsWith(".") || s.endsWith("?") || s.endsWith("!"));
  }

  private synchronized String retrieveNext(String user, String seed) {
    try {
      Connection conn = getConnection();
      Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
          ResultSet.CONCUR_READ_ONLY);
      seed = seed.replaceAll("\\\\", "\\\\\\\\");
      seed = seed.replaceAll("'", "\\\\'");
      user = user.replaceAll("\\\\", "\\\\\\\\");
      user = user.replaceAll("'", "\\\\'");

      ResultSet countSet = statement
          .executeQuery("SELECT w.user, w.seed, SUM(w.weight / g.weight) AS total "
              + "FROM weightedmarkovbyuser w INNER JOIN weightedmarkov g "
              + "ON g.seed = w.seed AND g.word = w.word " + "WHERE w.user = '" + user
              + "' AND w.seed = '" + seed + "';");
      countSet.first();
      double count = countSet.getDouble("total");
      countSet.close();

      if (count == 0 || Double.isNaN(count)) {
        statement.close();
        return "";
      }

      double target = random.nextDouble() * count;
      ResultSet rs = statement
          .executeQuery("SELECT w.user, w.seed, w.word, (w.weight / g.weight) AS weight "
              + "FROM weightedmarkovbyuser w INNER JOIN weightedmarkov g "
              + "ON g.seed = w.seed AND g.word = w.word " + "WHERE w.user = '" + user
              + "' AND w.seed = '" + seed + "';");
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
      boolean wasSet = false;
      String lastWord = "";
      while (rs.next()) {
        double weight = rs.getDouble("weight");
        if (target >= 0 && target < weight) {
          result = rs.getString("word");
          wasSet = true;
          break;
        }
        target -= weight;
        lastWord = rs.getString("word");
      }
      if (!wasSet) {
        result = lastWord;
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
      return "";
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      return "";
    }
  }

}
