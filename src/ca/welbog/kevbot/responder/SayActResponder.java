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

public class SayActResponder implements Responder {

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("act");
    aliases.add("say");
    return new Documentation(
        "Syntax: say|act <CHANNEL|USERNAME> <MESSAGE>\nSend a message or emote as this bot to a channel or username.",
        aliases);
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.getMessage().matches("(?i)^(say|act)\\s+\\S+\\s+\\S+.*")) {
      return null;
    }
    StringTokenizer tokenizer = new StringTokenizer(r.getMessage());
    String command = tokenizer.nextToken(); // Get rid of the "chgnick" part.
    String channel = tokenizer.nextToken();
    String message = "";
    while (tokenizer.hasMoreTokens()) {
      message += tokenizer.nextToken() + " ";
    }
    message = message.trim();
    Type type = Type.ACTION;
    if (command.equalsIgnoreCase("say")) {
      type = Type.MESSAGE;
    }
    return new Response(channel, message, type);
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
