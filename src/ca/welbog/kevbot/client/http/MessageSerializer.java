package ca.welbog.kevbot.client.http;

import java.io.IOException;
import java.net.URI;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Request.Type;
import ca.welbog.kevbot.communication.Response;

public class MessageSerializer {

  ObjectMapper mapper;

  public MessageSerializer() {
    mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
  }

  public Request getRequest(String json)
      throws JsonParseException, JsonMappingException, IOException {
    return mapper.readValue(json, Request.class);
  }

  public Request getRequestFromGETPath(URI path) {
    Request request = new Request("HTTP", "HTTP", "HTTP", "", "KevBot", AddressingMode.STANDARD,
        Type.MESSAGE);
    Map<String, List<String>> map = splitQuery(path);
    boolean foundSomething = false;
    if (map.containsKey("medium")) {
      request.setMedium(map.get("medium").get(0));
    }
    if (map.containsKey("channel")) {
      request.setChannel(map.get("channel").get(0));
    }
    if (map.containsKey("sender")) {
      request.setSender(map.get("sender").get(0));
    }
    if (map.containsKey("message")) {
      foundSomething = true;
      request.setMessage(map.get("message").get(0));
    }
    if (map.containsKey("nickname")) {
      request.setNickname(map.get("nickname").get(0));
    }
    if (map.containsKey("mode")) {
      request.setMode(AddressingMode.valueOf(map.get("mode").get(0)));
    }
    if (map.containsKey("type")) {
      request.setType(Request.Type.valueOf(map.get("type").get(0)));
    }
    if (!foundSomething) {
      return null;
    }
    return request;
  }

  private Map<String, List<String>> splitQuery(URI url) {
    if (url == null || url.getQuery() == null || url.getQuery().trim().isEmpty()) {
      return Collections.emptyMap();
    }
    return Arrays.stream(url.getQuery().split("&")).map(this::splitQueryParameter)
        .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
  }

  private SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
    final int idx = it.indexOf("=");
    final String key = idx > 0 ? it.substring(0, idx) : it;
    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
    return new SimpleImmutableEntry<>(key, value);
  }

  public String getJSON(Request request) throws JsonProcessingException {
    return mapper.writeValueAsString(request);
  }

  public Response getResponse(String json)
      throws JsonParseException, JsonMappingException, IOException {
    return mapper.readValue(json, Response.class);
  }

  public String getJSON(Response response) throws JsonProcessingException {
    return mapper.writeValueAsString(response);
  }
}
