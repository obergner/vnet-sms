package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.nettysupport.monitor.DefaultChannelMonitor;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;

public class IncomingMessagesMonitoringChannelHandlerTest {

	private static class SimpleChannelMonitorCallback extends
	        DefaultChannelMonitor {

		final AtomicLong	numberOfReceivedLoginRequests	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedLoginResponses	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedPingRequests	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedPingResponses	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedSms		       = new AtomicLong(0);

		void reset() {
			this.numberOfReceivedSms.set(0);
			this.numberOfReceivedLoginRequests.set(0);
			this.numberOfReceivedLoginResponses.set(0);
			this.numberOfReceivedPingRequests.set(0);
			this.numberOfReceivedPingResponses.set(0);
		}

		@Override
		public void loginRequestReceived() {
			this.numberOfReceivedLoginRequests.incrementAndGet();
		}

		@Override
		public void loginResponseReceived() {
			this.numberOfReceivedLoginResponses.incrementAndGet();
		}

		@Override
		public void pingRequestReceived() {
			this.numberOfReceivedPingRequests.incrementAndGet();
		}

		@Override
		public void pingResponseReceived() {
			this.numberOfReceivedPingResponses.incrementAndGet();
		}

		@Override
		public void smsReceived() {
			this.numberOfReceivedSms.incrementAndGet();
		}
	}

	private final SimpleChannelMonitorCallback	                    monitorCallback	= new SimpleChannelMonitorCallback();

	private final IncomingMessagesMonitoringChannelHandler<Integer>	objectUnderTest	= new IncomingMessagesMonitoringChannelHandler<Integer>();

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
	public final void assertThatTransportProtocolAdapterCorrectlyUpdatesNumberOfReceivedLoginRequests() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(new LoginRequest(
		                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                "secret"));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received login requests",
		        1L, this.monitorCallback.numberOfReceivedLoginRequests.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedLoginResponses() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(LoginResponse
		                .accept(new LoginRequest(
		                        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                        "secret")));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received login responses",
		        1L, this.monitorCallback.numberOfReceivedLoginResponses.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedPingRequests() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);

		embeddedPipeline.offer(new PingRequest());

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received ping requests",
		        1L, this.monitorCallback.numberOfReceivedPingRequests.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedPingResponses() {

		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);

		embeddedPipeline.offer(PingResponse.accept(new PingRequest()));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received ping responses",
		        1L, this.monitorCallback.numberOfReceivedPingResponses.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedSms() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        this.objectUnderTest);

		embeddedPipeline.offer(new Sms(
		        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms"));

		assertEquals(
		        "IncomingMessagesMonitoringChannelHandler did not correctly count number of received sms",
		        1L, this.monitorCallback.numberOfReceivedSms.get());
	}
}
