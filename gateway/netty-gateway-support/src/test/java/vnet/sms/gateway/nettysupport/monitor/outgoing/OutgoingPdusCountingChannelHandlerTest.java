package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;

import com.yammer.metrics.Metrics;

public class OutgoingPdusCountingChannelHandlerTest {

	private final OutgoingPdusCountingChannelHandler<GsmPdu>	objectUnderTest	= new OutgoingPdusCountingChannelHandler<GsmPdu>(
	                                                                                    GsmPdu.class,
	                                                                                    Metrics.defaultRegistry());

	@Test
	public final void assertThatWriteRequestedCorrectlyUpdatesNumberOfSentPdus()
	        throws Throwable {
		final int numberOfSentPdus = 32;
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		for (int i = 0; i < numberOfSentPdus; i++) {
			embeddedPipeline.send(new PingRequest());
		}

		assertEquals(
		        "OutgoingPdusCountingChannelHandler did not correctly count number of sent PDUs",
		        numberOfSentPdus, this.objectUnderTest.getNumberOfSentPdus()
		                .count());
	}
}
