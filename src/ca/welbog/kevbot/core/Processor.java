package ca.welbog.kevbot.core;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Subsequence;
import ca.welbog.kevbot.log.Logger;
import ca.welbog.kevbot.persist.SingleFile;
import ca.welbog.kevbot.responder.HelpResponder;

public class Processor {

  private Logger log = null;
  private List<Responder> responders;
  private SingleFile ignore, admin, bannedWords;
  private static final int MAX_ITERATION = 256;

  public Processor(Logger logger, List<Responder> responders) {
    this.log = logger;
    this.responders = responders;

    // Create a help responder to handle help messages
    HelpResponder responder = new HelpResponder(responders);
    responders.add(0, responder);

    // Create in-memory stores
    ignore = new SingleFile("ignore.txt");
    admin = new SingleFile("admin.txt");
    bannedWords = new SingleFile("banned.txt");
  }

  public Response getResponseForRequest(Request request) {

    // If KevBot said something, we ignore it.
    if (request.getSender().equalsIgnoreCase(request.getNickname())) {
      return Response.STOP_NOW;
    }

    // Determine if ignored
    if (ignore.exists(request.getSender())) {
      return Response.STOP_NOW;
    }

    // Ignore messages with banned words, for various terms of service.
    if (containsBannedWords(request.getMessage())) {
      return Response.STOP_NOW;
    }

    // Determine if admin
    boolean auth = false;
    if (admin.exists(request.getSender())) {
      auth = true;
    }

    Response response = runCoreResponders(request, auth);
    log.debug("Found response from core: " + response);
    if (response.getType() != Response.Type.STOP_PROCESSING) {
      response = runPluginResponders(request, auth);
      log.debug("Found response from plugin: " + response);
    }
    return response;

  }

  private boolean containsBannedWords(String message) {
    if (message == null || message.trim().isEmpty()) {
      return false;
    }
    message = message.toLowerCase();
    // TODO: Gonna wanna make this a lot more efficient.
    for (String badWord : bannedWords.getAll()) {
      Pattern pattern = Pattern.compile(".*" + badWord + ".*");
      if (pattern.matcher(message).matches()) {
        return true;
      }
    }
    return false;
  }

  private Response runCoreResponders(Request request, boolean admin) {
    // Things like admin, ignore, etc.
    if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
      return new Response(null, null, Response.Type.STOP_PROCESSING);
    }
    StringTokenizer tokenizer = new StringTokenizer(request.getMessage());
    String command = tokenizer.nextToken().trim().toLowerCase();
    String detail = null;
    if (tokenizer.hasMoreTokens()) {
      detail = tokenizer.nextToken();
    }
    boolean foundResponse = false;
    boolean detailExists = detail != null && !detail.trim().isEmpty();
    if (detailExists && admin) {
      if (command.equals("admin")) {
        addIfAbsent(this.admin, detail);
        foundResponse = true;
      }
      else if (command.equals("unadmin")) {
        removeIfExists(this.admin, detail);
        foundResponse = true;
      }
      else if (command.equals("ignoreuser")) {
        addIfAbsent(this.ignore, detail);
        foundResponse = true;
      }
      else if (command.equals("unignoreuser")) {
        removeIfExists(this.ignore, detail);
        foundResponse = true;
      }
      else if (command.equals("ignoreword")) {
        addIfAbsent(this.bannedWords, detail);
        foundResponse = true;
      }
      else if (command.equals("unignoreword")) {
        removeIfExists(this.bannedWords, detail);
        foundResponse = true;
      }
      else if (command.equals("quit") && request.isPrivateMessage()) {
        String rest = detail;
        while (tokenizer.hasMoreTokens()) {
          rest += " " + tokenizer.nextToken();
        }
        return new Response(null, rest, Response.Type.QUIT);
      }
    }
    if (foundResponse) {
      return Response.STOP_NOW;
    }

    return Response.CONTINUE;
  }

  private Response runPluginResponders(Request request, boolean admin) {
    Response response = null;
    for (Responder responder : responders) {
      if (!responder.isAdminOnly() || (responder.isAdminOnly() && admin)) {
        response = responder.getResponse(request);
        if (response != null) {
          log.debug("Found response from plugin: " + response);
          log.debug("Response class: " + responder.getClass().getName());
          break;
        }
      }
    }
    if (response == null) {
      return Response.STOP_NOW;
    }
    if (!response.hasContent()) {
      return Response.STOP_NOW;
    }
    if (!response.iterable()) {
      return response;
    }

    return runRecursiveResponders(request, response, admin);
  }

  private Response runRecursiveResponders(
  		Request request, 
  		Response intermediateResponse,
      boolean admin) {
  	
  	Subsequence subsequence = Subsequence.buildFirstSubsequence(intermediateResponse.getBody());
  	if (!subsequence.containsSubsequence()) {
      return intermediateResponse;
    }
  	
    // Loop until there's no more recursion, or until we've hit a maximum.
    int iteration = 0;
    while (subsequence.containsSubsequence() && iteration < MAX_ITERATION) {
      Request innerRequest = new Request(
      		request.getMedium(), request.getChannel(),
          request.getSender(), subsequence.getRecursiveString(), 
          request.getNickname(), request.getMode(),
          request.getType());
      innerRequest.allowReplies(false);
      String innerResponse = runRecursiveResponders(innerRequest);
      iteration++;
      subsequence = Subsequence.buildFirstSubsequence(subsequence.replaceSubsequence(innerResponse));
    }
    return new Response(
    		intermediateResponse.getTarget(),
    		subsequence.toString(),
    		intermediateResponse.getType()
    		);
  }

  private String runRecursiveResponders(Request request) {
    Response response = null;
    for (Responder responder : responders) {
      if (responder.isAdminOnly() || responder.getResponderType() != ResponderType.RECURSIVE) {
        continue;
      } // Only deal with non-admin recursive commands
      response = responder.getResponse(request);
      if (response != null) {
        break;
      }
    }
    if (response == null) {
      return "";
    }
    return response.getBody();
  }

  private void removeIfExists(SingleFile rahl, String value) {
    if (rahl.exists(value)) {
      rahl.delete(value);
    }
  }

  private void addIfAbsent(SingleFile rahl, String value) {
    if (!rahl.exists(value)) {
      rahl.add(value);
    }
  }
}
