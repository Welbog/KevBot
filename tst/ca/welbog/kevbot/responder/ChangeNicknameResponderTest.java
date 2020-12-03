package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.communication.Request.Type;

public class ChangeNicknameResponderTest {
  
  ChangeNicknameResponder responder;

  @Before
  public void setUp() throws Exception {
    responder = new ChangeNicknameResponder();
  }

  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("chgnick"));
  }

  @Test
  public void testGetResponse_changeSuccessful() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.NICKNAME, response.getType());
    assertEquals("infoob", response.getBody());
  }

  @Test
  public void testGetResponse_noMatch() {
    Request request = createRequest();
    request.setMessage("notchgnicknope infoob");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  @Test
  public void testGetResponse_noArgument() {
    Request request = createRequest();
    request.setMessage("chgnick");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  private Request createRequest() {
    return new Request("medium", "channel", "user", "chgnick infoob", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
