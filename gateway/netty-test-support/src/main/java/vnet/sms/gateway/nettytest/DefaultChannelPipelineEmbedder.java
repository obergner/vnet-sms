/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package vnet.sms.gateway.nettytest;

import static org.jboss.netty.channel.Channels.close;
import static org.jboss.netty.channel.Channels.fireChannelBound;
import static org.jboss.netty.channel.Channels.fireChannelClosed;
import static org.jboss.netty.channel.Channels.fireChannelConnected;
import static org.jboss.netty.channel.Channels.fireChannelDisconnected;
import static org.jboss.netty.channel.Channels.fireChannelOpen;
import static org.jboss.netty.channel.Channels.fireChannelUnbound;
import static org.jboss.netty.channel.Channels.fireMessageReceived;
import static org.jboss.netty.channel.Channels.write;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineException;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class DefaultChannelPipelineEmbedder implements ChannelPipelineEmbedder {

	private final Channel	                       channel;

	private final ChannelPipeline	               pipeline;

	private final ChannelEventRecordingChannelSink	sink	               = new ChannelEventRecordingChannelSink();

	private final Queue<MessageEvent>	           receivedMessageEvents	= new LinkedList<MessageEvent>();

	private final Queue<ChannelEvent>	           downstreamChannelEvents	= new LinkedList<ChannelEvent>();

	private final Queue<MessageEvent>	           sentMessageEvents	   = new LinkedList<MessageEvent>();

	private final Queue<ChannelEvent>	           upstreamChannelEvents	= new LinkedList<ChannelEvent>();

	private final AtomicReference<Throwable>	   thrownException	       = new AtomicReference<Throwable>();

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public DefaultChannelPipelineEmbedder(final ChannelHandler... handlers) {
		this.pipeline = new DefaultChannelPipeline();
		configurePipeline(handlers);
		this.channel = new EmbeddedChannel(this.pipeline, this.sink);
		fireInitialEvents();
	}

	public DefaultChannelPipelineEmbedder(
	        final ChannelPipelineFactory channelPipelineFactory)
	        throws Exception {
		this.pipeline = channelPipelineFactory.getPipeline();
		configurePipeline(this.pipeline);
		this.channel = new EmbeddedChannel(this.pipeline, this.sink);
		fireInitialEvents();
	}

	public DefaultChannelPipelineEmbedder(
	        final ChannelBufferFactory bufferFactory,
	        final ChannelHandler... handlers) {
		this(handlers);
		getChannel().getConfig().setBufferFactory(bufferFactory);
	}

	public DefaultChannelPipelineEmbedder(
	        final ChannelBufferFactory bufferFactory,
	        final ChannelPipelineFactory channelPipelineFactory)
	        throws Exception {
		this(channelPipelineFactory);
		getChannel().getConfig().setBufferFactory(bufferFactory);
	}

	// ------------------------------------------------------------------------
	// Internal
	// ------------------------------------------------------------------------

	private void fireInitialEvents() {
		// Fire the typical initial events.
		fireChannelOpen(this.channel);
		fireChannelBound(this.channel, this.channel.getLocalAddress());
		fireChannelConnected(this.channel, this.channel.getRemoteAddress());
	}

	private void configurePipeline(final ChannelHandler... handlers) {
		if (handlers == null) {
			throw new NullPointerException("handlers");
		}

		if (handlers.length == 0) {
			throw new IllegalArgumentException(
			        "handlers should contain at least one "
			                + ChannelHandler.class.getSimpleName() + '.');
		}

		for (int i = 0; i < handlers.length; i++) {
			final ChannelHandler h = handlers[i];
			if (h == null) {
				throw new NullPointerException("handlers[" + i + "]");
			}
			this.pipeline.addLast(String.valueOf(i), handlers[i]);
		}
		this.pipeline.addLast("EXCEPTIONS-RECORDER",
		        new ExceptionRecordingUpstreamChannelHandler());
		this.pipeline.addLast("SENT-MESSAGES-RECORDER",
		        new ReceivedMessagesRecordingUpstreamChannelHandler());
	}

	private void configurePipeline(final ChannelPipeline pipe) {
		pipe.addLast("EXCEPTIONS-RECORDER",
		        new ExceptionRecordingUpstreamChannelHandler());
		pipe.addLast("SENT-MESSAGES-RECORDER",
		        new ReceivedMessagesRecordingUpstreamChannelHandler());
	}

	/**
	 * Returns the virtual {@link Channel} which will be used as a mock during
	 * encoding and decoding.
	 */
	@Override
	public final Channel getChannel() {
		return this.channel;
	}

	// ------------------------------------------------------------------------
	// Receiving messages
	// ------------------------------------------------------------------------

	@Override
	public boolean receive(final Object input) throws Throwable {
		fireMessageReceived(getChannel(), input);
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
		return !this.receivedMessageEvents.isEmpty();
	}

	@Override
	public boolean finishReceive() throws Throwable {
		close(this.channel);
		fireChannelDisconnected(this.channel);
		fireChannelUnbound(this.channel);
		fireChannelClosed(this.channel);
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}

		return !this.receivedMessageEvents.isEmpty();
	}

	@Override
	public void injectUpstreamChannelEvent(final ChannelEvent e) {
		getChannel().getPipeline().sendUpstream(e);

	}

	@Override
	public final MessageEvent nextReceivedMessageEvent() {
		return this.receivedMessageEvents.poll();
	}

	@Override
	public final MessageEvent nextReceivedMessageEvent(
	        final MessageEventFilter predicate) {
		for (final MessageEvent candidate : this.receivedMessageEvents) {
			if (predicate.matches(candidate)) {
				this.receivedMessageEvents.remove(candidate);
				return candidate;
			}
		}
		return null;
	}

	@Override
	public final MessageEvent[] allReceivedMessageEvents() {
		final int size = numberOfReceivedMessageEvents();
		final MessageEvent[] a = new MessageEvent[size];
		for (int i = 0; i < size; i++) {
			final MessageEvent product = nextReceivedMessageEvent();
			if (product == null) {
				throw new ConcurrentModificationException();
			}
			a[i] = product;
		}
		return a;
	}

	@Override
	public final int numberOfReceivedMessageEvents() {
		return this.receivedMessageEvents.size();
	}

	@Override
	public ChannelEvent nextUpstreamChannelEvent() {
		return this.upstreamChannelEvents.poll();
	}

	@Override
	public ChannelEvent nextUpstreamChannelEvent(
	        final ChannelEventFilter predicate) {
		for (final ChannelEvent candidate : this.upstreamChannelEvents) {
			if (predicate.matches(candidate)) {
				this.upstreamChannelEvents.remove(candidate);
				return candidate;
			}
		}
		return null;
	}

	@Override
	public ChannelEvent[] allUpstreamChannelEvents() {
		final int size = numberOfUpstreamChannelEvents();
		final ChannelEvent[] a = new ChannelEvent[size];
		for (int i = 0; i < size; i++) {
			final ChannelEvent product = nextUpstreamChannelEvent();
			if (product == null) {
				throw new ConcurrentModificationException();
			}
			a[i] = product;
		}
		return a;
	}

	@Override
	public int numberOfUpstreamChannelEvents() {
		return this.upstreamChannelEvents.size();
	}

	// ------------------------------------------------------------------------
	// Sending messages
	// ------------------------------------------------------------------------

	@Override
	public boolean send(final Object input) throws Throwable {
		write(getChannel(), input).setSuccess();
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
		return !this.sentMessageEvents.isEmpty();
	}

	@Override
	public MessageEvent nextSentMessageEvent() {
		return this.sentMessageEvents.poll();
	}

	@Override
	public final MessageEvent nextSentMessageEvent(
	        final MessageEventFilter predicate) {
		for (final MessageEvent candidate : this.sentMessageEvents) {
			if (predicate.matches(candidate)) {
				this.sentMessageEvents.remove(candidate);
				return candidate;
			}
		}
		return null;
	}

	@Override
	public MessageEvent[] allSentMessageEvents() {
		final int size = numberOfSentMessageEvents();
		final MessageEvent[] a = new MessageEvent[size];
		for (int i = 0; i < size; i++) {
			final MessageEvent product = nextSentMessageEvent();
			if (product == null) {
				throw new ConcurrentModificationException();
			}
			a[i] = product;
		}
		return a;
	}

	@Override
	public int numberOfSentMessageEvents() {
		return this.sentMessageEvents.size();
	}

	@Override
	public ChannelEvent nextDownstreamChannelEvent() {
		return this.downstreamChannelEvents.poll();
	}

	@Override
	public ChannelEvent nextDownstreamChannelEvent(
	        final ChannelEventFilter predicate) {
		for (final ChannelEvent candidate : this.downstreamChannelEvents) {
			if (predicate.matches(candidate)) {
				this.downstreamChannelEvents.remove(candidate);
				return candidate;
			}
		}
		return null;
	}

	@Override
	public ChannelEvent[] allDownstreamChannelEvents() {
		final int size = numberOfDownstreamChannelEvents();
		final ChannelEvent[] a = new ChannelEvent[size];
		for (int i = 0; i < size; i++) {
			final ChannelEvent product = nextDownstreamChannelEvent();
			if (product == null) {
				throw new ConcurrentModificationException();
			}
			a[i] = product;
		}
		return a;
	}

	@Override
	public int numberOfDownstreamChannelEvents() {
		return this.downstreamChannelEvents.size();
	}

	// ------------------------------------------------------------------------
	// Misc
	// ------------------------------------------------------------------------

	@Override
	public ChannelPipeline getPipeline() {
		return this.pipeline;
	}

	// ------------------------------------------------------------------------
	// Inner classes
	// ------------------------------------------------------------------------

	private final class ReceivedMessagesRecordingUpstreamChannelHandler
	        implements ChannelUpstreamHandler {

		ReceivedMessagesRecordingUpstreamChannelHandler() {
		}

		@Override
		public void handleUpstream(final ChannelHandlerContext ctx,
		        final ChannelEvent e) throws Exception {
			final boolean accepted;
			if (e instanceof MessageEvent) {
				accepted = DefaultChannelPipelineEmbedder.this.receivedMessageEvents
				        .offer((MessageEvent) e);
			} else if (e instanceof ExceptionEvent) {
				accepted = true;
				ctx.sendUpstream(e);
			} else {
				accepted = DefaultChannelPipelineEmbedder.this.upstreamChannelEvents
				        .offer(e);
			}
			assert accepted;
		}
	}

	private final class ExceptionRecordingUpstreamChannelHandler extends
	        SimpleChannelUpstreamHandler {

		ExceptionRecordingUpstreamChannelHandler() {
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx,
		        final ExceptionEvent e) throws Exception {
			DefaultChannelPipelineEmbedder.this.thrownException.set(e
			        .getCause());
		}
	}

	private final class ChannelEventRecordingChannelSink implements ChannelSink {

		ChannelEventRecordingChannelSink() {
		}

		@Override
		public void eventSunk(final ChannelPipeline pipeline,
		        final ChannelEvent e) {
			handleEvent(e);
		}

		private void handleEvent(final ChannelEvent e) {
			if (e instanceof MessageEvent) {
				final boolean offered = DefaultChannelPipelineEmbedder.this.sentMessageEvents
				        .offer((MessageEvent) e);
				assert offered;
			} else if (e instanceof ExceptionEvent) {
				DefaultChannelPipelineEmbedder.this.thrownException
				        .set(((ExceptionEvent) e).getCause());
			} else {
				final boolean offered = DefaultChannelPipelineEmbedder.this.downstreamChannelEvents
				        .offer(e);
				assert offered;
			}
		}

		@Override
		public void exceptionCaught(final ChannelPipeline pipeline,
		        final ChannelEvent e, final ChannelPipelineException cause)
		        throws Exception {
			final Throwable actualCause = cause.getCause() != null ? cause
			        .getCause() : cause;
			DefaultChannelPipelineEmbedder.this.thrownException
			        .set(actualCause);
		}
	}
}
