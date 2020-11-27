package ca.welbog.kevbot.communication;

import java.util.List;

public class Documentation {
  public String body;
  public List<String> aliases;
  
  public String getBody() {
    return body;
  }
  
  public List<String> getAliases() {
    return aliases;
  }
  
  public Documentation(String body, List<String> aliases) {
    this.body = body;
    this.aliases = aliases;
  }
}
