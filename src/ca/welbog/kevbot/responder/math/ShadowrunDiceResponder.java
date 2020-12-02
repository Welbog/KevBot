package ca.welbog.kevbot.responder.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;

public class ShadowrunDiceResponder implements Responder {

  private static final int MAX_DICE = 64;
  private static final int FACES = 6;
  private static final int GLITCH_THRESHOLD = 1;
  private static final int EXPLODE_THRESHOLD = 6;
  private static final int SUCCESS_THRESHOLD = 5;

  private static final Random random = new Random();

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("shadowrundice");
    return new Documentation("Syntax: sr m or srx m, where m is a whole number.\n"
        + "Let this bot roll m size-sided dice. If x is specified, the dice will be re-rolled on 6.\n"
        + "This counts succeesses (fives and sixes) and glitches (more than half of the rolls are 1)\n"
        + "See also: dice", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    // Roll the dice based on a string in the form xdy.
    // Where x and y are digits, x is optional.
    if (!r.canReply()) {
      return null;
    }
    String body = r.getMessage().trim();
    if (!body.matches("srx?\\s+\\d+")) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(body);
    String type = tokenizer.nextToken();
    String numberOfDiceString = tokenizer.nextToken();
    int numberOfDice = Integer.parseInt(numberOfDiceString);

    boolean explode = type.contains("x");

    if (numberOfDice > MAX_DICE) {
      return new Response(r.getChannel(), "Error: " + r.getSender()
          + ", number of dice to roll cannot be greater than " + MAX_DICE + ".", Type.MESSAGE);
    }

    String diceRollString = "";
    int result = 0;
    int explosions = 0;
    int successes = 0;
    int glitches = 0;
    for (int i = 0; i < numberOfDice + explosions; i++) {
      int temp = random.nextInt(FACES) + 1; // (int)Math.floor(Math.random()*(double)y)+1;
      result += temp;
      if (temp >= SUCCESS_THRESHOLD) {
        successes++;
      }
      if (temp <= GLITCH_THRESHOLD) {
        glitches++;
      }
      if (explode && temp >= EXPLODE_THRESHOLD) {
        explosions++;
      }
      diceRollString += temp + " ";
      if (explosions > MAX_DICE) {
        break;
      }
    }

    String explodeResponse = "";
    if (explode) {
      explodeResponse = "(" + explosions + " explosions) ";
    }
    String shadowRunString = "";
    if (((double) glitches / (double) (numberOfDice + explosions)) >= 0.5) {
      if (successes == 0) {
        shadowRunString += "CRITICAL ";
      }
      shadowRunString += "GLITCH ";
    }
    shadowRunString += successes + " success" + ((successes == 1) ? ("") : ("es"));

    return new Response(r.getChannel(),
        r.getSender() + ": " + shadowRunString + ", rolls: " + diceRollString + explodeResponse,
        Type.MESSAGE);
  }
  
  @Override
  public void close() {
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
