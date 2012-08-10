package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;

import com.yammer.metrics.Metrics;

public class IncomingPdusCountingChannelHandlerTest {

	private final IncomingPdusCountingChannelHandler<GsmPdu>	objectUnderTest	= new IncomingPdusCountingChannelHandler<GsmPdu>(
	                                                                                    GsmPdu.class,
	                                                                                    Metrics.defaultRegistry());

	@Test
	public final void assertThatMessageReceivedCorrectlyUpdatesNumberOfReceivedPdus()
	        throws Throwable {
		final int numberOfReceivedPdus = 32;
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		for (int i = 0; i < numberOfReceivedPdus; i++) {
			embeddedPipeline.receive(new PingRequest());
		}

		assertEquals(
		        "IncomingPdusCountingChannelHandler did not correctly count number of received PDUs",
		        numberOfReceivedPdus, this.objectUnderTest
		                .getNumberOfReceivedPdus().count());
	}
}
