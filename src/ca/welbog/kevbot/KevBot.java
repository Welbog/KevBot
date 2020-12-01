package ca.welbog.kevbot;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import ca.welbog.kevbot.http.HTTPListener;
import ca.welbog.kevbot.log.Logger;

public class KevBot {

  public static final String VERSION = "6.2.0";

  public static void main(String[] args) throws IOException {

    String configFile = args[0];
    ApplicationContext spring = new FileSystemXmlApplicationContext(configFile);
    KevBot bot = spring.getBean(KevBot.class);
    bot.startHTTP();
  }

  private Logger log = null;
  private HTTPListener listener = null;
  
  public KevBot(Logger log, HTTPListener listener) {
    this.log = log;
    this.listener = listener;
  }

  private void startHTTP() throws IOException {
    log.log("KevBot HTTP initialization started!");
    listener.start();
    log.log("KevBot HTTP listener launched!");
  }
}
