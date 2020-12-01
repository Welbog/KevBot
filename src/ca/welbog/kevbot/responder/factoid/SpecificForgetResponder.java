package ca.welbog.kevbot.responder.factoid;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;

public class SpecificForgetResponder implements Responder {

  private DoubleSQL replies;
  private static Pattern verbPattern = Pattern.compile("^(.*?)(\\s+(is|are)\\s+.*)$");
  private static Pattern commandPattern = Pattern.compile("(?i)^(forget-specific)\\s+(\\S+.*)$");

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("forget-specific");
    return new Documentation("Syntax: forget-specific <MESSAGE> is/are <REPLY>\n"
        + "Remove a specific reply from a multi-reply message from the database of replies. This is a hard delete.\n"
        + "Example: if 'dog is cat | is mouse' is a factoid, you can forget just the 'dog is mouse' part with 'forget-specific dog is mouse'.\n"
        + "See also: factoid, reply.", aliases);
  }

  @Override
  public Response getResponse(Request r) {

    Matcher commandMatcher = commandPattern.matcher(r.getMessage().trim());
    if (!commandMatcher.matches()) {
      return null;
    }
    String factoid = commandMatcher.group(2).trim();

    Matcher verbMatcher = verbPattern.matcher(factoid);
    if (!verbMatcher.matches()) {
      if (r.mustReply()) {
        return new Response(r.getChannel(), "This is not a valid factoid.", Type.MESSAGE);
      }
      else {
        return new Response(null, null, null);
      }
    }
    String message = verbMatcher.group(1).trim();
    String reply = verbMatcher.group(2).trim();

    String successMessage = replies.deleteSpecific(message, reply);
    if (r.mustReply()) {
      return new Response(r.getChannel(), successMessage, Type.MESSAGE);
    }
    else {
      return new Response(null, null, null);
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

  public static void main(String[] args) {
    String testString = "this is a test, is a TEST!";
    String testString2 = "thare aresuper thingare are area test!";
    String testString3 = "is a test";
    String testString4 = "a test is ";

    printStuff(testString);
    printStuff(testString2);
    printStuff(testString3);
    printStuff(testString4);

  }

  private static void printStuff(String input) {
    Matcher m = verbPattern.matcher(input);

    System.out.println("TESTING BEGINS FOR " + input);
    System.out.println(m.matches());
    if (m.matches()) {
      for (int i = 0; i < m.groupCount(); i++) {
        System.out.println("GROUP " + i + ": " + m.group(i));
      }
    }
    System.out.println("TESTING ENDS");
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
