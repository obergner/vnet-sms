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

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.AbstractChannel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.DefaultChannelConfig;

final class EmbeddedChannel extends AbstractChannel {

	private static final AtomicInteger	UNIQUE_ID	 = new AtomicInteger(0);

	private static final int	       ST_INITIAL	 = Integer.MIN_VALUE;

	private static final int	       ST_OPEN	     = 0;

	private static final int	       ST_BOUND	     = 1;

	private static final int	       ST_CONNECTED	 = 2;

	private static final int	       ST_CLOSED	 = -1;

	private volatile int	           state	     = ST_INITIAL;

	private final ChannelConfig	       config;

	private final SocketAddress	       localAddress	 = new EmbeddedSocketAddress();

	private final SocketAddress	       remoteAddress	= new EmbeddedSocketAddress();

	EmbeddedChannel(final ChannelPipeline pipeline, final ChannelSink sink) {
		super(UNIQUE_ID.getAndIncrement(), null, NullChannelFactory.INSTANCE,
		        pipeline, sink);
		this.config = new DefaultChannelConfig();
	}

	@Override
	public ChannelConfig getConfig() {
		return this.config;
	}

	@Override
	public SocketAddress getLocalAddress() {
		return this.localAddress;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	@Override
	public boolean isOpen() {
		return this.state >= ST_OPEN;
	}

	void setOpen() {
		assert this.state == ST_INITIAL : "Invalid state: " + this.state;
		this.state = ST_OPEN;
	}

	@Override
	public boolean isBound() {
		return this.state >= ST_BOUND;
	}

	void setBound() {
		assert this.state == ST_OPEN : "Invalid state: " + this.state;
		this.state = ST_BOUND;
	}

	@Override
	public boolean isConnected() {
		return this.state == ST_CONNECTED;
	}

	void setConnected() {
		if (this.state != ST_CLOSED) {
			this.state = ST_CONNECTED;
		}
	}

	@Override
	protected boolean setClosed() {
		this.state = ST_CLOSED;
		return super.setClosed();
	}
}
