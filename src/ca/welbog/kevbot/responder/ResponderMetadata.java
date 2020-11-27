package ca.welbog.kevbot.responder;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;

public class ResponderMetadata {
  public Responder responder;
  public boolean admin = false;
  public Type type; 
  public enum Type {
    CORE,
    RECURSIVE
  }
  public ResponderMetadata(Responder r, boolean isAdminOnly, Type type) {
    this.responder = r;
    this.admin = isAdminOnly;
    this.type = type;
  }
  
  public Responder getResponder() {
    return responder;
  }
  public boolean isAdminOnly() {
    return admin;
  }
  public Type getType() {
    return type;
  }
  public Response getResponse(Request request) {
    return responder.getResponse(request);
  }
  public Documentation getDocumentation() {
    return responder.getDocumentation();
  }
}
