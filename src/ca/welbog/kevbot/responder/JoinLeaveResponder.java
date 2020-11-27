package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.service.Service;

public class JoinLeaveResponder implements Responder {

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("leave");
    aliases.add("join");
    return new Documentation(
        "Syntax: join|leave <CHANNEL>\nMake this bot join or leave a specific channel.",
        aliases
    );
  }

  @Override
  public Response getResponse(Request r) {
    String body = r.getMessage();
    if (!body.matches("(?i)^(join|leave)\\s+\\S+\\s*$")) {
      return null;
    }
    
    StringTokenizer tokenizer = new StringTokenizer(body);
    String command = tokenizer.nextToken();
    String channel = tokenizer.nextToken();
    
    if (!channel.startsWith("#")) { channel = "#" + channel; }
    
    if (command.equalsIgnoreCase("join")) { //join
      return new Response(null, channel, Type.JOIN);
    }
    else { // leave
      return new Response(null, channel, Type.LEAVE);
    }
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
