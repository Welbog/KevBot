package ca.welbog.kevbot.client.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;

public class HTTPClient {

  private MessageSerializer serializer;
  private String url = "http://localhost:1237/kevbot";

  public HTTPClient() {
    serializer = new MessageSerializer();
  }

  public Response send(Request request) throws IOException {
    System.out.println("Sending request to server: " + request);
    URL url = new URL(this.url);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(2500);
    connection.setReadTimeout(5000);
    connection.setRequestMethod("POST");
    connection.setRequestProperty("User-Agent",
        "KevClient-" + request.getMedium() + "-" + request.getChannel());
    connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

    connection.setDoOutput(true);
    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

    String json = serializer.getJSON(request);
    outputStream.writeBytes(json);
    outputStream.flush();
    outputStream.close();

    int responseCode = connection.getResponseCode();
    if (responseCode >= 200 && responseCode <= 299) {
      BufferedReader inputStream = new BufferedReader(
          new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer responseBuffer = new StringBuffer();

      while ((inputLine = inputStream.readLine()) != null) {
        responseBuffer.append(inputLine);
      }
      inputStream.close();
      return serializer.getResponse(responseBuffer.toString());
    }
    else {
      throw new IOException("Response code was" + responseCode);
    }
  }
}
