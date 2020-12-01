package ca.welbog.kevbot.responder.math;

import java.util.ArrayList;
import java.util.List;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Response.Type;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.core.ResponderType;
import ca.welbog.kevbot.persist.KarmaFile;

public class MathResponder implements Responder {

  @Override
  public Documentation getDocumentation() {
    List<String> aliases = new ArrayList<String>();
    aliases.add("math");
    return new Documentation(
        "Syntax: a+b, a-b, a*b, a/b, a^b, a%b, (a), -a, a!, funct(a), e, pi, adb, axb\n"
            + "Also more complicated expressions such as (a+b-c)*d/e^f\n"
            + "Where a, b, c, d, e and f are numbers in base 10 (decimals allowed).\n"
            + "Functions are: cos, tan, sin, abs, sign, log(x,y), lg, ln, rand, floor, ceiling, round, if\n"
            + "Read-only variables can be retrieved from the karma database as follows: @string\n"
            + "Read/write variables can be stored using the syntax $var='expression' and read using '$var'.\n"
            + "Similarly, functions can be stored using the syntax $func($vars)='expression' and invoked using '$func($vars)'.\n"
            + "The last calculated value is always stored in the variable '_last'.\n"
            + "Self-altering assignment is supported in the form '$var+=10', which is read as '$var=$var+10'. +, -, /, *, % and ^ are supported in this way.\n"
            + "Order of operations is preserved.\n" + "See also: dice.",
        aliases);
  }

  @Override
  public Response getResponse(Request r) {

    // Find and replace "@word" constructs with that word's karma in brackets.
    String body = r.getMessage();
    String mathMessage = "";
    for (int i = 0; i < body.length(); i++) {
      if (body.charAt(i) != '@') {
        mathMessage += body.charAt(i);
      }
      else { // Found @
        int j = i + 1;
        String var = "";
        while (Character.isLetterOrDigit(body.charAt(j))) {
          var += body.charAt(j);
          j++;
          if (j >= body.length()) {
            break;
          }
        }

        // TODO: Replace this with something better.
        KarmaFile karma = new KarmaFile("karma.txt", "karmb.txt"); // Read-only
                                                                   // karma file
        mathMessage += "(" + karma.getKarma(var) + ")";

        i = j - 1;
      }
    }

    List<MathParser2> parsers = new ArrayList<MathParser2>();

    if (!mathMessage.contains("[")) {
      parsers.add(new MathParser2(mathMessage));
    }
    else {
      int start = -1;
      for (int i = 0; i < mathMessage.length(); i++) {
        if (mathMessage.charAt(i) == '[') {
          start = i + 1;
        }
        else if (mathMessage.charAt(i) == ']' && start != -1) {
          String subMessage = mathMessage.substring(start, i);
          System.out.println(subMessage);
          parsers.add(new MathParser2(subMessage));
          start = -1;
        }
      }
    }

    if (!r.canReply()) {
      return null;
    } // We still want to define functions/variables if we shouldn't reply.

    String output = "";
    for (MathParser2 math : parsers) {
      if (math.getFail()) {
        continue;
      }

      String result = math.getValue();
      if (result.equalsIgnoreCase("nan")) {
        result = "Nullity";
      } // Transreal math
      if (output.isEmpty()) {
        output = result;
      }
      else {
        output += ", ";
        output += result;
      }
    }
    if (output.isEmpty()) {
      return null;
    }
    return new Response(r.getChannel(), output, Type.MESSAGE);
  }

  @Override
  public List<String> getRequiredServiceNames() {
    return null;
  }
  
  @Override
  public void close() {
  }

  public static void main(String[] args) {
    MathResponder responder = new MathResponder();
    System.out.println(responder.getResponse(new Request("irc", "#test", "user", "[$dog=5]wa[$dog]",
        "KevBot", AddressingMode.STANDARD, Request.Type.MESSAGE)).getBody());
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
