package ca.welbog.kevbot.communication;

public interface StatefulBot {
  public void processRequest(Request request);
  public void sendResponse(Response response);

  public enum Mode {
    STANDARD,
    QUIET
  }
}

