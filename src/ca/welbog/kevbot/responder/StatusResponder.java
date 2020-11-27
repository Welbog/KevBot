package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.communication.StatefulBot;
import ca.welbog.kevbot.persist.ConnectionProvider;
import ca.welbog.kevbot.responder.factoid.DoubleSQL;
import ca.welbog.kevbot.responder.markov.SQLWeightedMarkov;
import ca.welbog.kevbot.service.Service;

public class StatusResponder implements Responder {

  private DoubleSQL replies;
  SQLWeightedMarkov markov1;
  private Date startTime;
  
  public StatusResponder() {
    startTime = new Date();
  }

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("status");
    return new Documentation(
      "Syntax: status\n" +
      "Check what mode this bot is in and check how many factoids it is referencing.\n" +
      "'Addressing required' is normally called 'quiet mode'.\n" +
      "See also: addressing, quiet.",
      aliases
    );
  }

  @Override
  public Response getResponse(Request r) {

    if (!r.canReply()) { return null; }
    
    if (!r.getMessage().matches("(?i)^status\\s*")) { return null; }
    
    String result = "Addressing is in ";
    if (r.getMode() == StatefulBot.Mode.STANDARD) {
      result += "optional";
    }
    else {
      result += "required";
    }
    result += " mode. ";
    result += "I am currently referencing " + (replies.getSize()) + " factoids. ";
    result += "The Markov database contains " + (markov1.size()) + " unique words. ";
    result += "I have been online since " + startTime.toString() + ".";
    
    return new Response(r.getChannel(),result,Type.MESSAGE);
  }

  @Override
  public List<String> getRequiredServiceNames() {
    ArrayList<String> services = new ArrayList<String>();
    services.add("SQL");
    return services;
  }

  @Override
  public void addService(String name, Service service) {
    replies = new DoubleSQL((ConnectionProvider)service);
    markov1 = new SQLWeightedMarkov((ConnectionProvider)service,1);
  }

  @Override
  public void close() {
  }


}
