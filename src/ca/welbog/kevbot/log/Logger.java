package ca.welbog.kevbot.log;

public class Logger {
  private boolean isDebug = false;
  private static Logger singleton = null;

  public Logger(boolean debug) {
    this.isDebug = debug;
    singleton = this;
  }

  public void log(String message) {
    System.out.println(message);
  }

  public void debug(String message) {
    if (isDebug) {
      System.out.println(message);
    }
  }

  public void log(String message, Exception e) {
    System.out.print(message);
    e.printStackTrace(System.out);
    System.out.println();
  }

  public static void debugStatic(String message) {
    if (singleton == null) {
      new Logger(true);
    }
    singleton.debug(message);
  }
}
