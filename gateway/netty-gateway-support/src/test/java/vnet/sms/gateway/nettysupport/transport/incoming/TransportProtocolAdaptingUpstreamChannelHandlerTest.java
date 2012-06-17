package vnet.sms.gateway.nettysupport.transport.incoming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.netty.handler.codec.embedder.CodecEmbedderException;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Test;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;

public class TransportProtocolAdaptingUpstreamChannelHandlerTest {

	private final TransportProtocolAdaptingUpstreamChannelHandler<Integer, GsmPdu>	objectUnderTest	= new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler();

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest() {
		final DecoderEmbedder<GsmPdu> embeddedPipeline = new DecoderEmbedder<GsmPdu>(
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(new LoginRequest(
		                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                "secret"));
		final GsmPdu convertedPdu = embeddedPipeline.poll();
		embeddedPipeline.finish();

		assertNotNull(
		        "IncomingMessagesMonitoringChannelHandler converted LoginRequest to null output",
		        convertedPdu);
		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler converted LoginRequest to unexpected output",
		        LoginRequest.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginResponse() {
		final DecoderEmbedder<GsmPdu> embeddedPipeline = new DecoderEmbedder<GsmPdu>(
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(LoginResponse
		                .accept(new LoginRequest(
		                        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                        "secret")));
		final GsmPdu convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "IncomingMessagesMonitoringChannelHandler converted LoginResponse to null output",
		        convertedPdu);
		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler converted LoginResponse to unexpected output",
		        LoginResponse.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingRequest() {
		final DecoderEmbedder<GsmPdu> embeddedPipeline = new DecoderEmbedder<GsmPdu>(
		        this.objectUnderTest);

		embeddedPipeline.offer(new PingRequest());
		final GsmPdu convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "IncomingMessagesMonitoringChannelHandler converted PingRequest to null output",
		        convertedPdu);
		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler converted PingRequest to unexpected output",
		        PingRequest.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingResponse() {
		final DecoderEmbedder<GsmPdu> embeddedPipeline = new DecoderEmbedder<GsmPdu>(
		        this.objectUnderTest);

		embeddedPipeline.offer(PingResponse.accept(new PingRequest()));
		final GsmPdu convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "IncomingMessagesMonitoringChannelHandler converted PingResponse to null output",
		        convertedPdu);
		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler converted PingResponse to unexpected output",
		        PingResponse.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms() {
		final DecoderEmbedder<GsmPdu> embeddedPipeline = new DecoderEmbedder<GsmPdu>(
		        this.objectUnderTest);

		embeddedPipeline.offer(new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms"));
		final GsmPdu convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "IncomingMessagesMonitoringChannelHandler converted Sms to null output",
		        convertedPdu);
		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler converted Sms to unexpected output",
		        Sms.class, convertedPdu.getClass());
	}

	@Test(expected = CodecEmbedderException.class)
	public final void assertThatTransportProtocolAdapterRejectsUnknownPduType() {
		final DecoderEmbedder<GsmPdu> embeddedPipeline = new DecoderEmbedder<GsmPdu>(
		        this.objectUnderTest);
		final Object unknownPdu = new Object();

		embeddedPipeline.offer(unknownPdu);
	}
}
