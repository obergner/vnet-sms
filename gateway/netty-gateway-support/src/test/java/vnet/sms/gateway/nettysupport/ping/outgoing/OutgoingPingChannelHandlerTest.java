package vnet.sms.gateway.nettysupport.ping.outgoing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.util.HashedWheelTimer;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;
import vnet.sms.gateway.nettytest.embedded.ChannelEventFilters;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;

import com.google.common.base.Predicate;

public class OutgoingPingChannelHandlerTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullWindowIdGenerator() {
		new OutgoingPingChannelHandler<Integer>(10, 10, null,
		        new HashedWheelTimer(), new HashedWheelTimer());
	}

	@Test
	public final void assertThatOutgoingPingChannelHandlerSendsPingAfterChannelHasBeenAuthenticatedAndPingIntervalElapsed()
	        throws Throwable {
		final int pingIntervalSeconds = 1;
		final int pingTimeoutMillis = 20000;
		final OutgoingPingChannelHandler<Integer> objectUnderTest = new OutgoingPingChannelHandler<Integer>(
		        pingIntervalSeconds, pingTimeoutMillis,
		        new TestWindowIdGenerator(), new HashedWheelTimer(),
		        new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
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
		        .downstreamMessageEvents().nextMessageEvent();

		assertNotNull(
		        "OutgoingPingChannelHandler did not send any message after channel has been authenticated and ping interval elapsed",
		        pingRequestCandidate);
		assertEquals(
		        "OutgoingPingChannelHandler sent wrong message event after channel has been authenticated and ping interval elapsed",
		        SendPingRequestEvent.class, pingRequestCandidate.getClass());
	}

	@Test
	public final void assertThatOutgoingPingChannelHandlerSendsStarteToPingEventUpstreamAfterChannelHasBeenAuthenticatedAndPingIntervalElapsed()
	        throws Throwable {
		final int pingIntervalSeconds = 1;
		final int pingTimeoutMillis = 20000;
		final OutgoingPingChannelHandler<Integer> objectUnderTest = new OutgoingPingChannelHandler<Integer>(
		        pingIntervalSeconds, pingTimeoutMillis,
		        new TestWindowIdGenerator(), new HashedWheelTimer(),
		        new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
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
		        .upstreamChannelEvents().nextMatchingChannelEvent(
		                new Predicate<ChannelEvent>() {
			                @Override
			                public boolean apply(final ChannelEvent event) {
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
	        throws Throwable {
		final int pingIntervalSeconds = 1;
		final int pingTimeoutMillis = 1000;
		final OutgoingPingChannelHandler<Integer> objectUnderTest = new OutgoingPingChannelHandler<Integer>(
		        pingIntervalSeconds, pingTimeoutMillis,
		        new TestWindowIdGenerator(), new HashedWheelTimer(),
		        new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
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
		        .downstreamMessageEvents().nextMessageEvent();
		assertNotNull(
		        "OutgoingPingChannelHandler did not send any message after channel has been connected and ping interval elapsed",
		        pingRequestCandidate);

		Thread.sleep(pingTimeoutMillis + 100);
		final ChannelEvent noPingResponseReceived = embeddedPipeline
		        .upstreamChannelEvents().nextMatchingChannelEvent(
		                ChannelEventFilters
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
		        new TestWindowIdGenerator(), new HashedWheelTimer(),
		        new HashedWheelTimer());

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);
		embeddedPipeline.connectChannel();
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
		        .downstreamMessageEvents().nextMessageEvent();
		assertNotNull(
		        "OutgoingPingChannelHandler did not send any message after channel has been connected and ping interval elapsed",
		        pingRequestCandidate);

		embeddedPipeline.receive(PingResponse
		        .accept((PingRequest) pingRequestCandidate.getMessage()));

		Thread.sleep(pingTimeoutMillis + 100);
		final ChannelEvent noPingResponseReceived = embeddedPipeline
		        .upstreamChannelEvents().nextMatchingChannelEvent(
		                ChannelEventFilters
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
