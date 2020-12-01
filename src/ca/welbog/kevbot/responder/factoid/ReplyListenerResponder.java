package ca.welbog.kevbot.responder.factoid;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;

public class ReplyListenerResponder implements Responder {

  private DoubleSQL replies;
  private static final Pattern VERB_PATTERN = Pattern.compile("^(is|are)\\s+");

  @Override
  public Documentation getDocumentation() {
    return null;
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.getMessage().matches("(?i)^.*\\s+(is|are)\\s+.*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(r.getMessage());

    String first = tokenizer.nextToken();
    Vector<String> remainder = new Vector<String>();
    while (tokenizer.hasMoreTokens()) {
      remainder.add(tokenizer.nextToken());
    }

    if (containsIs(first, remainder) < 0) {
      return null;
    }

    String reply = doAllTheWork(first, remainder, containsIs(first, remainder), r.getSender());
    if (reply == null || reply.isEmpty()) {
      return null;
    }

    if (r.mustReply()) {
      return new Response(r.getChannel(), reply, Type.MESSAGE);
    }
    return new Response(null, null, null);
  }

  /**
   * Helper methods returns the location of "is" in a Vector.
   */
  private int containsIs(String firstword, Vector<String> v) {
    int i = -1;
    int start = 0;
    if (firstword.matches("no[,.]?")) {
      start = 1;
    }
    for (int x = start; x < v.size() - 1; x++) {
      if ((((String) v.get(x)).equalsIgnoreCase("is"))
          || (((String) v.get(x)).equalsIgnoreCase("are"))) {
        i = x;
        return i;
      }
    }
    return i;
  }

  /**
   * Create and store a message/reply pair.
   * 
   * @param f
   *          The first part of the message (not included in the Vector).
   * @param m
   *          The Vector containing the rest of the message.
   * @param v
   *          The location of the word "is."
   * @param channel
   *          The channel to talk to.
   * @param a
   *          Addressed?
   */
  private String doAllTheWork(String f, Vector<String> r, int v, String user) {
    String m = f + " ";
    String mf = "";
    String temp = "";
    boolean multiple = false;
    for (int i = 0; i < v; i++) {
      temp = (String) r.get(i);
      m = m + temp + " ";
      mf = mf + temp + " ";
    }
    m = m.trim();
    mf = mf.trim();
    temp = "";
    String arrr = "";
    String verb = r.get(v);
    if (replies.exists(m.trim())) {
      if (r.get(v + 1).equalsIgnoreCase("|")) {
        multiple = true;
      }
      if (r.get(v + 1).equalsIgnoreCase("also")) {
        multiple = true;
        r.set(v + 1, "|");
      }
    }
    for (int i = (multiple) ? (v + 2) : (v); i < r.size(); i++) {
      temp = r.get(i);
      arrr = arrr + temp + " ";
    }
    System.out.println(arrr);
    if (replies.exists(m.trim())) {
      if (multiple) {
        addMultiples(m.trim(), arrr.trim(), user, verb);
        return "Okay.";
      }
      return "... But " + replies.getMessage(m.trim()) + " " + replies.getFullReply(m.trim());
    }
    else if (f.toLowerCase().matches("no[,.]?")) {
      String tempus = mf.trim();
      if (!tempus.equals("")) {
        replies.delete(mf, user);
        addMultiples(mf.trim(), arrr.trim(), user, verb);
        return "Okay.";
      }
      return "";
    }
    else {
      addMultiples(m.trim(), arrr.trim(), user, verb);
      return "Okay.";
    }
  }

  private void addMultiples(String message, String delimitedReplies, String user, String verb) {

    String[] multis = delimitedReplies.split("\\s+\\|\\s+"); // Split the
                                                             // request into
                                                             // multiples
    for (String individualReply : multis) {
      if (individualReply.trim().equals("")) {
        continue;
      }
      Matcher matcher = VERB_PATTERN.matcher(individualReply.trim());
      if (matcher.find()) {
        replies.append(message.trim(), individualReply.trim(), user);
      }
      else {
        replies.append(message.trim(), verb + " " + individualReply.trim(), user);
      }
    }
  }

  @Override
  public List<String> getRequiredServiceNames() {
    ArrayList<String> services = new ArrayList<String>();
    services.add("SQL");
    return services;
  }
  
  public void setFactoidDatabase(DoubleSQL database) {
    replies = database;
  }

  @Override
  public void close() {
  }

  private boolean isAdminOnly = false;
  private ResponderType responderType = ResponderType.CORE;

  @Override
  public boolean isAdminOnly() {
    return isAdminOnly;
  }

  @Override
  public void setAdminOnly(boolean value) {
    isAdminOnly = value;
  }

  @Override
  public ResponderType getResponderType() {
    return responderType;
  }

  @Override
  public void setResponderType(ResponderType type) {
    responderType = type;
  }
}
