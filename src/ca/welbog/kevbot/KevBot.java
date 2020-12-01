package ca.welbog.kevbot;

import java.io.IOException;

import ca.welbog.kevbot.configuration.Configuration;
import ca.welbog.kevbot.core.Processor;
import ca.welbog.kevbot.http.HTTPListener;
import ca.welbog.kevbot.log.Logger;

public class KevBot {

  public static final String VERSION = "6.2.0";

  public static void main(String[] args) throws IOException {

    String configFile = args[0];
    Configuration config = new Configuration(configFile);
    KevBot bot = new KevBot(config);
    bot.startHTTP();
  }

  private Logger log = null;
  private Configuration config;

  public KevBot(Configuration config) {
    log = new Logger(false);
    this.config = config;

  }

  private void startHTTP() throws IOException {
    log.log("KevBot HTTP initialization started!");
    Processor processor = new Processor(log, config.getResponders());
    HTTPListener adapter = new HTTPListener(log, processor);
    log.log("KevBot HTTP listener launched!");
  }
}
