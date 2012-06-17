package vnet.sms.gateway.nettysupport.transport.outgoing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.junit.Test;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;

public class TransportProtocolAdaptingDownstreamChannelHandlerTest {

	private final TransportProtocolAdaptingDownstreamChannelHandler<Integer, GsmPdu>	objectUnderTest	= new ObjectSerializationTransportProtocolAdaptingDownstreamChannelHandler();

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
}
