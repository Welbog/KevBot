package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Request.Type;

public class JoinLeaveResponderTest {
  
  JoinLeaveResponder responder;

  @Before
  public void setUp() throws Exception {
    responder = new JoinLeaveResponder();
  }

  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("join"));
    assertTrue(documentation.aliases.contains("leave"));
  }

  @Test
  public void testGetResponse_join() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.JOIN, response.getType());
    assertEquals("#channel", response.getBody());
  }

  @Test
  public void testGetResponse_leave() {
    Request request = createRequest();
    request.setMessage("leave channel");
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.LEAVE, response.getType());
    assertEquals("#channel", response.getBody());
  }

  @Test
  public void testGetResponse_noMatch() {
    Request request = createRequest();
    request.setMessage("wowzerleavee channel");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  @Test
  public void testGetResponse_wrongArguments() {
    Request request = createRequest();
    request.setMessage("join channel five");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  private Request createRequest() {
    return new Request("medium", "someChannel", "user", "join channel", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
