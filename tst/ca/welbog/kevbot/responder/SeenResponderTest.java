package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import org.junit.After;
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
import ca.welbog.kevbot.persist.DoubleFile;
import ca.welbog.kevbot.persist.RAHLFactory;
import ca.welbog.kevbot.communication.Request.Type;

@RunWith(MockitoJUnitRunner.class)
public class SeenResponderTest {
  
  SeenResponder responder;
  
  @Mock RAHLFactory mockRAHLFactory;
  @Mock DoubleFile mockDoubleFile;

  @Before
  public void setUp() throws Exception {
  	Mockito.when(mockRAHLFactory.createDoubleFile(Mockito.anyString(), Mockito.anyString())).thenReturn(mockDoubleFile);
  	Mockito.when(mockDoubleFile.getReply("infoob")).thenReturn("Last seen infoob whenever.");
  	
    responder = new SeenResponder(mockRAHLFactory);
  }
  
  @After
  public void tearDown() throws Exception {
  	Mockito.verify(mockDoubleFile, Mockito.atMostOnce()).write();
  	Mockito.verifyNoMoreInteractions(mockDoubleFile);
  }

  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("seen"));
  }

  @Test
  public void testGetResponse_existingEntry() {
    Request request = createRequest();
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals(Response.Type.MESSAGE, response.getType());
    assertNotEquals(0, response.getBody().length());
    assertEquals("Last seen infoob whenever.", response.getBody());
    Mockito.verify(mockDoubleFile).getReply("infoob");
  	Mockito.verify(mockDoubleFile).addOver(Mockito.eq("user"), Mockito.anyString());
  }

  @Test
  public void testGetResponse_unknownUser() {
    Request request = createRequest();
    request.setMessage("seen nothing");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
    Mockito.verify(mockDoubleFile).getReply("nothing");
  	Mockito.verify(mockDoubleFile).addOver(Mockito.eq("user"), Mockito.anyString());
  }

  @Test
  public void testGetResponse_noMatch() {
    Request request = createRequest();
    request.setMessage("haveyouseenmy infoob");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  	Mockito.verify(mockDoubleFile).addOver(Mockito.eq("user"), Mockito.anyString());
  }

  @Test
  public void testGetResponse_wrongArguments() {
    Request request = createRequest();
    request.setMessage("seen");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
  	Mockito.verify(mockDoubleFile).addOver(Mockito.eq("user"), Mockito.anyString());
  }

  private Request createRequest() {
    return new Request("medium", "channel", "user", "seen infoob", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
