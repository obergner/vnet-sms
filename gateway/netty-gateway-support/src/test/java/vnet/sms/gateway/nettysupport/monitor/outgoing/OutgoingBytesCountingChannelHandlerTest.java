package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;

import com.yammer.metrics.Metrics;

public class OutgoingBytesCountingChannelHandlerTest {

	private final OutgoingBytesCountingChannelHandler	objectUnderTest	= new OutgoingBytesCountingChannelHandler(
	                                                                            Metrics.defaultRegistry());

	@Test
	public final void assertThatWriteRequestedCorrectlyUpdatesNumberOfSentBytes()
	        throws Throwable {
		final byte[] sentBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline.send(ChannelBuffers.copiedBuffer(sentBytes));

		assertEquals(
		        "OutgoingBytesCountingChannelHandler did not correctly count number of sent bytes",
		        sentBytes.length, this.objectUnderTest
		                .getTotalNumberOfSentBytes().count());
	}
}
