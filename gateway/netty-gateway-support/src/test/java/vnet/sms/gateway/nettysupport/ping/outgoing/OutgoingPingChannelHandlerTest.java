package vnet.sms.gateway.nettysupport.ping.outgoing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;
import vnet.sms.gateway.nettytest.ChannelEventFilter;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;

public class OutgoingPingChannelHandlerTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullWindowIdGenerator() {
		new OutgoingPingChannelHandler<Integer>(10, 10, null);
	}

	@Test
	public final void assertThatOutgoingPingChannelHandlerSendsPingAfterChannelHasBeenAuthenticatedAndPingIntervalElapsed()
	        throws InterruptedException {
		final int pingIntervalSeconds = 1;
		final int pingTimeoutMillis = 20000;
		final OutgoingPingChannelHandler<Integer> objectUnderTest = new OutgoingPingChannelHandler<Integer>(
		        pingIntervalSeconds, pingTimeoutMillis,
		        new TestWindowIdGenerator());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		// Simulate successful channel authentication => we start to ping after
		// pingIntervalSeconds
		embeddedPipeline
		        .injectUpstreamChannelEvent(new ChannelSuccessfullyAuthenticatedEvent(
		                embeddedPipeline.getChannel(),
		                new LoginRequest(
		                        "assertThatOutgoingPingChannelHandlerSendsPingAfterChannelHasBeenAuthenticatedAndPingIntervalElapsed",
		                        "password")));
		Thread.sleep(pingIntervalSeconds * 1000 + 100);
		final MessageEvent pingRequestCandidate = embeddedPipeline
		        .nextSentMessageEvent();

		assertNotNull(
		        "OutgoingPingChannelHandler did not send any message after channel has been authenticated and ping interval elapsed",
		        pingRequestCandidate);
		assertEquals(
		        "OutgoingPingChannelHandler sent wrong message event after channel has been authenticated and ping interval elapsed",
		        SendPingRequestEvent.class, pingRequestCandidate.getClass());
	}

	@Test
	public final void assertThatOutgoingPingChannelHandlerSendsStarteToPingEventUpstreamAfterChannelHasBeenAuthenticatedAndPingIntervalElapsed()
	        throws InterruptedException {
		final int pingIntervalSeconds = 1;
		final int pingTimeoutMillis = 20000;
		final OutgoingPingChannelHandler<Integer> objectUnderTest = new OutgoingPingChannelHandler<Integer>(
		        pingIntervalSeconds, pingTimeoutMillis,
		        new TestWindowIdGenerator());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		// Simulate successful channel authentication => we start to ping after
		// pingIntervalSeconds
		embeddedPipeline
		        .injectUpstreamChannelEvent(new ChannelSuccessfullyAuthenticatedEvent(
		                embeddedPipeline.getChannel(),
		                new LoginRequest(
		                        "assertThatOutgoingPingChannelHandlerSendsStarteToPingEventUpstreamAfterChannelHasBeenAuthenticatedAndPingIntervalElapsed",
		                        "password")));
		Thread.sleep(pingIntervalSeconds * 1000 + 100);
		final ChannelEvent startedToPing = embeddedPipeline
		        .nextUpstreamChannelEvent(new ChannelEventFilter() {
			        @Override
			        public boolean matches(final ChannelEvent event) {
				        return event instanceof StartedToPingEvent;
			        }
		        });

		assertNotNull(
		        "OutgoingPingChannelHandler did not send "
		                + StartedToPingEvent.class.getName()
		                + " after channel has been authenticated and ping interval elapsed",
		        startedToPing);
	}

	@Test
	public final void assertThatOutgoingPingChannelHandlerSendsNoPingResponseReceivedWithinTimeoutEventUpstreamIfPingResponseTimeoutExpires()
	        throws InterruptedException {
		final int pingIntervalSeconds = 1;
		final int pingTimeoutMillis = 1000;
		final OutgoingPingChannelHandler<Integer> objectUnderTest = new OutgoingPingChannelHandler<Integer>(
		        pingIntervalSeconds, pingTimeoutMillis,
		        new TestWindowIdGenerator());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		// Simulate successful channel authentication => we start to ping after
		// pingIntervalSeconds
		embeddedPipeline
		        .injectUpstreamChannelEvent(new ChannelSuccessfullyAuthenticatedEvent(
		                embeddedPipeline.getChannel(),
		                new LoginRequest(
		                        "assertThatOutgoingPingChannelHandlerSendsNoPingResponseReceivedWithinTimeoutEventUpstreamIfPingResponseTimeoutExpires",
		                        "password")));
		Thread.sleep(pingIntervalSeconds * 1000 + pingTimeoutMillis + 100);
		final MessageEvent pingRequestCandidate = embeddedPipeline
		        .nextSentMessageEvent();
		assertNotNull(
		        "OutgoingPingChannelHandler did not send any message after channel has been connected and ping interval elapsed",
		        pingRequestCandidate);

		Thread.sleep(pingTimeoutMillis + 100);
		final ChannelEvent noPingResponseReceived = embeddedPipeline
		        .nextUpstreamChannelEvent(ChannelEventFilter.FILTERS
		                .ofType(PingResponseTimeoutExpiredEvent.class));
		assertNotNull(
		        "OutgoingPingChannelHandler did not send expected PingResponseTimeoutExpiredEvent although ping response timeout expired",
		        noPingResponseReceived);
	}

	@Test
	public final void assertThatOutgoingPingChannelHandlerDoesNotSendNoPingResponseReceivedWithinTimeoutEventUpstreamIfItReceivesPingResponseWithinTimeout()
	        throws Throwable {
		final int pingIntervalSeconds = 1;
		final int pingTimeoutMillis = 1000;
		final OutgoingPingChannelHandler<Integer> objectUnderTest = new OutgoingPingChannelHandler<Integer>(
		        pingIntervalSeconds, pingTimeoutMillis,
		        new TestWindowIdGenerator());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		// Simulate successful channel authentication => we start to ping after
		// pingIntervalSeconds
		embeddedPipeline
		        .injectUpstreamChannelEvent(new ChannelSuccessfullyAuthenticatedEvent(
		                embeddedPipeline.getChannel(),
		                new LoginRequest(
		                        "assertThatOutgoingPingChannelHandlerDoesNotSendNoPingResponseReceivedWithinTimeoutEventUpstreamIfItReceivesPingResponseWithinTimeout",
		                        "password")));
		Thread.sleep(pingIntervalSeconds * 1000 + 200);
		final MessageEvent pingRequestCandidate = embeddedPipeline
		        .nextSentMessageEvent();
		assertNotNull(
		        "OutgoingPingChannelHandler did not send any message after channel has been connected and ping interval elapsed",
		        pingRequestCandidate);

		embeddedPipeline.receive(PingResponse
		        .accept((PingRequest) pingRequestCandidate.getMessage()));

		Thread.sleep(pingTimeoutMillis + 100);
		final ChannelEvent noPingResponseReceived = embeddedPipeline
		        .nextUpstreamChannelEvent(ChannelEventFilter.FILTERS
		                .ofType(PingResponseTimeoutExpiredEvent.class));
		assertNull(
		        "OutgoingPingChannelHandler sent unexpected PingResponseTimeoutExpiredEvent although it received a ping response within timeout",
		        noPingResponseReceived);
	}

	private static final class TestWindowIdGenerator implements
	        MessageReferenceGenerator<Integer> {

		private final AtomicInteger	nextId	= new AtomicInteger();

		@Override
		public Integer nextMessageReference() {
			return this.nextId.incrementAndGet();
		}
	}
}
