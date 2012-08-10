package vnet.sms.gateway.nettytest.embedded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.DefaultIdleStateEvent;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.junit.Test;

import com.google.common.base.Predicate;

public class DefaultChannelPipelineEmbedderTest {

	static {
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
	}

	@Test
	public final void assertThatReceivePropagatesReceivedMessageAsMessageEventToReceivedMessageEventsQueue()
	        throws Throwable {
		final Object message = "assertThatReceivePropagatesReceivedMessageAsMessageEventToOutputQueue";
		final ChannelUpstreamHandler passThroughHandler = new SimpleChannelUpstreamHandler();

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        passThroughHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.receive(message);
		final MessageEvent result = objectUnderTest.upstreamMessageEvents()
		        .nextMessageEvent();

		assertNotNull("receive(" + message
		        + ") did not propagate received message event to output queue",
		        result);
		assertEquals("receive(" + message
		        + ") propagated the wrong message to output queue", message,
		        result.getMessage());
	}

	@Test
	public final void assertThatNextReceivedMessageEventConformsToChannelEventFilterPassedIn()
	        throws Throwable {
		final Object firstMessage = "firstReceivedMessage";
		final Object secondMessage = "secondReceivedMessage";
		final ChannelUpstreamHandler passThroughHandler = new SimpleChannelUpstreamHandler();

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        passThroughHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.receive(firstMessage);
		objectUnderTest.receive(secondMessage);
		final Predicate<MessageEvent> matchSecondMessage = MessageEventFilters
		        .payloadEquals(secondMessage);
		final MessageEvent result = objectUnderTest.upstreamMessageEvents()
		        .nextMatchingMessageEvent(matchSecondMessage);

		assertNotNull("nextReceivedMessageEvent(" + matchSecondMessage
		        + ") returned null instead of expected second message", result);
		assertEquals(
		        "nextReceivedMessageEvent("
		                + matchSecondMessage
		                + ") returned message event that does not match the filter passed in",
		        secondMessage, result.getMessage());
	}

	@Test(expected = SecurityException.class)
	public final void assertThatReceiveRethrowsExceptionThrownByChannelHandler()
	        throws Throwable {
		final Object message = "assertThatReceiveRethrowsExceptionThrownByChannelHandler";
		final ChannelUpstreamHandler exceptionThrowingHandler = new SimpleChannelUpstreamHandler() {
			@Override
			public void messageReceived(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				ctx.sendUpstream(new DefaultExceptionEvent(
				        ctx.getChannel(),
				        new SecurityException(
				                "assertThatReceiveRethrowsExceptionThrownByChannelHandler")));
			}
		};

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        exceptionThrowingHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.receive(message);
	}

	@Test
	public final void assertThatReceivePropagatesNonMessageEventsCreatedInternallyToUpstreamChannelEventQueue()
	        throws Throwable {
		final Object message = "assertThatReceivePropagatesNonMessageEventsCreatedInternallyToChannelEventQueue";
		final ChannelUpstreamHandler channelEventGeneratingHandler = new SimpleChannelUpstreamHandler() {
			@Override
			public void messageReceived(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				ctx.sendUpstream(new DefaultIdleStateEvent(ctx.getChannel(),
				        IdleState.ALL_IDLE, System.currentTimeMillis()));
			}
		};

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        channelEventGeneratingHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.receive(message);
		final ChannelEvent result = objectUnderTest.upstreamChannelEvents()
		        .nextMatchingChannelEvent(
		                ChannelEventFilters.ofType(IdleStateEvent.class));

		assertNotNull(
		        "receive("
		                + message
		                + ") did not propagate internally generated channel event to upstream channel event queue",
		        result);
		assertEquals("receive(" + message
		        + ") propagated the wrong message to output queue",
		        DefaultIdleStateEvent.class, result.getClass());
	}

	@Test
	public final void assertThatReceivePropagatesAllNonMessageEventsToUpstreamChannelEventQueue()
	        throws Throwable {
		final Object message = "assertThatReceivePropagatesAllNonMessageEventsToUpstreamChannelEventQueue";
		final ChannelUpstreamHandler channelEventGeneratingHandler = new SimpleChannelUpstreamHandler() {
			@Override
			public void messageReceived(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				ctx.sendUpstream(new DefaultIdleStateEvent(ctx.getChannel(),
				        IdleState.ALL_IDLE, System.currentTimeMillis()));
			}
		};

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        channelEventGeneratingHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.receive(message);
		final int numberOfPropagatedChannelEvents = objectUnderTest
		        .upstreamChannelEvents().allChannelEvents().length;

		assertEquals(
		        "receive("
		                + message
		                + ") should have propagated OPEN, BOUND, CONNECTED and IdleStateEvent to upstream channel event queue",
		        4, numberOfPropagatedChannelEvents);
	}

	@Test
	public final void assertThatSendPropagatesSentMessageAsMessageEventToSentMessageEventsQueue()
	        throws Throwable {
		final Object message = "assertThatSendPropagatesSentMessageAsMessageEventToSentMessageEventsQueue";
		final ChannelDownstreamHandler passThroughHandler = new SimpleChannelDownstreamHandler();

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        passThroughHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.send(message);
		final MessageEvent result = objectUnderTest.downstreamMessageEvents()
		        .nextMessageEvent();

		assertNotNull("send(" + message
		        + ") did not propagate sent message event to output queue",
		        result);
		assertEquals("send(" + message
		        + ") propagated the wrong message to output queue", message,
		        result.getMessage());
	}

	@Test
	public final void assertThatNextSentMessageEventConformsToChannelEventFilterPassedIn()
	        throws Throwable {
		final Object firstMessage = "firstSentMessage";
		final Object secondMessage = "secondSentMessage";
		final ChannelDownstreamHandler passThroughHandler = new SimpleChannelDownstreamHandler();

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        passThroughHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.send(firstMessage);
		objectUnderTest.send(secondMessage);
		final Predicate<MessageEvent> matchSecondMessage = MessageEventFilters
		        .payloadEquals(secondMessage);
		final MessageEvent result = objectUnderTest.downstreamMessageEvents()
		        .nextMatchingMessageEvent(matchSecondMessage);

		assertNotNull("nextSentMessageEvent(" + matchSecondMessage
		        + ") returned null instead of expected second message", result);
		assertEquals(
		        "nextSentMessageEvent("
		                + matchSecondMessage
		                + ") returned message event that does not match the filter passed in",
		        secondMessage, result.getMessage());
	}

	@Test(expected = SecurityException.class)
	public final void assertThatSendRethrowsExceptionThrownByChannelHandler()
	        throws Throwable {
		final Object message = "assertThatSendRethrowsExceptionThrownByChannelHandler";
		final ChannelDownstreamHandler exceptionThrowingHandler = new SimpleChannelDownstreamHandler() {
			@Override
			public void writeRequested(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				ctx.sendDownstream(new DefaultExceptionEvent(
				        ctx.getChannel(),
				        new SecurityException(
				                "assertThatSendRethrowsExceptionThrownByChannelHandler")));
			}
		};

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        exceptionThrowingHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.send(message);
	}

	@Test
	public final void assertThatSendPropagatesNonMessageEventsCreatedInternallyToUpstreamChannelEventQueue()
	        throws Throwable {
		final Object message = "assertThatSendPropagatesNonMessageEventsCreatedInternallyToChannelEventQueue";
		final ChannelDownstreamHandler channelEventGeneratingHandler = new SimpleChannelDownstreamHandler() {
			@Override
			public void writeRequested(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				ctx.sendDownstream(new DefaultIdleStateEvent(ctx.getChannel(),
				        IdleState.ALL_IDLE, System.currentTimeMillis()));
			}
		};

		final DefaultChannelPipelineEmbedder objectUnderTest = new DefaultChannelPipelineEmbedder(
		        channelEventGeneratingHandler);
		objectUnderTest.connectChannel();
		objectUnderTest.send(message);
		final ChannelEvent result = objectUnderTest.downstreamChannelEvents()
		        .nextMatchingChannelEvent(
		                ChannelEventFilters.ofType(IdleStateEvent.class));

		assertNotNull(
		        "send("
		                + message
		                + ") did not propagate internally generated channel event to downstream channel event queue",
		        result);
		assertEquals(
		        "send("
		                + message
		                + ") propagated the wrong message to downstream channel event queue",
		        DefaultIdleStateEvent.class, result.getClass());
	}
}
