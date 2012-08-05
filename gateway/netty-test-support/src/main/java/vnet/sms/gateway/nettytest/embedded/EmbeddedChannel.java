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

class EmbeddedChannel extends AbstractChannel {

	private static final AtomicInteger	UNIQUE_ID	 = new AtomicInteger(0);

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
	public boolean isBound() {
		return true;
	}

	@Override
	public boolean isConnected() {
		return true;
	}
}
