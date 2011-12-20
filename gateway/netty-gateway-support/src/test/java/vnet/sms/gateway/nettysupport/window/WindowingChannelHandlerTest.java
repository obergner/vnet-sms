package vnet.sms.gateway.nettysupport.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore;
import vnet.sms.gateway.nettytest.ChannelEventFilter;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;

public class WindowingChannelHandlerTest {

	@Test
	public final void assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest()
	        throws Throwable {
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        new IncomingWindowStore<Integer>(100, 1000), null);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);

		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest",
		                "secret", new InetSocketAddress(1),
		                new InetSocketAddress(1)));
		final MessageEvent propagatedMessageEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull("WindowingChannelHandler did not propagate LoginRequest",
		        propagatedMessageEvent);
		assertEquals(
		        "WindowingChannelHandler converted LoginRequest to unexpected output",
		        LoginRequest.class, propagatedMessageEvent.getMessage()
		                .getClass());
	}

	@Test
	public final void assertThatWindowedChannelHandlerIssuesNoWindowForIncomingMessageAvailableEventIfNoWindowIsAvailable()
	        throws Throwable {
		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatWindowedChannelHandlerIssuesNoWindowForIncomingMessageEventIfNoWindowIsAvailable",
		        "secret", new InetSocketAddress(1), new InetSocketAddress(1));

		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        new IncomingWindowStore<Integer>(1, 1), null);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);

		embeddedPipeline.receive(loginRequest);
		embeddedPipeline.nextReceivedMessageEvent();
		embeddedPipeline.receive(loginRequest);

		final ChannelEvent propagatedMessageEvent = embeddedPipeline
		        .nextUpstreamChannelEvent(ChannelEventFilter.FILTERS
		                .ofType(NoWindowForIncomingMessageAvailableEvent.class));

		assertNotNull(
		        "WindowingChannelHandler did not propagate error event when rejecting incoming message due to no window available",
		        propagatedMessageEvent);
		assertEquals(
		        "WindowingChannelHandler propagated unexpected event when rejecting incoming message due to no window available",
		        NoWindowForIncomingMessageAvailableEvent.class,
		        propagatedMessageEvent.getClass());
	}
}
