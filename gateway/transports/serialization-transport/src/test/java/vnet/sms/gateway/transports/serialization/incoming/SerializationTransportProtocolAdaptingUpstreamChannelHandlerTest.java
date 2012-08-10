package vnet.sms.gateway.transports.serialization.incoming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.receive.ReceivedLoginRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedLoginRequestEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestEvent;
import vnet.sms.common.wme.receive.ReceivedSmsEvent;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class SerializationTransportProtocolAdaptingUpstreamChannelHandlerTest {

	private final SerializationTransportProtocolAdaptingUpstreamChannelHandler	objectUnderTest	= new SerializationTransportProtocolAdaptingUpstreamChannelHandler();

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline
		        .receive(ReferenceableMessageContainer
		                .wrap(1,
		                        new LoginRequest(
		                                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                                "secret")));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .upstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginRequest to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginRequest to unexpected output",
		        ReceivedLoginRequestEvent.class, convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginResponse()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline
		        .receive(ReferenceableMessageContainer.wrap(
		                1,
		                LoginResponse
		                        .accept(new LoginRequest(
		                                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                                "secret"))));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .upstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginResponse to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginResponse to unexpected output",
		        ReceivedLoginRequestAcknowledgementEvent.class,
		        convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingRequest()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline.receive(ReferenceableMessageContainer.wrap(1,
		        new PingRequest()));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .upstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingRequest to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingRequest to unexpected output",
		        ReceivedPingRequestEvent.class, convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingResponse()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline.receive(ReferenceableMessageContainer.wrap(1,
		        PingResponse.accept(new PingRequest())));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .upstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingResponse to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingResponse to unexpected output",
		        ReceivedPingRequestAcknowledgementEvent.class,
		        convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline
		        .receive(ReferenceableMessageContainer
		                .wrap(1,
		                        new Sms(new Msisdn("01686754432"), new Msisdn(
		                                "01686754432"),
		                                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms")));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .upstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted Sms to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted Sms to unexpected output",
		        ReceivedSmsEvent.class, convertedPduEvent.getClass());
	}
}
