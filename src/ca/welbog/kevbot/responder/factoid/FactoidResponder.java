package ca.welbog.kevbot.responder.factoid;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.persist.ConnectionProvider;
import ca.welbog.kevbot.service.Service;

public class FactoidResponder implements Responder {

  private DoubleSQL replies;
  private static final int MAX_LINE_LENGTH = 256;

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("factoid");
    return new Documentation("Syntax: factoid [MESSAGE]\n"
        + "Get a factoid from this bot's database by its base MESSAGE.\n"
        + "If MESSAGE is omitted, this function returns a random factoid.\n"
        + "Also gives details about who created the message and, if applicable, who deleted it.\n"
        + "If used in a private message, this will print the entire contents of a factoid, potentially writing across multiple lines.\n"
        + "See also: forget, reply.", aliases);
  }

  @Override
  public Response getResponse(Request r) {

    if (!r.canReply()) {
      return null;
    }

    if (!r.getMessage().matches("(?i)^factoid.*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(r.getMessage());
    String command = tokenizer.nextToken();
    if (!command.matches("(?i)^factoid$")) {
      return null;
    }

    String rest = "";
    while (tokenizer.hasMoreTokens()) {
      rest += tokenizer.nextToken() + " ";
    }
    rest = rest.trim();

    String factoid = "";
    if (rest.isEmpty()) {
      factoid = replies.getRandom();
    }
    else {
      factoid = replies.getDetails(rest);
    }
    if (factoid.trim().equals("")) {
      return new Response(r.getChannel(), "No factoids match that string.", Type.MESSAGE);
    }
    else {
      if (r.isPrivateMessage()) {
        factoid = breakUp(factoid);
      }
      return new Response(r.getChannel(), factoid, Type.MESSAGE);
    }
  }

  @Override
  public List<String> getRequiredServiceNames() {
    ArrayList<String> services = new ArrayList<String>();
    services.add("SQL");
    return services;
  }

  @Override
  public void addService(String name, Service service) {
    replies = new DoubleSQL((ConnectionProvider) service);
  }

  @Override
  public void close() {
  }

  // Helper method for breaking a very long string into a series of shorter
  // strings, each at least MAX_LINE_LENGTH long.
  private static String breakUp(String input) {
    int start = MAX_LINE_LENGTH;
    while (true) {
      // Find the first "|" after 'start' number of characters, and add a
      // newline after it.
      Pattern p = Pattern.compile("^.{" + start + "}[^|]*\\|", Pattern.DOTALL);
      Matcher m = p.matcher(input);
      if (m.find()) {
        input = m.replaceFirst("$0\n");
        start = m.end() + 1 + MAX_LINE_LENGTH; // m.end() is the size of the
                                               // group that was matched, and
                                               // we've added one character to
                                               // that
      }
      else {
        break;
      }
    }
    return input;
  }

  public static void main(String[] args) {
    String test = "dog is <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat"
        + " | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat | <say> cat";

    System.out.println(test);
    System.out.println(test.length());
    System.out.println(breakUp(test));
    System.out.println(breakUp(test).length());
    System.out.println(breakUp(test).split("\n").length);
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
