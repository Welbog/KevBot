package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.service.Service;

public class ChangeNicknameResponder implements Responder {

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("chgnick");
    return new Documentation("Syntax: chgnick <NEWNICK>\nChange this bot's nickname to <NEWNICK>.",
        aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (r.getMessage().trim().matches("(?i)^chgnick\\s+\\S+$")) {
      StringTokenizer tokenizer = new StringTokenizer(r.getMessage());
      tokenizer.nextToken(); // Get rid of the "chgnick" part.
      String detail = tokenizer.nextToken();
      return new Response(r.getChannel(), detail, Type.NICKNAME);
    }
    return null;
  }

  @Override
  public List<String> getRequiredServiceNames() {
    return null;
  }

  @Override
  public void addService(String name, Service service) {
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
