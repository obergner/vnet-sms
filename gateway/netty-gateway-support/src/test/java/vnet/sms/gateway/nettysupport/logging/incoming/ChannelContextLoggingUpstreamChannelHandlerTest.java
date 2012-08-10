package vnet.sms.gateway.nettysupport.logging.incoming;

import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.slf4j.MDC;

import vnet.sms.common.messages.PingRequest;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;

public class ChannelContextLoggingUpstreamChannelHandlerTest {

	@Test
	public final void assertThatChannelContextLoggingUpstreamChannelHandlerRemovedCurrentChannelFromMDCAfterReturning()
	        throws Throwable {
		final ChannelContextLoggingUpstreamChannelHandler objectUnderTest = new ChannelContextLoggingUpstreamChannelHandler();

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
		final PingRequest pingRequest = new PingRequest();
		embeddedPipeline.receive(pingRequest);
		final String currentChannelInMdc = MDC
		        .get(ChannelContextLoggingUpstreamChannelHandler.CURRENT_CHANNEL_MDC_KEY);

		assertNull(
		        "ChannelContextLoggingUpstreamChannelHandler did not remove current channel from MDC after returning",
		        currentChannelInMdc);
	}
}
