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
import ca.welbog.kevbot.service.Service;

public class RebuildNotableResponder implements Responder {

  SQLWeightedMarkovByName markovby2;

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("rebuildotableusers");
    return new Documentation("Syntax: rebuildotableusers\n"
        + "Rebuilds the notable user list, which is used by the guess functionality.\n"
        + "WARNING: This operation can take up to five minutes and is not recommended to be run in periods of high activity.\n"
        + "See also: guess", aliases);
  }

  @Override
  public Response getResponse(Request r) {

    String body = r.getMessage();

    if (!body.matches("(?i)^(rebuildnotableusers)\\s*$")) {
      return null;
    }

    SingleFile notable = new SingleFile("notable.txt");
    markovby2.rebuildNotableUsers();
    List<String> users = markovby2.getNotableUsers();
    notable.deleteAll();
    notable.addAll(users);
    notable.write();

    if (users.size() == 0) {
      return new Response(null, null, null);
    }

    String s = "";
    for (String u : users) {
      s += u + " ";
    }

    return new Response(r.getChannel(), s, Type.MESSAGE);
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

    SingleFile notable = new SingleFile("notable.txt"); // Create the notable
                                                        // list if it doesn't
                                                        // exist at startup.
    List<String> users = notable.getAll();
    if (users.size() == 0) {
      markovby2.rebuildNotableUsers();
      users = markovby2.getNotableUsers();
      notable.deleteAll();
      notable.addAll(users);
      notable.write();
    }
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
