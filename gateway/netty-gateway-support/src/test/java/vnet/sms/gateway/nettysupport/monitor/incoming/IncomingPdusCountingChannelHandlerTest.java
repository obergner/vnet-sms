package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.gateway.nettysupport.monitor.DefaultChannelMonitor;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;

public class IncomingPdusCountingChannelHandlerTest {

	private static class SimpleChannelMonitorCallback extends
	        DefaultChannelMonitor {

		final AtomicLong	numberOfReceivedPdus	= new AtomicLong(0);

		void reset() {
			this.numberOfReceivedPdus.set(0);
		}

		@Override
		public void pduReceived() {
			this.numberOfReceivedPdus.incrementAndGet();
		}
	}

	private final SimpleChannelMonitorCallback	              monitorCallback	= new SimpleChannelMonitorCallback();

	private final IncomingPdusCountingChannelHandler<Message>	objectUnderTest	= new IncomingPdusCountingChannelHandler<Message>(
	                                                                                    Message.class);

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
	public final void assertThatMessageReceivedCorrectlyUpdatesNumberOfReceivedPdus()
	        throws Throwable {
		final int numberOfReceivedPdus = 32;
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);

		for (int i = 0; i < numberOfReceivedPdus; i++) {
			embeddedPipeline.receive(new PingRequest());
		}

		assertEquals(
		        "IncomingPdusCountingChannelHandler did not correctly count number of received PDUs",
		        numberOfReceivedPdus,
		        this.monitorCallback.numberOfReceivedPdus.get());
	}
}
