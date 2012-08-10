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
package vnet.sms.gateway.nettytest.embedded;

import static org.jboss.netty.channel.Channels.close;
import static org.jboss.netty.channel.Channels.disconnect;
import static org.jboss.netty.channel.Channels.fireChannelBound;
import static org.jboss.netty.channel.Channels.fireChannelClosed;
import static org.jboss.netty.channel.Channels.fireChannelConnected;
import static org.jboss.netty.channel.Channels.fireChannelDisconnected;
import static org.jboss.netty.channel.Channels.fireChannelOpen;
import static org.jboss.netty.channel.Channels.fireChannelUnbound;
import static org.jboss.netty.channel.Channels.fireMessageReceived;
import static org.jboss.netty.channel.Channels.unbind;
import static org.jboss.netty.channel.Channels.write;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineException;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class DefaultChannelPipelineEmbedder implements ChannelPipelineEmbedder {

	private final Channel	                       channel;

	private final ChannelPipeline	               pipeline;

	private final ChannelEventRecordingChannelSink	sink	               = new ChannelEventRecordingChannelSink();

	private final DefaultChannelEvents	           downstreamChannelEvents	= new DefaultChannelEvents();

	private final DefaultMessageEvents	           downstreamMessageEvents	= new DefaultMessageEvents();

	private final DefaultChannelEvents	           upstreamChannelEvents	= new DefaultChannelEvents();

	private final DefaultMessageEvents	           upstreamMessageEvents	= new DefaultMessageEvents();

	private final AtomicReference<Throwable>	   thrownException	       = new AtomicReference<Throwable>();

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public DefaultChannelPipelineEmbedder(final ChannelHandler... handlers) {
		this.pipeline = new DefaultChannelPipeline();
		this.channel = new EmbeddedChannel(this.pipeline, this.sink);
		configurePipeline(handlers);
	}

	public DefaultChannelPipelineEmbedder(
	        final ChannelPipelineFactory channelPipelineFactory)
	        throws Exception {
		this.pipeline = channelPipelineFactory.getPipeline();
		this.channel = new EmbeddedChannel(this.pipeline, this.sink);
		configurePipeline(this.pipeline);
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

	private void configurePipeline(final ChannelHandler... handlers) {
		if (handlers == null) {
			throw new NullPointerException("handlers");
		}

		if (handlers.length == 0) {
			throw new IllegalArgumentException(
			        "handlers should contain at least one "
			                + ChannelHandler.class.getSimpleName() + '.');
		}

		try {
			for (int i = 0; i < handlers.length; i++) {
				final ChannelHandler h = handlers[i];
				if (h == null) {
					throw new NullPointerException("handlers[" + i + "]");
				}
				if (h instanceof LifeCycleAwareChannelHandler) {
					LifeCycleAwareChannelHandler.class.cast(h).beforeAdd(
					        new EmbeddedChannelHandlerContext(h));
				}
				this.pipeline.addLast(String.valueOf(i), h);
				if (h instanceof LifeCycleAwareChannelHandler) {
					LifeCycleAwareChannelHandler.class.cast(h).afterAdd(
					        new EmbeddedChannelHandlerContext(h));
				}
			}
			this.pipeline.addLast("EXCEPTIONS-RECORDER",
			        new ExceptionRecordingUpstreamChannelHandler());
			this.pipeline.addLast("SENT-MESSAGES-RECORDER",
			        new UpstreamChannelEventsRecordingChannelHandler());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void configurePipeline(final ChannelPipeline pipe) {
		pipe.addLast("EXCEPTIONS-RECORDER",
		        new ExceptionRecordingUpstreamChannelHandler());
		pipe.addLast("SENT-MESSAGES-RECORDER",
		        new UpstreamChannelEventsRecordingChannelHandler());
	}

	// ------------------------------------------------------------------------
	// Open, bind, connect
	// ------------------------------------------------------------------------

	@Override
	public void openChannel() throws Throwable {
		if (!this.channel.isOpen()) {
			fireChannelOpen(this.channel);
		}
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
	}

	@Override
	public void bindChannel() throws Throwable {
		openChannel();
		if (!this.channel.isBound()) {
			fireChannelBound(this.channel, this.channel.getLocalAddress());
		}
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
	}

	@Override
	public void connectChannel() throws Throwable {
		openChannel();
		bindChannel();
		if (!this.channel.isConnected()) {
			fireChannelConnected(this.channel, this.channel.getRemoteAddress());
		}
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
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
		return !this.upstreamChannelEvents.isEmpty();
	}

	@Override
	public void injectUpstreamChannelEvent(final ChannelEvent e) {
		getChannel().getPipeline().sendUpstream(e);

	}

	@Override
	public ChannelEvents upstreamChannelEvents() {
		return this.upstreamChannelEvents;
	}

	@Override
	public MessageEvents upstreamMessageEvents() {
		return this.upstreamMessageEvents;
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
		return !this.downstreamChannelEvents.isEmpty();
	}

	@Override
	public ChannelEvents downstreamChannelEvents() {
		return this.downstreamChannelEvents;
	}

	@Override
	public MessageEvents downstreamMessageEvents() {
		return this.downstreamMessageEvents;
	}

	// ------------------------------------------------------------------------
	// Disconnect, unbind, close
	// ------------------------------------------------------------------------

	@Override
	public void disconnectChannel() throws Throwable {
		disconnect(this.channel);
		if (this.channel.isConnected()) {
			fireChannelDisconnected(this.channel);
		}
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
	}

	@Override
	public void unbindChannel() throws Throwable {
		disconnectChannel();
		unbind(this.channel);
		if (this.channel.isBound()) {
			fireChannelUnbound(this.channel);
		}
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
	}

	@Override
	public void closeChannel() throws Throwable {
		disconnectChannel();
		unbindChannel();
		close(this.channel);
		if (this.channel.isOpen()) {
			fireChannelClosed(this.channel);
		}
		if (this.thrownException.get() != null) {
			throw this.thrownException.getAndSet(null);
		}
	}

	// ------------------------------------------------------------------------
	// Misc
	// ------------------------------------------------------------------------

	@Override
	public ChannelPipeline getPipeline() {
		return this.pipeline;
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
	// Inner classes
	// ------------------------------------------------------------------------

	private final class EmbeddedChannelHandlerContext implements
	        ChannelHandlerContext {

		private final ChannelHandler	h;

		EmbeddedChannelHandlerContext(final ChannelHandler h) {
			this.h = h;
		}

		@Override
		public void setAttachment(final Object attachment) {
		}

		@Override
		public void sendUpstream(final ChannelEvent e) {
		}

		@Override
		public void sendDownstream(final ChannelEvent e) {
		}

		@Override
		public ChannelPipeline getPipeline() {
			return getChannel().getPipeline();
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public ChannelHandler getHandler() {
			return this.h;
		}

		@Override
		public Channel getChannel() {
			return DefaultChannelPipelineEmbedder.this.channel;
		}

		@Override
		public Object getAttachment() {
			return null;
		}

		@Override
		public boolean canHandleUpstream() {
			return false;
		}

		@Override
		public boolean canHandleDownstream() {
			return false;
		}
	}

	private final class UpstreamChannelEventsRecordingChannelHandler implements
	        ChannelUpstreamHandler {

		UpstreamChannelEventsRecordingChannelHandler() {
		}

		@Override
		public void handleUpstream(final ChannelHandlerContext ctx,
		        final ChannelEvent e) throws Exception {
			if (e instanceof ChannelStateEvent) {
				final ChannelStateEvent event = (ChannelStateEvent) e;
				final EmbeddedChannel channel = (EmbeddedChannel) event
				        .getChannel();
				final ChannelState state = event.getState();
				final Object value = event.getValue();

				switch (state) {
				case OPEN:
					if (Boolean.FALSE.equals(value)) {
						channel.close();
					} else {
						channel.setOpen();
					}
					break;
				case BOUND:
					if (value != null) {
						channel.setBound();
					} else {
						channel.unbind();
					}
					break;
				case CONNECTED:
					if (value != null) {
						channel.setConnected();
					} else {
						channel.disconnect();
					}
					break;
				}
				DefaultChannelPipelineEmbedder.this.upstreamChannelEvents
				        .onEvent(e);
			} else if (e instanceof ExceptionEvent) {
				// Noop
			} else if (e instanceof MessageEvent) {
				DefaultChannelPipelineEmbedder.this.upstreamMessageEvents
				        .onEvent((MessageEvent) e);
			} else {
				DefaultChannelPipelineEmbedder.this.upstreamChannelEvents
				        .onEvent(e);
			}
			ctx.sendUpstream(e);
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
			DefaultChannelPipelineEmbedder.this.upstreamChannelEvents
			        .onExceptionEvent(e);
			DefaultChannelPipelineEmbedder.this.upstreamMessageEvents
			        .onExceptionEvent(e);
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
			if (e instanceof ExceptionEvent) {
				DefaultChannelPipelineEmbedder.this.thrownException
				        .set(((ExceptionEvent) e).getCause());
				DefaultChannelPipelineEmbedder.this.downstreamChannelEvents
				        .onExceptionEvent((ExceptionEvent) e);
				DefaultChannelPipelineEmbedder.this.downstreamMessageEvents
				        .onExceptionEvent((ExceptionEvent) e);
			} else if (e instanceof MessageEvent) {
				DefaultChannelPipelineEmbedder.this.downstreamMessageEvents
				        .onEvent((MessageEvent) e);
			} else {
				DefaultChannelPipelineEmbedder.this.downstreamChannelEvents
				        .onEvent(e);
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
			DefaultChannelPipelineEmbedder.this.downstreamChannelEvents
			        .onExceptionEvent(new DefaultExceptionEvent(e.getChannel(),
			                actualCause));
		}

		@Override
		public ChannelFuture execute(final ChannelPipeline pipeline,
		        final Runnable task) {
			try {
				task.run();
				return Channels.succeededFuture(pipeline.getChannel());
			} catch (final Throwable t) {
				return Channels.failedFuture(pipeline.getChannel(), t);
			}
		}
	}
}
