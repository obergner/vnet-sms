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
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelPipeline;

public interface ChannelPipelineEmbedder {

	// ------------------------------------------------------------------------
	// Open, bind, connect
	// ------------------------------------------------------------------------

	void openChannel() throws Throwable;

	void bindChannel() throws Throwable;

	void connectChannel() throws Throwable;

	// ------------------------------------------------------------------------
	// Receiving messages
	// ------------------------------------------------------------------------

	boolean receive(Object input) throws Throwable;

	void injectUpstreamChannelEvent(ChannelEvent e) throws Throwable;

	ChannelEvents upstreamChannelEvents();

	MessageEvents upstreamMessageEvents();

	// ------------------------------------------------------------------------
	// Sending messages
	// ------------------------------------------------------------------------

	boolean send(Object input) throws Throwable;

	ChannelEvents downstreamChannelEvents();

	MessageEvents downstreamMessageEvents();

	// ------------------------------------------------------------------------
	// Disconnect, unbind, close
	// ------------------------------------------------------------------------

	void disconnectChannel() throws Throwable;

	void unbindChannel() throws Throwable;

	void closeChannel() throws Throwable;

	// ------------------------------------------------------------------------
	// Misc
	// ------------------------------------------------------------------------

	ChannelPipeline getPipeline();

	Channel getChannel();
}
