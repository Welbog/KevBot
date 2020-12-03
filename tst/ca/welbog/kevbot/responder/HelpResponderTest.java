package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.core.Responder;
import ca.welbog.kevbot.communication.Request.Type;

@RunWith(MockitoJUnitRunner.class)
public class HelpResponderTest {
  
  HelpResponder responder;
  
  @Mock Responder mockRegularResponder;
  @Mock Responder mockAdminResponder;
  @Mock Documentation mockRegularDocumentation;
  @Mock Documentation mockAdminDocumentation;

  @Before
  public void setUp() throws Exception {
  	Mockito.when(mockRegularDocumentation.getAliases()).thenReturn(Arrays.asList("command","someAlias"));
  	Mockito.when(mockRegularDocumentation.getBody()).thenReturn("Command's help");
  	Mockito.when(mockRegularResponder.getDocumentation()).thenReturn(mockRegularDocumentation);
  	Mockito.when(mockAdminDocumentation.getAliases()).thenReturn(Arrays.asList("adminCommand","someAdminAlias"));
  	Mockito.when(mockAdminDocumentation.getBody()).thenReturn("Admin command's help");
  	Mockito.when(mockAdminResponder.getDocumentation()).thenReturn(mockAdminDocumentation);
  	Mockito.when(mockAdminResponder.isAdminOnly()).thenReturn(true);
  	
    responder = new HelpResponder(Arrays.asList(mockRegularResponder, mockAdminResponder));
  }

  @Test
  public void testGetResponse_base() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertTrue(response.getBody().contains("command"));
    assertTrue(response.getBody().contains("someAlias"));
    assertTrue(response.getBody().contains("adminCommand*"));
    assertTrue(response.getBody().contains("someAdminAlias*"));
  }

  @Test
  public void testGetResponse_baseOutsidePM() {
    Request request = createRequest();
    request.setChannel("someChannel");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  @Test
  public void testGetResponse_command() {
    Request request = createRequest();
    request.setMessage("help command");
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertTrue(response.getBody().contains("Command's help"));
    assertFalse(response.getBody().contains("This command is admin-only"));
  }

  @Test
  public void testGetResponse_adminCommand() {
    Request request = createRequest();
    request.setMessage("help adminCommand");
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertTrue(response.getBody().contains("Admin command's help"));
    assertTrue(response.getBody().contains("This command is admin-only"));
  }
  
  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNull(documentation);
    // Help is the only responder that doesn't need documentation.
  }

  private Request createRequest() {
    return new Request("medium", "user", "user", "help", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
