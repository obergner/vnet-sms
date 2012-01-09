package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vnet.sms.gateway.nettysupport.monitor.DefaultChannelMonitor;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;

public class OutgoingBytesCountingChannelHandlerTest {

	private static class SimpleChannelMonitorCallback extends
	        DefaultChannelMonitor {

		final AtomicLong	numberOfSentBytes	= new AtomicLong(0);

		void reset() {
			this.numberOfSentBytes.set(0);
		}

		@Override
		public void sendBytes(final long numberOfBytes) {
			this.numberOfSentBytes.addAndGet(numberOfBytes);
		}
	}

	private final SimpleChannelMonitorCallback	      monitorCallback	= new SimpleChannelMonitorCallback();

	private final OutgoingBytesCountingChannelHandler	objectUnderTest	= new OutgoingBytesCountingChannelHandler();

	@Before
	public void addMonitor() {
		this.objectUnderTest.addMonitor(this.monitorCallback);
	}

	@After
	public void resetMonitor() {
		this.monitorCallback.reset();
		this.objectUnderTest.clearMonitors();
	}

	@Test
	public final void assertThatWriteRequestedCorrectlyUpdatesNumberOfSentBytes()
	        throws Throwable {
		final byte[] sentBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline.send(ChannelBuffers.copiedBuffer(sentBytes));

		assertEquals(
		        "OutgoingBytesCountingChannelHandler did not correctly count number of sent bytes",
		        sentBytes.length, this.monitorCallback.numberOfSentBytes.get());
	}
}
