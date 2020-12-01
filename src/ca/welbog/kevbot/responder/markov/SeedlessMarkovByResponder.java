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
import ca.welbog.kevbot.utils.NamesWithSpacesUtils;
import ca.welbog.kevbot.utils.NamesWithSpacesUtils.NameAndRemainder;

public class SeedlessMarkovByResponder implements Responder {

  SQLWeightedMarkovByName markovby1;
  SQLWeightedMarkovByName markovby2;

  public SeedlessMarkovByResponder() {
  }

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("seedlessmarkovby");
    aliases.add("seedlessmarkov2by");
    aliases.add("n2by");
    return new Documentation("Syntax: seedlessmarkovby|seedlessmarkov2by|n2by <USER> [SEED]\n"
        + "Use the reply database to produce a nonsense message immitating a specific <USER>. 'n2' is a shortcut for 'seedlessmarkov2'.\n"
        + "SEED is an optional phrase that will start the Markov chain which starts off the message, but SEED will not be in the generated output.\n"
        + "See also: guess, markov, reply, status, s2by", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) {
      return null;
    }

    String body = r.getMessage();

    if (!body.matches("(?i)^(n2by|seedlessmarkovby|seedlessmarkov2by)\\s+\\S+.*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    String command = tokenizer.nextToken();
    String user = tokenizer.nextToken();

    Vector<String> tokens = new Vector<String>();
    while (tokenizer.hasMoreTokens()) {
      tokens.add(tokenizer.nextToken());
    }

    // If the username has more than one word, rejig the name and vector.
    NameAndRemainder nameAndRemainder = NamesWithSpacesUtils.getNameWithSpaces(user, tokens);
    user = nameAndRemainder.name;
    tokens = nameAndRemainder.remainder;

    int order = 2;
    if (command.equalsIgnoreCase("markovby")) {
      order = 1;
    }

    String result = "";
    if (order == 1) {
      result = markovby1.generateSentence(user, tokens, false);
    }

    else if (order == 2) {
      result = markovby2.generateSentence(user, tokens, false);
    }

    if (result.trim().isEmpty()) {
      return new Response(null, null, null);
    }

    return new Response(r.getChannel(), result, Type.MESSAGE);
  }

  @Override
  public List<String> getRequiredServiceNames() {
    ArrayList<String> services = new ArrayList<String>();
    services.add("SQL");
    return services;
  }
  
  public void setOrder1ByDatabase(SQLWeightedMarkovByName database) {
    markovby1 = database;
  }
  public void setOrder2ByDatabase(SQLWeightedMarkovByName database) {
    markovby2 = database;
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
