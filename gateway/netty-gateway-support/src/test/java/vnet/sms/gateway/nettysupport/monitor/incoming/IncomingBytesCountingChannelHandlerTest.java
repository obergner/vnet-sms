package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;

import com.yammer.metrics.Metrics;

public class IncomingBytesCountingChannelHandlerTest {

	private final IncomingBytesCountingChannelHandler	objectUnderTest	= new IncomingBytesCountingChannelHandler(
	                                                                            Metrics.defaultRegistry());

	@Test
	public final void assertThatMessageReceivedCorrectlyUpdatesNumberOfReceivedBytes()
	        throws Throwable {
		final byte[] receivedBytes = new byte[] { 1, 2, 3 };
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline.receive(ChannelBuffers.copiedBuffer(receivedBytes));

		assertEquals(
		        "IncomingBytesCountingChannelHandler did not correctly count number of received bytes",
		        receivedBytes.length, this.objectUnderTest
		                .getTotalNumberOfReceivedBytes().count());
	}
}
