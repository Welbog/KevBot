package ca.welbog.kevbot.persist;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

public class ConnectionProvider {
  private Connection conn = null;
  private Date lastConnectionCreationTime = new Date();
  private static final Long TIMEOUT_IN_MILLISECONDS = 600000L; // 10 minutes
  private static final String configFile = "kevbot.properties";

  // This'll look something like
  // "jdbc:mysql://localhost:port/kevbot?user=kevbot&password=something"
  private final String connectionString;

  public ConnectionProvider() throws IOException {
    Properties config = new Properties();
    InputStream input = new FileInputStream(configFile);
    config.load(input);
    input.close();
    connectionString = config.getProperty("connectionString");
    ca.welbog.kevbot.log.Logger.debugStatic("Connection provider online.");
  }

  private boolean verifyConnection() {
    try {
      Date now = new Date();
      if ((now.getTime() - lastConnectionCreationTime.getTime()) > TIMEOUT_IN_MILLISECONDS) {
        close();
      }
      if (conn != null && !conn.isClosed()) {
        return true;
      }
      else {
        if (conn == null) {
          System.out.println("Creating new MySQL connection.");

          try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
          }
          catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
          }
        }
        else {
          System.out.println("Recreating MySQL connection.");
        }
        conn = DriverManager.getConnection(connectionString);
        lastConnectionCreationTime = new Date();
        return true;
      }
    }
    catch (SQLException ex) {
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }
    return false;
  }

  public static void dispose(Statement s, ResultSet r) {
    try {
      if (r != null) {
        r.close();
      }
      if (s != null) {
        s.close();
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
      if (conn != null) {
        conn.close();
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
    conn = null;
  }
  public synchronized Connection getObject() {
    if (verifyConnection()) {
      return conn;
    }
    else {
      return null;
    }
  }

}
