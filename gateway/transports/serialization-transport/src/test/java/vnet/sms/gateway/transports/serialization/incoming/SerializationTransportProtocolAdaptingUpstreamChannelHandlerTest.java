package vnet.sms.gateway.transports.serialization.incoming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class SerializationTransportProtocolAdaptingUpstreamChannelHandlerTest {

	private final SerializationTransportProtocolAdaptingUpstreamChannelHandler	objectUnderTest	= new SerializationTransportProtocolAdaptingUpstreamChannelHandler();

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline
		        .receive(ReferenceableMessageContainer
		                .wrap(1,
		                        new LoginRequest(
		                                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                                "secret", new InetSocketAddress(1),
		                                new InetSocketAddress(1))));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginRequest to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginRequest to unexpected output",
		        LoginRequestReceivedEvent.class, convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginResponse()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline
		        .receive(ReferenceableMessageContainer.wrap(
		                1,
		                LoginResponse
		                        .accept(new LoginRequest(
		                                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                                "secret", new InetSocketAddress(1),
		                                new InetSocketAddress(1)))));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginResponse to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted LoginResponse to unexpected output",
		        LoginResponseReceivedEvent.class, convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingRequest()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline.receive(ReferenceableMessageContainer.wrap(1,
		        new PingRequest(new InetSocketAddress(1),
		                new InetSocketAddress(1))));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingRequest to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingRequest to unexpected output",
		        PingRequestReceivedEvent.class, convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingResponse()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline.receive(ReferenceableMessageContainer.wrap(1,
		        PingResponse.accept(new PingRequest(new InetSocketAddress(2),
		                new InetSocketAddress(3)))));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingResponse to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted PingResponse to unexpected output",
		        PingResponseReceivedEvent.class, convertedPduEvent.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline.receive(ReferenceableMessageContainer.wrap(1, new Sms(
		        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms",
		        new InetSocketAddress(1), new InetSocketAddress(2))));
		final MessageEvent convertedPduEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted Sms to null output",
		        convertedPduEvent);
		assertEquals(
		        "SerializationTransportProtocolAdaptingUpstreamChannelHandler converted Sms to unexpected output",
		        SmsReceivedEvent.class, convertedPduEvent.getClass());
	}
}
