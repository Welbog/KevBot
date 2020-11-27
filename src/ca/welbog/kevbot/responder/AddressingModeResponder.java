package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.List;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.communication.StatefulBot;
import ca.welbog.kevbot.service.Service;

public class AddressingModeResponder implements Responder {

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("quiet");
    return new Documentation(
        "Syntax: quiet\nToggle quiet mode.\nNOTE: when this bot is in quiet mode it will reply only to addresses.\nIt has a separate mode for every channel it occupies.\nSee also: addressing, status.",
        aliases
    );
  }

  @Override
  public Response getResponse(Request r) {
    String body = r.getMessage();
    if (!body.matches("^quiet$")) {
      return null;
    }
    
    StatefulBot.Mode newMode = StatefulBot.Mode.STANDARD;
    if (r.getMode() == StatefulBot.Mode.STANDARD) {
      newMode = StatefulBot.Mode.QUIET;
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

}
