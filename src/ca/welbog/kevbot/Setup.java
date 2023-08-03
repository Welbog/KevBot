package ca.welbog.kevbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Create MySQL structures needed by KevBot.
 */
public class Setup {

  private static final String configFile = "kevbot.properties";

  private static final String MESSAGE_TABLE = "CREATE TABLE `Message` (\n"
      + "  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" + "  `cleanmessage` text NOT NULL,\n"
      + "  `message` text NOT NULL,\n" + "  `createuser` varchar(255) DEFAULT NULL,\n"
      + "  `createdate` datetime DEFAULT NULL,\n" + "  `deleted` tinyint(1) NOT NULL DEFAULT '0',\n"
      + "  `deleteuser` varchar(255) DEFAULT NULL,\n" + "  `deletedate` datetime DEFAULT NULL,\n"
      + "  `regex` tinyint(4) NOT NULL DEFAULT '0',\n" + "  PRIMARY KEY (`id`),\n"
      + "  FULLTEXT KEY `cleanmessage` (`cleanmessage`)\n" + ") ENGINE=MyISAM;";
  private static final String REPLY_TABLE = "CREATE TABLE `Reply` (\n"
      + "  `messageid` int(10) unsigned NOT NULL DEFAULT '0',\n"
      + "  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" + "  `reply` text NOT NULL,\n"
      + "  PRIMARY KEY (`messageid`,`id`)\n" + ") ENGINE=MyISAM;";
  private static final String MARKOV_TABLE = "CREATE TABLE `weightedmarkov` (\n"
      + "  `seed` varchar(250) NOT NULL DEFAULT '',\n"
      + "  `word` varchar(250) NOT NULL DEFAULT '',\n"
      + "  `weight` bigint(20) unsigned NOT NULL DEFAULT '0',\n" + "  PRIMARY KEY (`seed`,`word`)\n"
      + ") ENGINE=MyISAM;";
  private static final String MARKOV_BY_USER_TABLE = "CREATE TABLE `weightedmarkovbyuser` (\n"
      + "  `user` varchar(40) NOT NULL DEFAULT '',\n"
      + "  `seed` varchar(230) NOT NULL DEFAULT '',\n"
      + "  `word` varchar(230) NOT NULL DEFAULT '',\n"
      + "  `weight` bigint(20) unsigned NOT NULL DEFAULT '0',\n"
      + "  PRIMARY KEY (`user`,`seed`,`word`),\n" + "  KEY `weightedmarkovbyuser_user` (`user`)\n"
      + ") ENGINE=MyISAM;";

  public static void main(String[] args) throws IOException, SQLException, InstantiationException,
      IllegalAccessException, ClassNotFoundException {

    Properties config = new Properties();
    InputStream input = new FileInputStream(configFile);
    config.load(input);
    input.close();
    String connectionString = config.getProperty("adminConnectionString");
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection conn = DriverManager.getConnection(connectionString);
    Statement statement = null;

    try {
      statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);;
      statement.execute(MESSAGE_TABLE);
      statement.execute(REPLY_TABLE);
      statement.execute(MARKOV_TABLE);
      statement.execute(MARKOV_BY_USER_TABLE);
    }
    finally {
      statement.close();
      conn.close();
    }
  }

}
