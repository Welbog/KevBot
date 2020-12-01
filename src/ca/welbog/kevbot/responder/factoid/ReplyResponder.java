package ca.welbog.kevbot.responder.factoid;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.persist.ConnectionProvider;
import ca.welbog.kevbot.service.Service;

public class ReplyResponder implements Responder {

  private DoubleSQL replies;

  public ReplyResponder() {
  }

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("reply");
    return new Documentation("Syntax: <MESSAGE> is/are <REPLY>\n"
        + "This bot is always 'listening' for the words 'is' and 'are'.\n"
        + "When it finds one, it records the message preceeding 'is' or 'are' and the reply following.\n"
        + "When it hears a recorded message, it will play back the corresponding reply.\n"
        + "EXAMPLE: 'This is fun' When this bot hears 'This' it will say 'This is fun'.\n"
        + "Advanced Syntax: [no] <MESSAGE> is/are [|/ALSO] [<SAY>/<ACT>] <REPLY> [`<SUBEXPRESSION>`]\n"
        + "NOTE: <SAY> and <ACT> are optional. Instead of saying a full sentence, when a reply contains <SAY> or <ACT>, KevBot will say or emote whatever is after those keywords.\n"
        + "The keyword '|' (or 'ALSO') indicates to KevBot to store multiple replies to MESSAGE.\n"
        + "EXAMPLE: 'yes is <say> no | <say> yes' will get the reply 'yes' or 'no' upon hearing 'yes.'\n"
        + "NOTE: the keyword 'no' indicates to KevBot to overwrite the reply to the message.\n"
        + "This bot will replace any instance of $who in the reply with the requestor's nick.\n"
        + "This bot will replace parameters in the <MESSAGE> with placeholders in the <REPLY>, such as 'punch $guy is <act> punches $guy.'\n"
        + "KevBot will interpret subexpressions in factoids that are delimited by backticks (`) as if they were their own commands when replying.\n"
        + "See also: factoid, forget, quiet, replyadv, recursion, status.", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) {
      return null;
    }

    Response reply = getReply(r.getMessage(), r.getSender(), r.getChannel());

    if (reply == null || !reply.hasContent()) { // If all else fails, look for a
                                                // reply.
      if (r.mustReply()) {
        return new Response(r.getChannel(),
            "I don't have anything matching " + strup(r.getMessage()) + ", " + r.getSender() + ".",
            Type.MESSAGE);
      }
      else {
        return null;
      }
    }

    reply.allowIteration();
    return reply;
  }

  private String strup(String s) {
    String strup = new String(s);
    if (strup.equals("")) {
      return "";
    }
    while (new Character(strup.charAt(strup.length() - 1)).toString()
        .matches("[`~!@#$%^&*_=|?/,. ]")) {
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
    return strup.trim();
  }

  private Response getReply(String body, String sender, String channel) {

    String reply = replies.getSingleReply(body); // Find the reply to the
                                                 // message

    if (reply.equalsIgnoreCase("")) {
      return null;
    } // If it wasn't there, who cares?

    String rrr = reply;
    StringTokenizer str = new StringTokenizer(rrr); // Tokenize that mofo.
    String first = "";
    String rest = "";
    String second = "";
    String temp = "";
    String temp2 = "";
    if (str.hasMoreTokens()) {
      first = str.nextToken();
      if (first.matches("[$]who.*")) {
        temp = first.substring(4);
        first = sender + temp;
      }
    }
    if (str.hasMoreTokens()) {
      second = str.nextToken();
      if (second.matches("[$]who.*")) {
        temp = second.substring(4);
        second = sender + temp;
      }
    }
    while (str.hasMoreTokens()) {
      temp = str.nextToken();
      if (temp.matches("[$]who.*")) {
        temp2 = temp.substring(4);
        temp = sender + temp2;
      }
      rest = rest + temp + " ";
    }
    rest = rest.trim();
    if (second.equalsIgnoreCase("<say>")) { // <say>/<act> are special cases.
      return new Response(channel, rest, Type.MESSAGE);
    }
    else if (second.equalsIgnoreCase("<act>")) {
      return new Response(channel, rest, Type.ACTION);
    }
    else {
      body = replies.getMessage(body);
      return new Response(channel, body + " " + first + " " + second + " " + rest, Type.MESSAGE);
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
