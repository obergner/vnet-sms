package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;

import com.yammer.metrics.Metrics;

public class IncomingMessagesMonitoringChannelHandlerTest {

	private final IncomingMessagesMonitoringChannelHandler<Integer>	objectUnderTest	= new IncomingMessagesMonitoringChannelHandler<Integer>(
	                                                                                        Metrics.defaultRegistry());

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyUpdatesNumberOfReceivedLoginRequests()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatTransportProtocolAdapterCorrectlyUpdatesNumberOfReceivedLoginRequests",
		                "secret"));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received login requests",
		        1L, this.objectUnderTest.getNumberOfReceivedLoginRequests()
		                .count());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedLoginResponses()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline
		        .receive(LoginResponse
		                .accept(new LoginRequest(
		                        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                        "secret")));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received login responses",
		        1L, this.objectUnderTest.getNumberOfReceivedLoginResponses()
		                .count());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedPingRequests()
	        throws Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline.receive(new PingRequest());

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received ping requests",
		        1L, this.objectUnderTest.getNumberOfReceivedPingRequests()
		                .count());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedPingResponses()
	        throws Throwable {

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline.receive(PingResponse.accept(new PingRequest()));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received ping responses",
		        1L, this.objectUnderTest.getNumberOfReceivedPingResponses()
		                .count());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedSms()
	        throws IllegalArgumentException, Throwable {
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);
		embeddedPipeline.connectChannel();

		embeddedPipeline.receive(new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms"));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received sms",
		        1L, this.objectUnderTest.getNumberOfReceivedSms().count());
	}
}
