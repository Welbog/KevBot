package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Request.Type;

public class SayActResponderTest {

  SayActResponder responder;
  
  @Before
  public void setUp() throws Exception {
    responder = new SayActResponder();
  }

  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("say"));
    assertTrue(documentation.aliases.contains("act"));
  }

  @Test
  public void testGetResponse_say() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.MESSAGE, response.getType());
    assertEquals("some phrase", response.getBody());
    assertEquals("channel", response.getTarget());
  }

  @Test
  public void testGetResponse_act() {
    Request request = createRequest();
    request.setMessage("act channel some phrase");
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.ACTION, response.getType());
    assertEquals("some phrase", response.getBody());
    assertEquals("channel", response.getTarget());
  }

  @Test
  public void testGetResponse_noMatch() {
    Request request = createRequest();
    request.setMessage("doaction channel some phrase");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  @Test
  public void testGetResponse_wrongArguments() {
    Request request = createRequest();
    request.setMessage("say channel");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  private Request createRequest() {
    return new Request("medium", "someChannel", "user", "say channel some phrase", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
