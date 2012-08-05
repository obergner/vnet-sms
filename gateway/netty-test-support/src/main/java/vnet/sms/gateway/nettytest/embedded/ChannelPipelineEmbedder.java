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
import org.jboss.netty.channel.MessageEvent;

public interface ChannelPipelineEmbedder {

	boolean receive(Object input) throws Throwable;

	void injectUpstreamChannelEvent(ChannelEvent e);

	boolean finishReceive() throws Throwable;

	MessageEvent nextReceivedMessageEvent();

	MessageEvent nextReceivedMessageEvent(MessageEventFilter predicate);

	MessageEvent[] allReceivedMessageEvents();

	ChannelEvent nextUpstreamChannelEvent();

	ChannelEvent nextUpstreamChannelEvent(ChannelEventFilter predicate);

	ChannelEvent[] allUpstreamChannelEvents();

	int numberOfUpstreamChannelEvents();

	int numberOfReceivedMessageEvents();

	boolean send(Object input) throws Throwable;

	MessageEvent nextSentMessageEvent();

	MessageEvent nextSentMessageEvent(MessageEventFilter predicate);

	MessageEvent[] allSentMessageEvents();

	int numberOfSentMessageEvents();

	ChannelEvent nextDownstreamChannelEvent();

	ChannelEvent nextDownstreamChannelEvent(ChannelEventFilter predicate);

	ChannelEvent[] allDownstreamChannelEvents();

	int numberOfDownstreamChannelEvents();

	ChannelPipeline getPipeline();

	Channel getChannel();
}
