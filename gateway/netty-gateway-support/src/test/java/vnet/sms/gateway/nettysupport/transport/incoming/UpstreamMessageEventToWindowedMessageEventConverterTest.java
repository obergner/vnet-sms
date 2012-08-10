package vnet.sms.gateway.nettysupport.transport.incoming;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.receive.ReceivedLoginRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedLoginRequestEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestEvent;
import vnet.sms.common.wme.receive.ReceivedSmsEvent;

public class UpstreamMessageEventToWindowedMessageEventConverterTest {

	@Test
	public final void assertThatConvertConvertsLoginRequestToLoginRequestReceivedEvent() {
		final LoginRequest inputMessage = new LoginRequest(
		        "assertThatConvertConvertsLoginRequestToLoginRequestReceivedEvent",
		        "secret");
		final Class<?> expectedOutputType = ReceivedLoginRequestEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	private void assertCorrectConversion(final GsmPdu inputMessage,
	        final Class<?> expectedOutputType) {
		final Integer messageReference = Integer.valueOf(1);
		final UpstreamMessageEvent upstreamMessageEvent = newUpstreamMessageEvent(inputMessage);

		final WindowedMessageEvent<Integer, ? extends GsmPdu> convertedMessage = UpstreamMessageEventToWindowedMessageEventConverter.INSTANCE
		        .convert(messageReference, upstreamMessageEvent, inputMessage);

		assertNotNull("convert(" + messageReference + ", "
		        + upstreamMessageEvent + ", " + inputMessage
		        + ") returned null", convertedMessage);
		assertEquals("convert(" + messageReference + ", "
		        + upstreamMessageEvent + ", " + inputMessage
		        + ") returned wrong message event type", expectedOutputType,
		        convertedMessage.getClass());
	}

	private UpstreamMessageEvent newUpstreamMessageEvent(final GsmPdu gsmPdu) {
		final Channel mockChannel = createNiceMock(Channel.class);
		replay(mockChannel);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        mockChannel, gsmPdu, new InetSocketAddress(1));
		return upstreamMessageEvent;
	}

	@Test
	public final void assertThatConvertConvertsLoginResponseToLoginResponseReceivedEvent() {
		final LoginResponse inputMessage = LoginResponse
		        .accept(new LoginRequest(
		                "assertThatConvertConvertsLoginResponseToLoginResponseReceivedEvent",
		                "secret"));
		final Class<?> expectedOutputType = ReceivedLoginRequestAcknowledgementEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	@Test
	public final void assertThatConvertConvertsPingRequestToPingRequestReceivedEvent() {
		final PingRequest inputMessage = new PingRequest();
		final Class<?> expectedOutputType = ReceivedPingRequestEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	@Test
	public final void assertThatConvertConvertsPingResponseToPingResponseReceivedEvent() {
		final PingResponse inputMessage = PingResponse
		        .accept(new PingRequest());
		final Class<?> expectedOutputType = ReceivedPingRequestAcknowledgementEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}

	@Test
	public final void assertThatConvertConvertsSmsToSmsReceivedEvent() {
		final Sms inputMessage = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatConvertConvertsSmsToSmsReceivedEvent");
		final Class<?> expectedOutputType = ReceivedSmsEvent.class;

		assertCorrectConversion(inputMessage, expectedOutputType);
	}
}
