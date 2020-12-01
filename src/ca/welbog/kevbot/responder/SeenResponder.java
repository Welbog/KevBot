package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.persist.DoubleFile;
import ca.welbog.kevbot.persist.DoubleStorage;
import ca.welbog.kevbot.service.Service;

public class SeenResponder implements Responder {

  DoubleStorage db;

  public SeenResponder() {
    db = new DoubleFile("seena.txt", "seenb.txt");
  }

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("seen");
    return new Documentation(
        "Syntax: seen <USERNAME>\nFind the last time a user said something, and what was said.",
        aliases);
  }

  @Override
  public Response getResponse(Request r) {

    // First, always update the seen database.
    String who = r.getSender();
    String when = new Date().toString();
    String where = r.getMedium() + ":" + r.getChannel();
    String what = r.getMessage();
    db.addOver(who, who + " was last seen " + when + " in " + where + " saying \"" + what + "\"");
    db.write();

    if (r.canReply() && r.getMessage().matches("(?i)^seen\\s+\\S+.*$")) {
      StringTokenizer tokenizer = new StringTokenizer(r.getMessage());
      tokenizer.nextToken(); // Remove "seen"
      String user = tokenizer.nextToken();
      // Get all remaining tokens, as Discord names can contain spaces.
      while (tokenizer.hasMoreTokens()) {
        user += " " + tokenizer.nextToken();
      }
      user.replaceAll("^\"", ""); // Remove quotes at the start and end
      user.replaceAll("\"$", "");
      String reply = db.getReply(user);
      if (reply != null && !reply.trim().equals("")) {
        return new Response(r.getChannel(), reply, Type.MESSAGE);
      }
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
    db.close();
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
