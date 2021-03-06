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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;

/**
 */
class NullChannelFactory implements ChannelFactory {

	static final ChannelFactory	INSTANCE	= new NullChannelFactory();

	private NullChannelFactory() {
	}

	@Override
	public Channel newChannel(final ChannelPipeline pipeline) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void releaseExternalResources() {
		// No external resources
	}
}
