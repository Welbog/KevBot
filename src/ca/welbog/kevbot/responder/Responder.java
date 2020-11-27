package ca.welbog.kevbot.responder;

import java.util.List;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.service.Service;

public interface Responder {
  public Documentation getDocumentation();
  public Response getResponse(Request r);
  public List<String> getRequiredServiceNames();
  public void addService(String name, Service service);
  public void close();
}
