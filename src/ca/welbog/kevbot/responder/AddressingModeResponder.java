package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.List;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.service.Service;

public class AddressingModeResponder implements Responder {

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("quiet");
    return new Documentation(
        "Syntax: quiet\nToggle quiet mode.\nNOTE: when this bot is in quiet mode it will reply only to addresses.\nIt has a separate mode for every channel it occupies.\nSee also: addressing, status.",
        aliases);
  }

  @Override
  public Response getResponse(Request r) {
    String body = r.getMessage();
    if (!body.matches("^quiet$")) {
      return null;
    }

    AddressingMode newMode = AddressingMode.STANDARD;
    if (r.getMode() == AddressingMode.STANDARD) {
      newMode = AddressingMode.QUIET;
    }
    return new Response(r.getChannel(), newMode.name(), Type.MODE);
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
