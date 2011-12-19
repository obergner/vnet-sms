package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.After;
import org.junit.Test;

import vnet.sms.gateway.nettysupport.monitor.DefaultChannelMonitorCallback;
import vnet.sms.gateway.nettysupport.monitor.TestChannelMonitorRegistry;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;

public class IncomingBytesCountingChannelHandlerTest {

	private static class SimpleChannelMonitorCallback extends
	        DefaultChannelMonitorCallback {

		final AtomicLong	numberOfReceivedBytes	= new AtomicLong(0);

		void reset() {
			this.numberOfReceivedBytes.set(0);
		}

		@Override
		public void bytesReceived(final long numberOfBytes) {
			this.numberOfReceivedBytes.addAndGet(numberOfBytes);
		}
	}

	private final SimpleChannelMonitorCallback	      monitorCallback	= new SimpleChannelMonitorCallback();

	private final IncomingBytesCountingChannelHandler	objectUnderTest	= new IncomingBytesCountingChannelHandler(
	                                                                            new TestChannelMonitorRegistry(
	                                                                                    this.monitorCallback));

	@After
	public void resetMonitor() {
		this.monitorCallback.reset();
	}

	@Test
	public final void assertThatMessageReceivedCorrectlyUpdatesNumberOfReceivedBytes()
	        throws Throwable {
		final byte[] receivedBytes = new byte[] { 1, 2, 3 };
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		embeddedPipeline.receive(ChannelBuffers.copiedBuffer(receivedBytes));

		assertEquals(
		        "IncomingBytesCountingChannelHandler did not correctly count number of received bytes",
		        receivedBytes.length,
		        this.monitorCallback.numberOfReceivedBytes.get());
	}
}
