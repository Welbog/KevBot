package ca.welbog.kevbot.responder;

import java.text.SimpleDateFormat;
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
import ca.welbog.kevbot.persist.SingleFile;

public class MessagingResponder implements Responder {
  private SingleFile messages;

  public MessagingResponder() {
    messages = new SingleFile("messages.txt");
  }

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("message");
    aliases.add("check");
    return new Documentation(
        "Syntax: message <USERNAME> <MESSAGE>\nLeave a message in this bot for <USERNAME> to pick up later.\n"
            + "Syntax: check\nCheck for messages left for you by other users.\n"
            + "NOTE: messages are deleted from the database as soon as they are delivered.\n"
            + "NOTE: messages cannot be deleted except by the intended recipient upon checking.\n"
            + "NOTE: messages can be set only in private messages to this bot. Retrieval is done only in private messages to this bot.",
        aliases);
  }

  @Override
  public synchronized Response getResponse(Request r) {
    String body = r.getMessage();

    // Messaging only works in private messages
    if (!r.isPrivateMessage()) {
      return null;
    }

    // Check for messages
    if (body.matches("(?i)check\\s*")) {
      if (!messages.exists(r.getSender())) { // Make sure the user has messages.
        return new Response(r.getChannel(), "You do not have any messages.", Type.MESSAGE);
      }
      messages.delete(r.getSender());
      SingleFile userMessages = new SingleFile(r.getSender() + ".messages"); // Find
                                                                             // his
                                                                             // message
                                                                             // file.
      int i = 0;
      String currMessage = userMessages.getLine(i);
      StringTokenizer str;
      String m = "";
      String text = "";
      while (currMessage != "") { // While there's still messages, send them to
                                  // the user.
        m = "";
        str = new StringTokenizer(currMessage);
        str.nextToken(); // Get rid of the first "word" which is secretly a
                         // timestamp.
        while (str.hasMoreTokens()) {
          m = m + str.nextToken() + " "; // Recompile the String.
        }
        text += m + "\n";
        i++;
        currMessage = userMessages.getLine(i); // Look for more messages in the
                                               // user's message file.
      }
      text += "End of message list.";
      userMessages.deleteAll(); // Destroy the user's message file.
      userMessages.close(); // Close it.
      messages.write();
      return new Response(r.getChannel(), text, Type.MESSAGE);
    }

    // Take a message
    if (body.matches("(?i)^message\\s+\\S+\\s+\\S+.*")) {
      StringTokenizer tokenizer = new StringTokenizer(body);
      tokenizer.nextToken();
      String target = tokenizer.nextToken();
      String rest = "";
      while (tokenizer.hasMoreTokens()) {
        rest += tokenizer.nextToken() + " ";
      }
      rest = rest.trim();
      messages.add(target);
      messages.write();

      SingleFile userMessages = new SingleFile(target + ".messages"); // Open a
                                                                      // file
                                                                      // for
                                                                      // append.
      SimpleDateFormat format = new SimpleDateFormat("YYYYMMddHHmmss");
      Date t = new Date(); // Find the timestamp. The purpose is to create a
                           // long "string" of numbers.
      String temp = format.format(t);
      ca.welbog.kevbot.log.Logger.debugStatic("Date format: " + temp);
      // With this timestamp, the messages are guaranteed to be returned to the
      // user in chronological order (oldest appears first).
      userMessages
          .add(temp + " Message from " + r.getSender() + " (" + t.toString() + "): " + rest);
      // Message format is stored in the following manner:
      // <TIMESTAMP> Message from <FROM> (<HUMAN-READABLE TIMESTAMP): <MESSAGE>
      userMessages.close(); // Write the message file and close it.
      return new Response(r.getChannel(), "Message taken.", Type.MESSAGE);
    }

    return null;
  }

  @Override
  public List<String> getRequiredServiceNames() {
    return null;
  }
  
  @Override
  public void close() {
    messages.close();
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
