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
import ca.welbog.kevbot.persist.KarmaFile;

public class KarmaResponder implements Responder {
  private KarmaFile karma; // KarmaFile for karma.

  public KarmaResponder() {
    karma = new KarmaFile("karma.txt", "karmb.txt");
  }

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("karma");
    return new Documentation(
        "Syntax: karma <WORD>\nCheck the karma of a word.\nKarma is changed with the following syntax: <WORD>++ and <WORD>--.\nThese will change the karma of <WORD> by one.\nNOTE: Karma cannot be changed in a private message.\nNOTE: You cannot change your own nickname's karma.",
        aliases);
  }

  @Override
  public Response getResponse(Request r) {
    String body = r.getMessage();

    // Match the main "karma" keyword.
    if (body.matches("(?i)^karma\\s+\\S+.*")) {
      StringTokenizer tokenizer = new StringTokenizer(body);
      tokenizer.nextToken();
      String word = tokenizer.nextToken();
      // Get all remaining tokens, as Discord names can contain spaces.
      while (tokenizer.hasMoreTokens()) {
        word += " " + tokenizer.nextToken();
      }
      word.replaceAll("^\"", ""); // Remove quotes at the start and end
      word.replaceAll("\"$", "");
      int i = karma.getKarma(word);
      return new Response(r.getChannel(), word + " has " + i + " karma.", Type.MESSAGE);
    }

    // Karma cannot be set in a private message
    if (body.matches("^.*(\\+\\+|--).*$") && r.isPrivateMessage()) {
      return new Response(r.getChannel(), "You cannot change karma in a private message.",
          Type.MESSAGE);
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    while (tokenizer.hasMoreTokens()) {
      String word = tokenizer.nextToken();
      adjustKarma(word, r.getSender());
    }

    // Karma cannot be set on the user's own name
    if (body.matches("(?i)^.*" + java.util.regex.Pattern.quote(r.getSender()) + "(\\+\\+|--).*$")) {
      return new Response(r.getSender(), "You cannot change your own karma.", Type.MESSAGE);
    }
    return null;
  }

  private void adjustKarma(String word, String sender) {
    if (word.matches(".*[+][+]")) {
      changeKarma(word.substring(0, word.length() - 2), 1, sender);
    }
    else if (word.matches(".*[-][-]")) {
      changeKarma(word.substring(0, word.length() - 2), -1, sender);
    }
  }

  private void changeKarma(String word, int change, String sender) {
    if (sender.equalsIgnoreCase(word)) {
      // You can't change your own karma.
      return;
    }
    ca.welbog.kevbot.log.Logger
        .debugStatic("changeKarma(" + karma.getRealLine(word) + ", " + change + ");");
    karma.changeKarma(word, change);
    karma.write();
  }

  @Override
  public void close() {
    karma.close();
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
