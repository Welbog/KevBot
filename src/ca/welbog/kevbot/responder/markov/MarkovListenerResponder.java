package ca.welbog.kevbot.responder.markov;

import java.util.ArrayList;
import java.util.List;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;

public class MarkovListenerResponder implements Responder {

  SQLWeightedMarkovByName markovby1;
  SQLWeightedMarkovByName markovby2;
  SQLWeightedMarkov markov1;
  SQLWeightedMarkov markov2;

  @Override
  public Documentation getDocumentation() {
    return null;
  }

  @Override
  public Response getResponse(Request r) {

    markov1.addSentence(r.getMessage());
    markov2.addSentence(r.getMessage());
    markovby1.addSentence(r.getSender(), r.getMessage());
    markovby2.addSentence(r.getSender(), r.getMessage());

    return null;
  }

  @Override
  public List<String> getRequiredServiceNames() {
    ArrayList<String> services = new ArrayList<String>();
    services.add("SQL");
    return services;
  }


  public void setOrder1Database(SQLWeightedMarkov database) {
    markov1 = database;
  }
  public void setOrder2Database(SQLWeightedMarkov database) {
    markov2 = database;
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
