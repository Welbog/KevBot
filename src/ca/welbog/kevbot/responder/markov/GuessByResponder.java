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
import ca.welbog.kevbot.persist.ConnectionProvider;
import ca.welbog.kevbot.service.Service;
import ca.welbog.kevbot.utils.NamesWithSpacesUtils;
import ca.welbog.kevbot.utils.NamesWithSpacesUtils.NameAndRemainder;

public class GuessByResponder implements Responder {

  SQLWeightedMarkovByName markovby2;

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("guessby");
    return new Documentation("Syntax: guessby <USER> <SENTENCE>\n"
        + "Determine how likely it was that a provided user said a provided sentence, using the markov database.\n"
        + "See also: guess, markov, s2by", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) {
      return null;
    }

    String body = r.getMessage();

    if (!body.matches("(?i)^(guessby)\\s+\\S+\\s+\\S+.*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    tokenizer.nextToken(); // Remove the "guessby" from the input.
    String user = tokenizer.nextToken();

    Vector<String> tokens = new Vector<String>();
    while (tokenizer.hasMoreTokens()) {
      tokens.add(tokenizer.nextToken());
    }

    // If the username has more than one word, rejig the name and vector.
    NameAndRemainder nameAndRemainder = NamesWithSpacesUtils.getNameWithSpaces(user, tokens);
    user = nameAndRemainder.name;
    tokens = nameAndRemainder.remainder;

    String result = markovby2.determineProbability(tokens, user);

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

  @Override
  public void addService(String name, Service service) {
    markovby2 = new SQLWeightedMarkovByName((ConnectionProvider) service, 2);
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
