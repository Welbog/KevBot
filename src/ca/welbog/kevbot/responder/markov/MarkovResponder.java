package ca.welbog.kevbot.responder.markov;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;

public class MarkovResponder implements Responder {

  SQLWeightedMarkov markov1;
  SQLWeightedMarkov markov2;

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("markov");
    aliases.add("markov2");
    aliases.add("m2");
    return new Documentation("Syntax: markov|markov2|m2 [SEED]\n"
        + "Use the reply database to produce a nonsense message. 'm2' is a shortcut for 'markov2'.\n"
        + "SEED is an optional phrase that will start the Markov chain which starts off the message.\n"
        + "See also: guess, markovby, reply, status, s2by", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) {
      return null;
    }

    String body = r.getMessage();

    if (!body.matches("(?i)^(m2|markov|markov2).*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    String command = tokenizer.nextToken();

    Vector<String> tokens = new Vector<String>();
    while (tokenizer.hasMoreTokens()) {
      tokens.add(tokenizer.nextToken());
    }

    int order = 2;
    if (command.equalsIgnoreCase("markov")) {
      order = 1;
    }

    String result = "";
    if (order == 1) {
      result = markov1.generateSentence(tokens, true);
    }

    else if (order == 2) {
      result = markov2.generateSentence(tokens, true);
    }

    if (result.trim().isEmpty()) {
      return new Response(null, null, null);
    }

    return new Response(r.getChannel(), result, Type.MESSAGE);
  }

  public void setOrder1Database(SQLWeightedMarkov database) {
    markov1 = database;
  }
  public void setOrder2Database(SQLWeightedMarkov database) {
    markov2 = database;
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
