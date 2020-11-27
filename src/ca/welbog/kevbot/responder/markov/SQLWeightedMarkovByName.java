package ca.welbog.kevbot.responder.markov;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import ca.welbog.kevbot.persist.ConnectionProvider;

public class SQLWeightedMarkovByName {
	private int _order = 1;
	private final int MAX_CHAIN_LENGTH = 36;
	private final int MIN_CHAIN_LENGTH = 12;
	private Random random;
	private ConnectionProvider provider = null;
	
	
	/*
	 * CREATE TABLE weightedmarkovbyuser (
	 *   user varchar(40) not null, 
	 *   seed varchar(230) not null, 
	 *   word varchar(230) not null, 
	 *   weight bigint unsigned not null, 
	 *   primary key (user, seed, word)
	 * );
	 * CREATE INDEX weightedmarkovbyuser_user ON weightedmarkovbyuser (user);
	 */
	
	public SQLWeightedMarkovByName(ConnectionProvider provider, int order) {
		_order = order;
		random = new Random();
		this.provider = provider;
	}
	
	private Connection getConnection() {
	  return (Connection)provider.getObject();
	}
	
	public synchronized int size() {
		try {
      Connection conn = getConnection();
			   Statement statement = conn.createStatement();
			   ResultSet rs = statement.executeQuery("SELECT COUNT(1) FROM weightedmarkovbyuser;");
			   //ResultSet rs = statement.executeQuery("SELECT * FROM markov;");
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
	
	public synchronized String generateSentence(String user, Vector<String> tokens, boolean useSeedInOutput) {

		Queue<String> baseQueue = new LinkedList<String>();
		Queue<String> startQueue = new LinkedList<String>();
		for (int i = 0; i < _order; i++) {
			baseQueue.add("");
			startQueue.add("");
		}
		for (int i = tokens.size()-_order; i < tokens.size(); i++) {
			if (i >= 0) {
				baseQueue.poll();
				baseQueue.add((String)tokens.get(i));
			}
		}
		for (int i = 0; i < tokens.size(); i++) {
			startQueue.add((String)tokens.get(i));
		}
		
		StringBuilder result = new StringBuilder();
		if (useSeedInOutput) {
		  String start = queueToToken(startQueue).trim();
		  result.append(start);
		}
		
		for (int i = 0; i < MAX_CHAIN_LENGTH; i++) {
			//System.out.println("Iteration: " + i);
			//System.out.println("Current: " + current.getValue());
			//System.out.println("Current children: " + current.childCount());
			//System.out.println("Result: " + result.toString());
			String base = queueToToken(baseQueue);
			String next = retrieveNext(user,base);
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
	
	public synchronized String generateSentence(String user) {
		Vector<String> v = new Vector<String>();
		for (int i = 1; i < _order; i++) {
			v.add("");
		}
		return generateSentence(user,v, true);
	}
	public synchronized void addSentence(String user, String sentence) {
		if (sentence == null) { return; }
		if (sentence.equals("")) { return; }
		StringTokenizer tokenizer = new StringTokenizer(sentence);
		if (tokenizer.countTokens() <= 1) { return; }
		
		Queue<String> currentToken = new LinkedList<String>();
		for (int i = 0; i < _order; i++) {
			currentToken.add("");
		}
		
		while (tokenizer.hasMoreTokens()) {
			String seed = queueToToken(currentToken);
			String word = tokenizer.nextToken();
			writePair(user,seed,word);
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
	public static void main(String[] args) {
		SQLWeightedMarkovByName m1 = new SQLWeightedMarkovByName(1);
		SQLWeightedMarkovByName m2 = new SQLWeightedMarkovByName(2);
		
		System.out.println(m1.generateSentence("Inferno"));
		
		List<String> users = Arrays.asList(new String[] {"Inferno","Nick","Agent","Blick_Winkel","Abent"});		
		long time = System.currentTimeMillis();
		System.out.println(m2.determineProbability("Nick is a butt.",users));
		System.out.println(System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
    System.out.println(m2.determineProbability("I am an archeologist, but I said frogs aren't birds.",users));
    System.out.println(System.currentTimeMillis() - time);
    time = System.currentTimeMillis();
    System.out.println(m2.determineProbability("Nick is a butt.",users));
    System.out.println(System.currentTimeMillis() - time);
    time = System.currentTimeMillis();
    System.out.println(m2.determineProbability("My favourite dog ate every sandwich in the universe.",users));
    System.out.println(System.currentTimeMillis() - time);
    time = System.currentTimeMillis();
    System.out.println(m2.determineProbability("you are a butt",users));
    System.out.println(System.currentTimeMillis() - time);
    time = System.currentTimeMillis();
    System.out.println(m2.determineProbability("sounds like your new function was a failure!",users));
    System.out.println(System.currentTimeMillis() - time);
    System.out.println(m2.determineProbability("I may have up the math.",users));
    System.out.println(System.currentTimeMillis() - time);
	}
	*/
	

  private synchronized String join(List<String> tokens) {
    String r = "";
    for (String t : tokens) {
      r += t + " ";
    }
    return r.trim();
  }
  
  /* For testing */
  /*
  private String determineProbability(String sentence, List<String> users) {
    List<String> list = new LinkedList<String>();
    StringTokenizer tokenizer = new StringTokenizer(sentence);
    while (tokenizer.hasMoreTokens()) {
      list.add(tokenizer.nextToken());
    }
    return determineProbability(list,users);
  }
  */
	
	public synchronized String determineProbability(List<String> tokens, List<String> users) {
	  if (tokens == null) { return ""; }
	  if (tokens.size() <= 1) { return ""; }
    
	  String bestUser = "";
	  double bestProbability = 0.0;
	  Hashtable<String,Double> globalTable = determineProbability(tokens);
	  for (String user : users) {
	    double current = determineProbabilityByName(tokens, user, globalTable);
	    //System.out.println("User: " + user + ", weight: " + current);
	    if (current > bestProbability) {
	      bestUser = user;
	      bestProbability = current;
	    }
	  }
	  if (bestUser.equalsIgnoreCase("")) {
	    return "I have no idea who said, \"" + join(tokens).trim() + "\"";
	  }
	  else {
	    DecimalFormat format = new DecimalFormat("#.##");
	    return "I am " + format.format(bestProbability*100.0) + "% confident that " + bestUser + " said, \"" + join(tokens).trim() + "\"";
	  }
	}
	
	public synchronized String determineProbability(List<String> tokens, String user) {
    if (tokens == null) { return ""; }
    if (tokens.size() <= 1) { return ""; }
    
    return determineProbability(tokens,user,determineProbability(tokens));
	}
	
	private synchronized String determineProbability(List<String> tokens, String user, Hashtable<String,Double> globalTable) {
    if (tokens == null) { return ""; }
    if (tokens.size() <= 1) { return ""; }
    
    double weight = determineProbabilityByName(tokens, user, globalTable);
    DecimalFormat format = new DecimalFormat("#.##");
    return "I am " + format.format(weight*100.0) + "% confident that " + user + " said, \"" + join(tokens).trim() + "\"";
	}
	
	/* Returns a hash table indexed by GLOBAL + seed, with the weight of that seed/word pair */
	private synchronized Hashtable<String,Double> determineProbability(List<String> tokens) {
    if (tokens == null) { return null; }
    if (tokens.size() <= 1) { return null; }
    
    Hashtable<String,Double> globalTable = new Hashtable<String,Double>();
    
    Queue<String> currentToken = new LinkedList<String>();
    for (int i = 0; i < _order; i++) {
      currentToken.add("");
    }
    
    for (String word : tokens) {
      String seed = queueToToken(currentToken);
      //System.out.println("Getting weight for " + user + "," + seed + "," + word);
      
      globalTable.put(seed, getWeight(seed,word));
      currentToken.poll(); // nextToken is now of length order-1
      currentToken.add(word); // nextToken is now of length order
    }
    return globalTable;
	}
	
	/* Returns a hash table indexed by user + seed, with the weight of that seed/word pair for that user */
	private synchronized double determineProbabilityByName(List<String> tokens, String user, Hashtable<String,Double> globalTable) {
    if (tokens == null) { return 0.0; }
    if (tokens.size() <= 1) { return 0.0; }
    
    Queue<String> currentToken = new LinkedList<String>();
    for (int i = 0; i < _order; i++) {
      currentToken.add("");
    }
    
    double weight = 0.0;
    for (String word : tokens) {
      String seed = queueToToken(currentToken);
      //System.out.println("Getting weight for " + user + "," + seed + "," + word);
      if (globalTable.get(seed) != 0.0 && !Double.isNaN(globalTable.get(seed))) {
        weight += getWeightByUser(user,seed,word) / globalTable.get(seed);
      }
      currentToken.poll(); // nextToken is now of length order-1
      currentToken.add(word); // nextToken is now of length order
    }
    return weight/(double)tokens.size();
	}
	
	private volatile List<String> notableUsers = null;
	public synchronized void rebuildNotableUsers() {
	  System.out.println("Rebuilding notable user list. This could take some time.");
	  notableUsers = null;
	  getNotableUsers();
	  System.out.println("Notable user list rebuilt.");
	}
	public synchronized List<String> getNotableUsers() {
	   //System.out.println("Getting notable users");
	   
	   if (notableUsers != null) { return notableUsers; }

    try {
      Connection conn = getConnection();
         Statement statement = conn.createStatement();
         
         // Find all users that have at least a certain number of word pairs
         ResultSet countSet = statement.executeQuery("select user, count(1) from weightedmarkovbyuser group by user order by 2 desc limit 20;");
         countSet.beforeFirst();
         List<String> users = new LinkedList<String>();
         while (countSet.next()) {
           //System.out.println("Added" + countSet.getString("user"));
           users.add(countSet.getString("user"));
         }
         countSet.close();
         statement.close();
         notableUsers = users;
         return users;
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
    return null;
	}

  private synchronized void writePair(String user, String seed, String word) {
    try {
      Connection conn = getConnection();
         Statement statement = conn.createStatement();
         seed = seed.replaceAll("\\\\", "\\\\\\\\");
         word = word.replaceAll("\\\\", "\\\\\\\\");
         seed = seed.replaceAll("'", "\\\\'");
         word = word.replaceAll("'", "\\\\'");
         user = user.replaceAll("\\\\", "\\\\\\\\");
         user = user.replaceAll("'", "\\\\'");
         
         // Need to confirm if the pair exists first,
         //   If so, increment its weight.
         //   If not, insert new pair with weight = 1
         ResultSet countSet = statement.executeQuery("SELECT COUNT(*) FROM weightedmarkovbyuser WHERE user = '"+user+"' AND seed = '"+seed+"' AND word = '"+word+"';");
         countSet.first();
         int count = countSet.getInt(1);
         countSet.close();
         if (count > 0) {
           statement.execute("UPDATE weightedmarkovbyuser SET weight = weight+1 WHERE user = '"+user+"' AND seed = '"+seed+"' AND word = '"+word+"';");
         }
         else {
           statement.execute("INSERT INTO weightedmarkovbyuser (user, seed, word, weight) VALUES ('"+user+"','"+seed+"','"+word+"',1);");
         }
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
  
  private synchronized double getWeight(String seed, String word) {
    try {
      Connection conn = getConnection();
         Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
         seed = seed.replaceAll("\\\\", "\\\\\\\\");
         seed = seed.replaceAll("'", "\\\\'");
         word = word.replaceAll("\\\\", "\\\\\\\\");
         word = word.replaceAll("'", "\\\\'");
         
         /*
         ResultSet countSet = statement.executeQuery("SELECT SUM(weight) FROM weightedmarkov WHERE user = '"+user+"' AND seed = '"+seed+"';");
         countSet.first();
         int count = countSet.getInt(1);
         countSet.close();
         
         if (count == 0) {
           statement.close();
           return 0.0;
         }
         */
         ResultSet rs = statement.executeQuery("SELECT weight FROM weightedmarkov WHERE seed = '"+seed+"' AND word = '"+word+"';");
         
         if (rs == null) {
           statement.close();
           return 0.0;
         }
         if (!rs.first()) {
           rs.close();
           statement.close();
           return 0.0;
         }
         rs.first();
         long weight = rs.getLong("weight");
           
         rs.close();
         statement.close();
         return (double)weight;
         //return (double)weight/(double)count;
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
    return 0.0;
  }
  
  private synchronized double getWeightByUser(String user, String seed, String word) {
    try {
      Connection conn = getConnection();
         Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
         seed = seed.replaceAll("\\\\", "\\\\\\\\");
         seed = seed.replaceAll("'", "\\\\'");
         user = user.replaceAll("\\\\", "\\\\\\\\");
         user = user.replaceAll("'", "\\\\'");
         word = word.replaceAll("\\\\", "\\\\\\\\");
         word = word.replaceAll("'", "\\\\'");
         
         /*
         ResultSet countSet = statement.executeQuery("SELECT SUM(weight) FROM weightedmarkovbyuser WHERE user = '"+user+"' AND seed = '"+seed+"';");
         countSet.first();
         int count = countSet.getInt(1);
         countSet.close();
         
         if (count == 0) {
           statement.close();
           return 0.0;
         }
         */
         ResultSet rs = statement.executeQuery("SELECT weight FROM weightedmarkovbyuser WHERE user = '"+user+"' AND seed = '"+seed+"' AND word = '"+word+"';");
         
         if (rs == null) {
           statement.close();
           return 0.0;
         }
         if (!rs.first()) {
           rs.close();
           statement.close();
           return 0.0;
         }
         rs.first();
         long weight = rs.getLong("weight");
           
         rs.close();
         statement.close();
         return (double)weight;
         //return (double)weight/(double)count;
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
    return 0.0;
  }
  
  private synchronized String retrieveNext(String user, String seed) {
    try {
      Connection conn = getConnection();
         Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
         seed = seed.replaceAll("\\\\", "\\\\\\\\");
         seed = seed.replaceAll("'", "\\\\'");
         user = user.replaceAll("\\\\", "\\\\\\\\");
         user = user.replaceAll("'", "\\\\'");
         
         ResultSet countSet = statement.executeQuery("SELECT SUM(weight) FROM weightedmarkovbyuser WHERE user = '"+user+"' AND seed = '"+seed+"';");
         countSet.first();
         int count = countSet.getInt(1);
         countSet.close();
         
         if (count == 0) {
           statement.close();
           return "";
         }
         
         int target = random.nextInt(count);
         ResultSet rs = statement.executeQuery("SELECT word, weight FROM weightedmarkovbyuser WHERE user = '"+user+"' AND seed = '"+seed+"';");
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
           if (target >= 0 && target < weight) { result = rs.getString("word"); break; }
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
        return "";
    }
    catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
      return "";
    }
  }
	
}
