package ca.welbog.kevbot.responder.markov;

import java.util.ArrayList;
import java.util.List;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.persist.ConnectionProvider;
import ca.welbog.kevbot.persist.SingleFile;

public class ShowNotableResponder implements Responder {

  SQLWeightedMarkovByName markovby2;

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("shownotableusers");
    return new Documentation("Syntax: shownotableusers\n"
        + "Print the list of notable users, used by the guess function.\n"
        + "See also: rebuildnotableusers, guess.", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) {
      return null;
    }

    String body = r.getMessage();

    if (!body.matches("(?i)^(shownotableusers)\\s*$")) {
      return null;
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

    if (users.size() == 0) {
      return new Response(null, null, null);
    }

    String s = "";
    for (String u : users) {
      s += u + " ";
    }

    return new Response(r.getChannel(), s, Type.MESSAGE);
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
