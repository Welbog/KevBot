package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Request.Type;
import ca.welbog.kevbot.communication.Response;

public class DateResponderTest {
  
  DateResponder responder;

  @Before
  public void setUp() throws Exception {
    responder = new DateResponder();
  }

  @Test
  public void getDocumentation() {
    Documentation documentation = responder.getDocumentation();
    
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("date"));
    assertNotNull(documentation.body);
    assertFalse(documentation.body.length() == 0);
  }

  @Test
  public void getResponse_matching() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(Response.Type.MESSAGE, response.getType());
    assertTrue(response.getBody().matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"));
  }

  @Test
  public void getResponse_notMatching() {
    Request request = createRequest();
    request.setMessage("somenondatevalue");
    
    assertNull(responder.getResponse(request));
  }

  @Test
  public void getResponse_notRepliableContext() {
    Request request = createRequest();
    request.setMode(AddressingMode.QUIET);

    assertNull(responder.getResponse(request));
  }
  
  private Request createRequest() {
    return new Request("medium", "channel", "user", "date", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }

}
