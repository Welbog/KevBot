package ca.welbog.kevbot.core;

import java.util.List;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;

public interface Responder {
  public Documentation getDocumentation();

  public Response getResponse(Request r);

  public List<String> getRequiredServiceNames();

  public void close();

  public boolean isAdminOnly();

  public void setAdminOnly(boolean value);

  public ResponderType getResponderType();

  public void setResponderType(ResponderType type);
}
