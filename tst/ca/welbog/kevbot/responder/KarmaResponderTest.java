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
import ca.welbog.kevbot.communication.Request.Type;
import ca.welbog.kevbot.persist.KarmaFile;
import ca.welbog.kevbot.persist.RAHLFactory;

@RunWith(MockitoJUnitRunner.class)
public class KarmaResponderTest {
  
  KarmaResponder responder;

	@Mock	RAHLFactory mockRAHLFactory;
	@Mock	KarmaFile mockKarmaFile;

  @Before
  public void setUp() throws Exception {
  	Mockito.when(mockRAHLFactory.createKarmaFile(Mockito.anyString(), Mockito.anyString())).thenReturn(mockKarmaFile);
  	
    responder = new KarmaResponder(mockRAHLFactory);
  }
  
  @After
  public void tearDown() throws Exception {
    Mockito.verify(mockKarmaFile, Mockito.atMostOnce()).write();
    Mockito.verifyNoMoreInteractions(mockKarmaFile);
  }

  @Test
  public void testGetDocumentation() {
    Documentation documentation = responder.getDocumentation();
    assertNotNull(documentation);
    assertTrue(documentation.aliases.contains("karma"));
  }

  @Test
  public void testGetResponse_adjustKarmaUp() {
    Request request = createRequest();
    request.setMessage("adjusting something++ karma");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
    Mockito.verify(mockKarmaFile).changeKarma("something", 1);
  }

  @Test
  public void testGetResponse_adjustKarmaDown() {
    Request request = createRequest();
    request.setMessage("adjusting karma something--");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
    Mockito.verify(mockKarmaFile).changeKarma("something", -1);
  }

  @Test
  public void testGetResponse_adjustOwnKarma() {
    Request request = createRequest();
    request.setMessage("adjusting user++ karma");
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals("You cannot change your own karma.", response.getBody());
    Mockito.verifyNoInteractions(mockKarmaFile);
  }

  @Test
  public void testGetResponse_adjustKarmaInPM() {
    Request request = createRequest();
    request.setChannel("user");
    request.setMessage("adjusting something++ karma");
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertEquals("You cannot change karma in a private message.", response.getBody());
    Mockito.verifyNoInteractions(mockKarmaFile);
  }

  @Test
  public void testGetResponse_retrieveKarma() {
    Request request = createRequest();
    Mockito.when(mockKarmaFile.getKarma(Mockito.anyString())).thenReturn(5555);
    
    Response response = responder.getResponse(request);
    
    assertNotNull(response);
    assertTrue(response.getBody().contains("5555"));
    Mockito.verify(mockKarmaFile).getKarma("infoob");
  }

  @Test
  public void testGetResponse_whiff() {
  	// A test for a statement with no karma in it at all
    Request request = createRequest();
    request.setMessage("This is just a string.");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
    Mockito.verifyNoInteractions(mockKarmaFile);
  }

  @Test
  public void testGetResponse_noMatch() {
    Request request = createRequest();
    request.setMessage("jkarman sandiego");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
    Mockito.verifyNoInteractions(mockKarmaFile);
  }

  @Test
  public void testGetResponse_badArguments() {
    Request request = createRequest();
    request.setMessage("karma");
    
    Response response = responder.getResponse(request);
    
    assertNull(response);
    Mockito.verifyNoInteractions(mockKarmaFile);
  }

  private Request createRequest() {
    return new Request("medium", "channel", "user", "karma infoob", "someUser", AddressingMode.STANDARD, Type.MESSAGE);
  }
}
