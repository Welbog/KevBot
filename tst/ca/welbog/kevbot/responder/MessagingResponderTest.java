package ca.welbog.kevbot.responder;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ca.welbog.kevbot.communication.AddressingMode;
import ca.welbog.kevbot.communication.Documentation;
import ca.welbog.kevbot.communication.Request;
import ca.welbog.kevbot.communication.Response;
import ca.welbog.kevbot.persist.RAHLFactory;
import ca.welbog.kevbot.persist.SingleFile;
import ca.welbog.kevbot.communication.Request.Type;

@RunWith(MockitoJUnitRunner.class)
public class MessagingResponderTest {

	MessagingResponder responder;

	@Mock	RAHLFactory mockRAHLFactory;
	@Mock	SingleFile mockMetaFile;
	@Mock	SingleFile mockMessagesFile;

	@Before
	public void setUp() throws Exception {
		Mockito.when(mockRAHLFactory.createSingleFile("messages.txt")).thenReturn(mockMetaFile);
		Mockito.when(mockRAHLFactory.createSingleFile("infoob.messages")).thenReturn(mockMessagesFile);
		Mockito.when(mockRAHLFactory.createSingleFile("someUser.messages")).thenReturn(mockMessagesFile);

		responder = new MessagingResponder(mockRAHLFactory);
	}

	@After
	public void tearDown() throws Exception {
		Mockito.verify(mockRAHLFactory).createSingleFile("messages.txt");
	}

	@Test
	public void testGetDocumentation() {
		Documentation documentation = responder.getDocumentation();
		assertNotNull(documentation);
		assertTrue(documentation.aliases.contains("message"));
		assertTrue(documentation.aliases.contains("check"));
	}

	@Test
	public void testGetResponse_leaveNewMessage() {
		Request request = createRequest();

		Response response = responder.getResponse(request);

		assertNotNull(response);
		Mockito.verify(mockMetaFile).add("infoob");
		Mockito.verify(mockMessagesFile).add(Mockito.anyString());
	}

	@Test
	public void testGetResponse_leaveAppendedMessage() {
		Request request = createRequest();

		Response response = responder.getResponse(request);

		assertNotNull(response);
		Mockito.verify(mockMessagesFile).add(Mockito.anyString());
	}

	@Test
	public void testGetResponse_getNoMessages() {
		Mockito.when(mockMetaFile.exists("someUser")).thenReturn(false);
		Request request = createRequest();
		request.setMessage("check");

		Response response = responder.getResponse(request);

		assertNotNull(response);
		assertEquals("You do not have any messages.", response.getBody());
		Mockito.verify(mockMetaFile).exists("someUser");
	}

	@Test
	public void testGetResponse_getSomeMessages() {
		Mockito.when(mockMetaFile.exists("someUser")).thenReturn(true);
		Mockito.when(mockMessagesFile.getLine(0)).thenReturn("0 First line");
		Mockito.when(mockMessagesFile.getLine(1)).thenReturn("1 Second line");
		Mockito.when(mockMessagesFile.getLine(2)).thenReturn("2 Third line");
		// Needed because of the weird way this old code works, relying on the end-of-file newline.
		Mockito.when(mockMessagesFile.getLine(3)).thenReturn("");
		
		Request request = createRequest();
		request.setMessage("check");

		Response response = responder.getResponse(request);

		assertNotNull(response);
		// 4 lines: one for each message (3) and one statement that the list is over.
		assertEquals(4, response.getBody().split("\n").length);
		Mockito.verify(mockMetaFile).exists("someUser");
		Mockito.verify(mockMessagesFile, Mockito.times(4)).getLine(Mockito.anyInt());
		Mockito.verify(mockMessagesFile).deleteAll();
	}

	@Test
	public void testGetResponse_noMatch() {
		Request request = createRequest();
		request.setMessage("notchecknope");

		Response response = responder.getResponse(request);

		assertNull(response);
	}

	@Test
	public void testGetResponse_wrongArguments() {
		Request request = createRequest();
		request.setMessage("message infoob");

		Response response = responder.getResponse(request);

		assertNull(response);
	}

	@Test
	public void testGetResponse_notPrivateMessage() {
		Request request = createRequest();
		request.setChannel("someChannel");

		Response response = responder.getResponse(request);

		assertNull(response);
	}

	private Request createRequest() {
		return new Request("medium", "someUser", "someUser", "message infoob some message", "kevbot",
				AddressingMode.STANDARD, Type.MESSAGE);
	}
}
