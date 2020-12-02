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

public class S2Responder implements Responder {

  SQLStrangeMarkovByName markovby2;

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("s2by");
    return new Documentation("Syntax: s2by <USER> [SEED]\n"
        + "Use the reply database to produce a nonsense message immitating a specific <USER>.\n"
        + "SEED is an optional phrase that will start the Markov chain which starts off the message.\n"
        + "This is similar to the 'm2by' command, but it uses a weighting model that optimizes for the 'guess' command.\n"
        + "See also: guess, m2by", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) {
      return null;
    }

    String body = r.getMessage();

    if (!body.matches("(?i)^(s2by)\\s+\\S+.*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    tokenizer.nextToken(); // Remove the "s2by" from the input.
    String user = tokenizer.nextToken();

    Vector<String> tokens = new Vector<String>();
    while (tokenizer.hasMoreTokens()) {
      tokens.add(tokenizer.nextToken());
    }

    // If the username has more than one word, rejig the name and vector.
    NameAndRemainder nameAndRemainder = NamesWithSpacesUtils.getNameWithSpaces(user, tokens);
    user = nameAndRemainder.name;
    tokens = nameAndRemainder.remainder;

    String result = markovby2.generateSentence(2, user, tokens);

    if (result.trim().isEmpty()) {
      return new Response(null, null, null);
    }

    return new Response(r.getChannel(), result, Type.MESSAGE);
  }

  public void setS2Database(SQLStrangeMarkovByName database) {
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
