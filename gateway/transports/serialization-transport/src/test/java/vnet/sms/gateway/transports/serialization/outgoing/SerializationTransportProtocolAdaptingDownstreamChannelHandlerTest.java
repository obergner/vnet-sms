package vnet.sms.gateway.transports.serialization.outgoing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestNackedEvent;
import vnet.sms.common.wme.receive.LoginRequestReceivedEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.common.wme.send.SendSmsContainer;
import vnet.sms.common.wme.send.SendSmsEvent;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class SerializationTransportProtocolAdaptingDownstreamChannelHandlerTest {

	private final SerializationTransportProtocolAdaptingDownstreamChannelHandler	objectUnderTest	= new SerializationTransportProtocolAdaptingDownstreamChannelHandler();

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPingRequestToPdu()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest,
		        new MessageEventWrappingDownstreamChannelHandler());

		embeddedPipeline.send(new PingRequest());
		final MessageEvent convertedMessageEvent = embeddedPipeline
		        .nextSentMessageEvent();

		assertNotNull(
		        "OutgoingMessagesMonitoringChannelHandler converted PingRequest to null output",
		        convertedMessageEvent);
		assertEquals(
		        "OutgoingMessagesMonitoringChannelHandler did not wrap converted PingRequest in DownstreamMessageEvent",
		        DownstreamMessageEvent.class, convertedMessageEvent.getClass());
		assertEquals(
		        "OutgoingMessagesMonitoringChannelHandler did not convert PingRequest to ReferenceableMessageContainer",
		        ReferenceableMessageContainer.class, convertedMessageEvent
		                .getMessage().getClass());
		assertEquals(
		        "OutgoingMessagesMonitoringChannelHandler did not put converted PingRequest in ReferenceableMessageContainer",
		        PingRequest.class,
		        ReferenceableMessageContainer.class
		                .cast(convertedMessageEvent.getMessage()).getMessage()
		                .getClass());
	}

	private static class MessageEventWrappingDownstreamChannelHandler extends
	        SimpleChannelDownstreamHandler {

		MessageEventWrappingDownstreamChannelHandler() {
		}

		@Override
		public void writeRequested(final ChannelHandlerContext ctx,
		        final MessageEvent e) throws Exception {
			final WindowedMessageEvent<Integer, ?> result;
			final Object message = e.getMessage();
			if (message instanceof PingRequest) {
				result = new SendPingRequestEvent<Integer>(Integer.valueOf(1),
				        e.getChannel(), (PingRequest) message);
			} else {
				throw new IllegalArgumentException("Unsupported message type: "
				        + message);
			}
			ctx.sendDownstream(result);
		}
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsAcceptedLoginRequestToPdu()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest,
		        new MessageEventWrappingDownstreamChannelHandler());

		final LoginRequest acceptedLoginRequest = new LoginRequest(
		        "assertThatTransportProtocolAdapterCorrectlyConvertsAcceptedLoginRequestToPdu",
		        "secret");
		final ReferenceableMessageContainer convertedMessageContainer = this.objectUnderTest
		        .convertLoginRequestAcceptedEventToPdu(ReceivedLoginRequestAckedEvent
		                .accept(new LoginRequestReceivedEvent<Integer>(1,
		                        new UpstreamMessageEvent(embeddedPipeline
		                                .getPipeline().getChannel(),
		                                acceptedLoginRequest,
		                                new InetSocketAddress(3)),
		                        acceptedLoginRequest)));

		assertNotNull(
		        "OutgoingMessagesMonitoringChannelHandler converted accepted LoginRequest to null output",
		        convertedMessageContainer);
		assertEquals(
		        "OutgoingMessagesMonitoringChannelHandler did not convert accepted LoginRequest to LoginResponse",
		        LoginResponse.class, convertedMessageContainer.getMessage()
		                .getClass());
		final LoginResponse loginResponse = convertedMessageContainer
		        .getMessage(LoginResponse.class);
		assertTrue(
		        "OutgoingMessagesMonitoringChannelHandler did not convert accepted LoginRequest to SUCCESSFUL LoginResponse",
		        loginResponse.loginSucceeded());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsRejectedLoginRequestToPdu()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest,
		        new MessageEventWrappingDownstreamChannelHandler());

		final LoginRequest rejectedLoginRequest = new LoginRequest(
		        "assertThatTransportProtocolAdapterCorrectlyConvertsRejectedLoginRequestToPdu",
		        "secret");
		final ReferenceableMessageContainer convertedMessageContainer = this.objectUnderTest
		        .convertLoginRequestRejectedEventToPdu(ReceivedLoginRequestNackedEvent
		                .reject(new LoginRequestReceivedEvent<Integer>(1,
		                        new UpstreamMessageEvent(embeddedPipeline
		                                .getPipeline().getChannel(),
		                                rejectedLoginRequest,
		                                new InetSocketAddress(3)),
		                        rejectedLoginRequest)));

		assertNotNull(
		        "OutgoingMessagesMonitoringChannelHandler converted rejected LoginRequest to null output",
		        convertedMessageContainer);
		assertEquals(
		        "OutgoingMessagesMonitoringChannelHandler did not convert rejected LoginRequest to LoginResponse",
		        LoginResponse.class, convertedMessageContainer.getMessage()
		                .getClass());
		final LoginResponse loginResponse = convertedMessageContainer
		        .getMessage(LoginResponse.class);
		assertFalse(
		        "OutgoingMessagesMonitoringChannelHandler did not convert rejected LoginRequest to FAILED LoginResponse",
		        loginResponse.loginSucceeded());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsSendSmsEventToPdu()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest,
		        new MessageEventWrappingDownstreamChannelHandler());

		final Sms moSms = new Sms(
		        "assertThatTransportProtocolAdapterCorrectlyConvertsSendSmsEventToPdu");
		final ReferenceableMessageContainer convertedMessageContainer = this.objectUnderTest
		        .convertSendSmsEventToPdu(SendSmsEvent
		                .convert(new DownstreamMessageEvent(embeddedPipeline
		                        .getChannel(), Channels.future(embeddedPipeline
		                        .getChannel()), new SendSmsContainer(moSms),
		                        null)));

		assertNotNull(
		        "OutgoingMessagesMonitoringChannelHandler converted SendSmsEvent to null output",
		        convertedMessageContainer);
		assertEquals(
		        "OutgoingMessagesMonitoringChannelHandler did not convert SendSmsEvent to Sms",
		        Sms.class, convertedMessageContainer.getMessage().getClass());
	}
}
