package ca.welbog.kevbot.communication;

import java.util.StringTokenizer;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Request {

  private String channel;
  private String sender;
  private String message;
  private Type type;
  private AddressingMode mode;
  private String nickname = "KevBot";
  private String medium = "unknown";
  private boolean allowReplies = true;

  public Request() {
    // For serialization
  }

  public Request(String medium, String channel, String sender, String message, String nickname,
      AddressingMode mode, Type type) {
    this.medium = medium.trim();
    this.channel = channel.trim();
    this.sender = sender.trim();
    this.type = type;
    this.nickname = nickname.trim();
    this.mode = mode;
    setMessage(message.trim());

  }

  @JsonIgnore
  public boolean isPrivateMessage() {
    return channel.equalsIgnoreCase(sender);
  }

  public String getMedium() {
    return medium;
  }

  public String getMessage() {
    String body = this.message;
    // Determine whether the bot was addressed, and if so modify the incoming
    // message.
    if (body.matches("(?i)^" + java.util.regex.Pattern.quote(nickname) + "[?\\/!1~`.,:;<>]+.*")) {

      body = body
          .replaceFirst("(?i)^" + java.util.regex.Pattern.quote(nickname) + "[?\\/!1~`.,:;<>]+", "")
          .trim();
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    String newBody = "";
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (token.matches("[Mm][Ee][?/!1~`.,<>]*")) {
        String temp = token.substring(2);
        if (sender.trim().contains(" ")) { // For multi-word names, add quotes.
          token = "\"" + sender + "\"" + temp;
        }
        else {
          token = sender + temp;
        }
      } // Look for "me" and replace with the current user. Useful for things
        // like "karma me?"
      if (token.matches("[$][Mm][Ee][?/!1~`.,<>]*")) { // Look for "\me" because
                                                       // it's special. Wheeee.
        String temp = token.substring(3);
        token = "me" + temp;
      }
      newBody += token + " ";
    }
    body = newBody.trim();
    return body;
  }

  public String getChannel() {
    return channel;
  }

  public String getSender() {
    return sender;
  }

  public Type getType() {
    return type;
  }

  public AddressingMode getMode() {
    return mode;
  }

  public String getNickname() {
    return nickname;
  }

  public void setMessage(String newMessage) {
    this.message = newMessage;
  }

  public boolean canReply() {
    boolean addressed = false;

    if (message
        .matches("(?i)^" + java.util.regex.Pattern.quote(nickname) + "[?\\/!1~`.,:;<>]+.*")) {
      addressed = true;
    }
    return isPrivateMessage() || addressed || getMode() == AddressingMode.STANDARD;
  }

  public boolean mustReply() {
    boolean addressed = false;

    if (message
        .matches("(?i)^" + java.util.regex.Pattern.quote(nickname) + "[?\\/!1~`.,:;<>]+.*")) {
      addressed = true;
    }
    return allowReplies && (isPrivateMessage() || addressed);
  }

  public void allowReplies(boolean value) {
    allowReplies = value;
  }

  public String toString() {
    return "  REQUEST " + medium + ":" + channel + ":<" + sender + "> "
        + (type == Type.MESSAGE ? "" : "ACTION ") + getMessage();
  }

  public enum Type {
    MESSAGE, ACTION
  }

  public void setMedium(String string) {
    this.medium = string;
  }

  public void setChannel(String string) {
    this.channel = string;
  }

  public void setSender(String string) {
    this.sender = string;
  }

  public void setNickname(String string) {
    this.nickname = string;
  }

  public void setMode(AddressingMode mode) {
    this.mode = mode;
  }

  public void setType(Request.Type type) {
    this.type = type;
  }
}
