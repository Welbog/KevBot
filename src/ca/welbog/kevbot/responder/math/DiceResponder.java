package ca.welbog.kevbot.responder.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.service.Service;

public class DiceResponder implements Responder {

  private static final int MAX_DICE = 64;
  private static final int MAX_FACES = 4096;
  private static final Random random = new Random();

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("dice");
    return new Documentation("Syntax: ndm or nxm, where n and m are whole numbers.\n"
        + "Let this bot roll n dice with m faces each. If n is omitted, roll 1 die with m faces.\n"
        + "If x is used, the dice \"explode\", meaning maximum rolls on a die add another die to pool, recursively.\n"
        + "NOTE: More than " + MAX_DICE + " dice, or dice with more than " + MAX_FACES
        + " are not permitted." + "", aliases);
  }

  @Override
  public Response getResponse(Request r) {
    // Roll the dice based on a string in the form xdy.
    // Where x and y are digits, x is optional.
    if (!r.canReply()) {
      return null;
    }
    String body = r.getMessage().trim();
    if (!body.matches("\\d*[xd]\\d+")) {
      return null;
    }

    int result = 0, x = 1, y = 0;
    String m = "";
    boolean explode = false;
    if (body.matches("\\d+[xd]\\d+")) { // First digit exists
      y = body.indexOf('d');
      if (y == -1) {
        y = body.indexOf('x');
        explode = true;
      }
      x = Integer.parseInt(body.substring(0, y));
      String temp = body.substring(y + 1);
      y = Integer.parseInt(temp);
    }
    else { // First digit does not exist
      // String must be of the form "dy"
      if (body.charAt(0) == 'x') {
        explode = true;
      }
      String temp = body.substring(1);
      y = Integer.parseInt(temp);
    }
    if (x > MAX_DICE) {
      return new Response(r.getChannel(), "Error: " + r.getSender()
          + ", number of dice to roll cannot be greater than " + MAX_DICE + ".", Type.MESSAGE);
    }
    if (y > MAX_FACES) {
      return new Response(r.getChannel(), "Error: " + r.getSender()
          + ", number of dice faces cannot be greater than " + MAX_FACES + ".", Type.MESSAGE);
    }
    ca.welbog.kevbot.log.Logger.debugStatic("DICE: " + x + "d" + y);
    int explosions = 0;
    for (int i = 0; i < x + explosions; i++) {
      int temp = random.nextInt(y) + 1; // (int)Math.floor(Math.random()*(double)y)+1;
      result += temp;
      if (explode && temp == y) {
        explosions++;
      }
      m += temp + " ";
      if (explosions > MAX_DICE) {
        break;
      }
    }
    String explodeResponse = "";
    if (explode) {
      explodeResponse = "(" + explosions + " explosions) ";
    }
    return new Response(r.getChannel(),
        r.getSender() + " rolled " + m + explodeResponse + "Total: " + result + "", Type.MESSAGE);
  }

  @Override
  public List<String> getRequiredServiceNames() {
    return null;
  }

  @Override
  public void addService(String name, Service service) {
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
