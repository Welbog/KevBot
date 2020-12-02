package ca.welbog.kevbot.responder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ca.welbog.kevbot.KevBot;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.log.Logger;

public class HelpResponder implements Responder {
  private Map<String, Responder> responders = new HashMap<>();

  public HelpResponder(List<Responder> responders) {
    for (Responder r : responders) {
      Documentation doc = r.getDocumentation();
      if (doc != null) {
        for (String alias : doc.getAliases()) {
          this.responders.put(alias, r);
        }
      }
    }
    Logger.debugStatic(this.responders.toString());
  }

  @Override
  public Response getResponse(Request r) {
    if (!r.isPrivateMessage()) {
      return null;
    }

    if (!r.getMessage().startsWith("help")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(r.getMessage());
    String help = tokenizer.nextToken();
    if (!help.trim().equalsIgnoreCase("help")) {
      return null;
    }

    String detail = null;
    if (tokenizer.hasMoreTokens()) {
      detail = tokenizer.nextToken();
    }
    String details = getDetails(detail, r.getNickname());
    String body = null;
    if (details == null) {
      String commandList = getCommandList();
      body = "KevBot's commands list (KevBot " + KevBot.VERSION + ")\n";
      body += commandList + "\n";
      body += "* indicates admin-only commands.\n";
      body += "For more information about each command, type 'help <COMMAND>' where <COMMAND> is from the list above.";
    }
    else {
      body = "";
      if (responders.get(detail) == null) {
        if (detail.equalsIgnoreCase("admin") || detail.equalsIgnoreCase("unadmin")
            || detail.equalsIgnoreCase("ignoreuser") || detail.equalsIgnoreCase("unignoreuser")
            || detail.equalsIgnoreCase("ignoreword") || detail.equalsIgnoreCase("unignoreword")
            || detail.equalsIgnoreCase("chgnick") || detail.equalsIgnoreCase("join")
            || detail.equalsIgnoreCase("leave") || detail.equalsIgnoreCase("quit")) {
          body += "This command is admin-only\n";
        }
      }
      else {
        if (responders.get(detail).isAdminOnly()) {
          body += "This command is admin-only\n";
        }
      }
      body += details;
    }

    return new Response(r.getChannel(), body, Type.MESSAGE);

  }

  private String getCommandList() {
    List<String> aliases = new ArrayList<String>();
    for (Map.Entry<String, Responder> entry : responders.entrySet()) {
      String alias = entry.getKey();
      if (entry.getValue().isAdminOnly()) {
        alias += "*";
      }
      aliases.add(alias);
    }
    aliases.add("admin*");
    aliases.add("unadmin*");
    aliases.add("ignoreuser*");
    aliases.add("unignoreuser*");
    aliases.add("ignoreword*");
    aliases.add("unignoreword*");
    aliases.add("addressing");
    aliases.add("recursion");
    aliases.add("quit*");
    java.util.Collections.sort(aliases);

    String list = null;
    for (int i = 0; i < aliases.size(); i++) {
      if (i == 0) {
        list = aliases.get(i);
      }
      else {
        list += ", ";
        list += aliases.get(i);
      }
    }
    return list;
  }

  private String getDetails(String alias, String name) {
    if (alias == null) {
      return null;
    }
    Responder responder = responders.get(alias);
    if (responder != null) {
      return responder.getDocumentation().getBody();
    }

    if (alias.equalsIgnoreCase("addressing")) {
      return "Syntax: " + name + "[?\\/!1~`.,:;<>]*\n" + "Ignore quiet mode, address " + name
          + ".\n" + "See also: quiet, status.";
    }
    else if (alias.equalsIgnoreCase("admin")) {
      return "Syntax: admin <USERNAME>\n" + "Allow a user access to " + name
          + "'s special commands.\n" + "See also: ignore, unadmin, unignore.";
    }
    else if (alias.equalsIgnoreCase("chgnick")) {
      return "Syntax: chgnick <NEWNICK>\n" + "Change " + name + "'s nickname to a new nickname.";
    }
    else if (alias.equalsIgnoreCase("ignoreuser")) {
      return "Syntax: ignoreuser <USERNAME>\n" + "Add a user to the list of users " + name
          + " ignores.\n" + "See also: admin, unadmin, unignoreuser.";
    }
    else if (alias.equalsIgnoreCase("ignoreword")) {
      return "Syntax: ignoreword <WORD>\n" + "Add a word to the list of words " + name
          + " ignores.\n" + "See also: ignoreuser, unignoreword.";
    }
    else if (alias.equalsIgnoreCase("join")) {
      return "Syntax: join <CHANNEL>\n" + "Make " + name + " join a channel.\n"
          + "See also: leave.";
    }
    else if (alias.equalsIgnoreCase("leave")) {
      return "Syntax: leave <CHANNEL>\n" + "Make " + name + " leave a channel.\n"
          + "See also: join.";
    }
    else if (alias.equalsIgnoreCase("quit")) {
      return "Syntax: quit [MESSAGE]\n" + "Make " + name + " quit this IRC server and shut down.";
    }
    else if (alias.equalsIgnoreCase("recursion")) {
      return "Syntax: <MESSAGE> is/are <REPLY> `<SUBEXPRESSION>`\n" + name
          + " will interpret subexpressions in factoids that are delimited by backticks (`).\n"
          + "For example, if a factoid is set like this, 'mydate is `date`', when a user says 'date' thereafter, KevBot will interpret the part between the backticks as an independent command and put the result (in this case, the current time) in the reply.\n"
          + "The result would be something like 'mydate is " + new Date().toString() + "'.\n"
          + "NOTE: backticks will be ignored if there are dollar signs in front of them.\n"
          + "NOTE: dollar signs will be ignored if there are dollar signs in front of them.\n"
          + "See also: factoid, reply.";
    }
    else if (alias.equalsIgnoreCase("unadmin")) {
      return "unadmin <USERNAME> - disallow <USERNAME> access to these special commands.\n"
          + "Syntax: unadmin <USERNAME>\n" + "Disallow a user access to " + name
          + "'s special commands.\n" + "See also: admin, ignore, unignore.";
    }
    else if (alias.equalsIgnoreCase("unignoreuser")) {
      return "Syntax: unignoreuser <USERNAME>\n" + "Remove a user from the list of users " + name
          + " ignores.\n" + "See also: admin, ignore, unadmin.";
    }
    else if (alias.equalsIgnoreCase("unignoreword")) {
      return "Syntax: unignoreword <WORD>\n" + "Remove a word to the list of words " + name
          + " ignores.\n" + "See also: unignoreuser, ignoreword.";
    }

    return null;
  }

  @Override
  public Documentation getDocumentation() {
    return null;
  }
  
  @Override
  public void close() {
  }

  @Override
  public boolean isAdminOnly() {
    // Always false
    return false;
  }

  @Override
  public void setAdminOnly(boolean value) {
    // Always false

  }

  @Override
  public ResponderType getResponderType() {
    // Always CORE
    return ResponderType.CORE;
  }

  @Override
  public void setResponderType(ResponderType type) {
    // Always CORE
  }

}
