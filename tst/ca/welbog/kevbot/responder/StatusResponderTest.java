package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

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
import ca.welbog.kevbot.responder.factoid.DoubleSQL;
import ca.welbog.kevbot.responder.markov.SQLWeightedMarkov;
import ca.welbog.kevbot.communication.Request.Type;

@RunWith(MockitoJUnitRunner.class)
public class StatusResponderTest {
  
  StatusResponder responder;
  
  @Mock DoubleSQL mockFactoidDB;
  @Mock SQLWeightedMarkov mockMarkov1DB;

  @Before
  public void setUp() throws Exception {
    responder = new StatusResponder();
    responder.setFactoidDatabase(mockFactoidDB);
    responder.setOrder1Database(mockMarkov1DB);
    
    Mockito.when(mockFactoidDB.getSize()).thenReturn(5555);
    Mockito.when(mockMarkov1DB.size()).thenReturn(6666);
  }

  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("status"));
  }

  @Test
  public void testGetResponse() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertNotEquals(0, response.getBody().length());
    assertTrue(response.getBody().contains("5555")); // Number of factoids
    assertTrue(response.getBody().contains("6666")); // Number of associations
    assertTrue(response.getBody().contains("optional")); // Addressing mode
  }

  @Test
  public void testGetResponse_noMatch() {
    Request request = createRequest();
    request.setMessage("notstatusatall");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  }

  private Request createRequest() {
    return new Request("medium", "channel", "user", "status", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
