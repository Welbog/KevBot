package ca.welbog.kevbot.responder.factoid;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;

public class ForgetUnforgetResponder implements Responder {

  private DoubleSQL replies;

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("forget");
    aliases.add("unforget");
    return new Documentation("Syntax: forget <MESSAGE>\n"
        + "Remove a message from the database of replies. This is a soft delete.\n"
        + "Syntax: unforget <MESSAGE>\n" + "Re-add a soft deleted reply from the database.\n"
        + "NOTE: soft deleted replies become deleted permanently if a new factoid is created matching the same message.\n"
        + "See also: factoid, reply.", aliases);
  }

  @Override
  public Response getResponse(Request r) {

    if (!r.getMessage().matches("(?i)^(unforget|forget)\\s+\\S+.*")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(r.getMessage());
    String command = tokenizer.nextToken();

    boolean delete = false; // delete vs undelete
    if (command.matches("(?i)^forget$")) {
      delete = true;
    }

    String rest = "";
    while (tokenizer.hasMoreTokens()) {
      rest += tokenizer.nextToken() + " ";
    }
    rest = rest.trim();

    if (delete) {
      replies.delete(rest, r.getSender());
    }
    else {
      replies.undelete(rest);
    }

    return new Response(null, null, null);

  }

  @Override
  public List<String> getRequiredServiceNames() {
    ArrayList<String> services = new ArrayList<String>();
    services.add("SQL");
    return services;
  }
  
  public void setFactoidDatabase(DoubleSQL database) {
    replies = database;
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
