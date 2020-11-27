package ca.welbog.kevbot.responder.markov;

import java.util.ArrayList;
import java.util.List;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.persist.ConnectionProvider;
import ca.welbog.kevbot.persist.SingleFile;
import ca.welbog.kevbot.responder.Responder;
import ca.welbog.kevbot.service.Service;

public class ShowNotableResponder implements Responder {

  SQLWeightedMarkovByName markovby2;
  
  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("shownotableusers");
    return new Documentation(
      "Syntax: shownotableusers\n" +
          "Print the list of notable users, used by the guess function.\n" +
          "See also: rebuildnotableusers, guess.",
      aliases
    );
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.canReply()) { return null; }
    
    String body = r.getMessage();
    
    if (!body.matches("(?i)^(shownotableusers)\\s*$")) { return null; } 
  
    SingleFile notable = new SingleFile("notable.txt"); // read-only notable user list
    List<String> users = notable.getAll();
    if (users.size() == 0) {
      markovby2.rebuildNotableUsers();
      users = markovby2.getNotableUsers();
      notable.addAll(users);
      notable.write();
    }
    
    if (users.size() == 0) { return new Response(null,null,null); }
    
    String s = "";
    for (String u : users) {
      s += u + " ";
    }
    
    return new Response(r.getChannel(),s,Type.MESSAGE);
  }

  @Override
  public List<String> getRequiredServiceNames() {
    ArrayList<String> services = new ArrayList<String>();
    services.add("SQL");
    return services;
  }

  @Override
  public void addService(String name, Service service) {
    markovby2 = new SQLWeightedMarkovByName((ConnectionProvider)service,2);
  }

  @Override
  public void close() {
  }


}
