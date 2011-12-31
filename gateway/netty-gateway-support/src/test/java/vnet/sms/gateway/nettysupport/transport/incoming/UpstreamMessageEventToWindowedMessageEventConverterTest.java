package vnet.sms.gateway.nettysupport.transport.incoming;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
import vnet.sms.common.wme.WindowedMessageEvent;

public class UpstreamMessageEventToWindowedMessageEventConverterTest {

	@Test
	public final void assertThatConvertConvertsLoginRequestToLoginRequestReceivedEvent() {
		final LoginRequest inputMessage = new LoginRequest(
		        "assertThatConvertConvertsLoginRequestToLoginRequestReceivedEvent",
		        "secret", new InetSocketAddress(1), new InetSocketAddress(1));
		final Class<?> expectedOutputType = LoginRequestReceivedEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	private void assertCorrectConversion(final Message inputMessage,
	        final Class<?> expectedOutputType) {
		final Integer messageReference = Integer.valueOf(1);
		final UpstreamMessageEvent upstreamMessageEvent = newUpstreamMessageEvent(inputMessage);

		final WindowedMessageEvent<Integer, ? extends Message> convertedMessage = UpstreamMessageEventToWindowedMessageEventConverter.INSTANCE
		        .convert(messageReference, upstreamMessageEvent, inputMessage);

		assertNotNull("convert(" + messageReference + ", "
		        + upstreamMessageEvent + ", " + inputMessage
		        + ") returned null", convertedMessage);
		assertEquals("convert(" + messageReference + ", "
		        + upstreamMessageEvent + ", " + inputMessage
		        + ") returned wrong message event type", expectedOutputType,
		        convertedMessage.getClass());
	}

	private UpstreamMessageEvent newUpstreamMessageEvent(final Message message) {
		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        mockChannel, message, new InetSocketAddress(1));
		return upstreamMessageEvent;
	}

	@Test
	public final void assertThatConvertConvertsLoginResponseToLoginResponseReceivedEvent() {
		final LoginResponse inputMessage = LoginResponse
		        .accept(new LoginRequest(
		                "assertThatConvertConvertsLoginResponseToLoginResponseReceivedEvent",
		                "secret", new InetSocketAddress(1),
		                new InetSocketAddress(1)));
		final Class<?> expectedOutputType = LoginResponseReceivedEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	@Test
	public final void assertThatConvertConvertsPingRequestToPingRequestReceivedEvent() {
		final PingRequest inputMessage = new PingRequest(new InetSocketAddress(
		        1), new InetSocketAddress(1));
		final Class<?> expectedOutputType = PingRequestReceivedEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	@Test
	public final void assertThatConvertConvertsPingResponseToPingResponseReceivedEvent() {
		final PingResponse inputMessage = PingResponse.accept(new PingRequest(
		        new InetSocketAddress(1), new InetSocketAddress(1)));
		final Class<?> expectedOutputType = PingResponseReceivedEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	@Test
	public final void assertThatConvertConvertsSmsToSmsReceivedEvent() {
		final Sms inputMessage = new Sms(
		        "assertThatConvertConvertsSmsToSmsReceivedEvent",
		        new InetSocketAddress(0), new InetSocketAddress(1));
		final Class<?> expectedOutputType = SmsReceivedEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}
}
