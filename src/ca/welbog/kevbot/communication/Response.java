package ca.welbog.kevbot.communication;

import java.util.Collection;

public class Response {

  private String body;
  private String target;
  private Type type;
  private boolean allowIteration = false;

  public static final Response STOP_NOW = new Response(null, null, Type.STOP_PROCESSING);
  public static final Response CONTINUE = new Response(null, null, Type.KEEP_PROCESSING);

  public Response() {
    // For serialization
  }

  public Response(String target, String body, Type type) {
    this.target = target;
    this.body = body;
    this.type = type;
    if (this.type == null) {
      this.type = Type.STOP_PROCESSING;
    }
  }

  public void allowIteration() {
    allowIteration = true;
  }

  public boolean iterable() {
    return allowIteration;
  }

  public boolean hasContent() {
    return body != null && !body.trim().isEmpty();
  }

  public String toString() {
    return " RESPONSE " + target + ":<" + "BOT" + "> " + body;
  }

  public String getTarget() {
    return target;
  }

  public String getBody() {
    return body;
  }

  public Type getType() {
    return type;
  }

  public enum Type {
    MESSAGE, ACTION, NICKNAME, MODE, JOIN, LEAVE, QUIT, STOP_PROCESSING, KEEP_PROCESSING
  }
}
