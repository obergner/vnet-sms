package vnet.sms.gateway.transports.serialization.outgoing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.junit.Test;

import vnet.sms.common.messages.PingRequest;
import vnet.sms.gateway.nettysupport.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.WindowedMessageEvent;
import vnet.sms.gateway.nettysupport.monitor.TestChannelMonitorRegistry;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

public class TransportProtocolAdaptingDownstreamChannelHandlerTest {

	private final SerializationTransportProtocolAdaptingDownstreamChannelHandler	objectUnderTest	= new SerializationTransportProtocolAdaptingDownstreamChannelHandler(
	                                                                                                        new TestChannelMonitorRegistry());

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPingRequestToPdu()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest,
		        new MessageEventWrappingDownstreamChannelHandler());

		embeddedPipeline.send(new PingRequest(new InetSocketAddress(1),
		        new InetSocketAddress(1)));
		final MessageEvent convertedMessageEvent = embeddedPipeline
		        .nextSentMessageEvent();

		assertNotNull(
		        "TransportProtocolAdaptingDownstreamChannelHandler converted PingRequest to null output",
		        convertedMessageEvent);
		assertEquals(
		        "TransportProtocolAdaptingDownstreamChannelHandler did not wrap converted PingRequest in DownstreamMessageEvent",
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
				        e.getChannel(), (PingRequest) message,
				        e.getRemoteAddress());
			} else {
				throw new IllegalArgumentException("Unsupported message type: "
				        + message);
			}
			ctx.sendDownstream(result);
		}
	}
}
