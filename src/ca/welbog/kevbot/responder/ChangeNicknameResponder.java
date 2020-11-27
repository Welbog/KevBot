package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.service.Service;

public class ChangeNicknameResponder implements Responder {

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("chgnick");
    return new Documentation(
        "Syntax: chgnick <NEWNICK>\nChange this bot's nickname to <NEWNICK>.",
        aliases
    );
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

}
