package vnet.sms.gateway.nettysupport.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;

import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore;

public class WindowingChannelHandlerTest {

	@Test
	public final void assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest() {
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        new IncomingWindowStore<Integer>(
		                "assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest",
		                100, 1000), null);

		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);

		embeddedPipeline
		        .offer(new LoginRequest(
		                "assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest",
		                "secret", new InetSocketAddress(1),
		                new InetSocketAddress(1)));
		final Message propagatedMessage = embeddedPipeline.poll();

		assertNotNull("WindowingChannelHandler did not propagate LoginRequest",
		        propagatedMessage);
		assertEquals(
		        "WindowingChannelHandler converted LoginRequest to unexpected output",
		        LoginRequest.class, propagatedMessage.getClass());
	}

	@Test
	public final void assertThatWindowedChannelHandlerIssuesNoWindowForIncomingMessageAvailableEventIfNoWindowIsAvailable() {
		// FIXME: We need a test framework for Netty ChannelHandlers that
		// propagates events and not their payload (messages) to the product
		// queue
		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest",
		        "secret", new InetSocketAddress(1), new InetSocketAddress(1));

		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        new IncomingWindowStore<Integer>(
		                "assertThatWindowedChannelHandlerIssuesNoWindowForIncomingMessageAvailableEventIfNoWindowIsAvailable",
		                1, 1), null);

		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);

		embeddedPipeline.offer(loginRequest);
		embeddedPipeline.poll();
		embeddedPipeline.offer(loginRequest);

		final Message propagatedMessage = embeddedPipeline.poll();

		assertNotNull("WindowingChannelHandler did not propagate LoginRequest",
		        propagatedMessage);
		assertEquals(
		        "WindowingChannelHandler converted LoginRequest to unexpected output",
		        LoginRequest.class, propagatedMessage.getClass());
	}
}
