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
import ca.welbog.kevbot.persist.SingleFile;

public class GuessResponder implements Responder {

  SQLWeightedMarkovByName markovby2;

  public GuessResponder() {
  }

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("guess");
    return new Documentation("Syntax: guess <SENTENCE>\n"
        + "Guess who wrote the sentence provided by weighing it against the markov database.\n"
        + "NOTE: This operation works only on the notable user list.\n"
        + "See also: guessby, s2by, shownotableusers", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) {
      return null;
    }

    String body = r.getMessage();

    if (!body.matches("(?i)^(guess)\\s+\\S+.*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    tokenizer.nextToken(); // Remove the "guess" from the input.

    Vector<String> tokens = new Vector<String>();
    while (tokenizer.hasMoreTokens()) {
      tokens.add(tokenizer.nextToken());
    }

    SingleFile notable = new SingleFile("notable.txt"); // read-only notable
                                                        // user list
    List<String> users = notable.getAll();
    if (users.size() == 0) {
      markovby2.rebuildNotableUsers();
      users = markovby2.getNotableUsers();
      notable.addAll(users);
      notable.write();
    }
    String result = markovby2.determineProbability(tokens, users);

    if (result.trim().isEmpty()) {
      return new Response(null, null, null);
    }

    return new Response(r.getChannel(), result, Type.MESSAGE);
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
