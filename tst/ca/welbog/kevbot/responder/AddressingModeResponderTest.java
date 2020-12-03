package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Request.Type;
import ca.welbog.kevbot.communication.Response;

public class AddressingModeResponderTest {
  
  AddressingModeResponder responder;

  @Before
  public void setUp() throws Exception {
    responder = new AddressingModeResponder();
  }

  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("quiet"));
  }

  @Test
  public void testGetResponse_toQuiet() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.MODE, response.getType());
    assertEquals(AddressingMode.QUIET.toString(), response.getBody());
  }

  @Test
  public void testGetResponse_toStandard() {
    Request request = createRequest();
    request.setMode(AddressingMode.QUIET);
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.MODE, response.getType());
    assertEquals(AddressingMode.STANDARD.toString(), response.getBody());
  }

  @Test
  public void testGetResponse_noMatch() {
    Request request = createRequest();
    request.setMessage("somequietthing");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  private Request createRequest() {
    return new Request("medium", "channel", "user", "quiet", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
