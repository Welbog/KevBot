package ca.welbog.kevbot.communication;

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

  public String findFirstSubString() {
    Pair pair = getFirstSubStringLocation(body);
    if (pair == null) {
      return null;
    }
    return getSubstring(body, pair);
  }

  private String getSubstring(String message, Pair location) {
    String s = message.substring(location.getLeft() + 1, location.getRight());
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '$' && (((i + 1) < s.length()) && (s.charAt(i + 1) == '`'))) { // Remove
                                                                                        // "$`"
                                                                                        // and
                                                                                        // replace
                                                                                        // with
                                                                                        // "`"
        s = s.substring(0, i) + s.substring(i + 1);
        i--;
      }
    }
    return s;
  }

  private Pair getFirstSubStringLocation(String message) {

    if (message.contains("`")) {
      // Find all text between backticks, excluding escaped backticks.
      boolean ignorenext = false;
      java.util.Queue<Integer> indexpairs = new java.util.LinkedList<Integer>();
      for (int i = 0; i < message.length(); i++) {
        if (ignorenext) {
          ignorenext = false;
          continue;
        }
        if (message.charAt(i) == '$') {
          ignorenext = true;
          // System.out.println("ORIGINAL MESSAGE: " + message);
          // message = message.substring(0,i) + message.substring(i+1);
          // System.out.println("NEW MESSAGE: " + message);
          // i--;
          continue;
        }
        if (message.charAt(i) == '`') {
          indexpairs.add(i);
        }

        // Pairs of ticks have been found, now it's time to
        // go through each pair and recurse on the substrings.
        if (indexpairs.size() == 2) {
          int firstindex = indexpairs.poll();
          int secondindex = indexpairs.poll();
          return new Pair(firstindex, secondindex);
        }
      }
    }
    return null;
  }

  public void replaceFirstSubString(String replacement) {
    if (replacement == null) {
      replacement = "";
    }
    ca.welbog.kevbot.log.Logger.debugStatic("Original: " + body);
    Pair pair = getFirstSubStringLocation(body);
    String start = body.substring(0, pair.getLeft());
    String end = body.substring(pair.getRight() + 1, body.length());
    body = start + replacement + end;
    ca.welbog.kevbot.log.Logger.debugStatic("Pair: " + pair.toString());
    ca.welbog.kevbot.log.Logger.debugStatic("replacement: " + replacement);
    ca.welbog.kevbot.log.Logger.debugStatic("new body: " + body);
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

  public class Pair {
    private int left, right;

    public Pair(int left, int right) {
      this.left = left;
      this.right = right;
    }

    public int getLeft() {
      return left;
    }

    public int getRight() {
      return right;
    }

    public String toString() {
      return "(" + left + "," + right + ")";
    }
  }
}
