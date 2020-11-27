package ca.welbog.kevbot.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.regex.Pattern;


import ca.welbog.kevbot.Processor;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Request.Type;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.StatefulBot;
import ca.welbog.kevbot.communication.StatefulBot.Mode;
import ca.welbog.kevbot.communication.http.MessageSerializer;
import ca.welbog.kevbot.log.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.*;

public class HTTPListener implements HttpHandler {
  
  private Processor processor;
  private Logger log;
  
  // HTTP stuff
  private final static String CONTEXT = "/kevbot";
  private final static int PORT = 1237;
  private HttpServer httpServer;
  private MessageSerializer serializer;
  
  public HTTPListener(Logger log, Processor processor) throws IOException {
    this.processor = processor;
    this.log = log;
    this.serializer = new MessageSerializer();
    
    httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    httpServer.createContext(CONTEXT, this);
    httpServer.setExecutor(null);
    httpServer.start();
  }

  public String processRequest(Request request) throws JsonProcessingException {
    Response response = new Response();
    try {
      log.log("Got HTTP request: " + request.toString());
      response = processor.getResponseForRequest(request);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    log.log("Got HTTP response: " + response.toString());
    return serializer.getJSON(response);
  }

  @Override
  public void handle(HttpExchange exchange) {
    try {
      URI uri = exchange.getRequestURI();
      String path = uri.getPath().replaceFirst(Pattern.quote(CONTEXT + "/"), "");
      
      Request request = serializer.getRequestFromGETPath(uri); // GET 
      if (request == null) { // POST
        String json = getPayloadFromExchange(exchange);
        request = serializer.getRequest(json);
      }
      String reply = processRequest(request); 
      exchange.sendResponseHeaders(200, reply.length());
      OutputStream output = exchange.getResponseBody();
      output.write(reply.getBytes());
      output.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getPayloadFromExchange(HttpExchange exchange) throws IOException {
    InputStream inputStream = exchange.getRequestBody();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String inputLine;
    StringBuffer buffer = new StringBuffer();
    
    while ((inputLine = reader.readLine()) != null) {
      buffer.append(inputLine);
    }
    reader.close();
    return buffer.toString();
  }

}
