package vnet.sms.gateway.server.framework.internal.channel;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertNotNull;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.notification.NotificationPublisher;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.gateway.nettysupport.monitor.incoming.InitialChannelEventsMonitor;
import vnet.sms.gateway.nettytest.embedded.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.embedded.DefaultChannelPipelineEmbedder;
import vnet.sms.gateway.server.framework.internal.jmsbridge.IncomingMessagesForwardingJmsBridge;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.server.framework.test.NoopJmsTemplate;
import vnet.sms.gateway.server.framework.test.SerialIntegersMessageReferenceGenerator;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

import com.google.common.base.Predicate;
import com.yammer.metrics.Metrics;

public class GatewayServerChannelPipelineFactoryLoginTest {

	@Test
	public final void assertThatGatewayServerChannelPipelineRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest()
	        throws Throwable {
		final int messageReference = 1;
		final LoginRequest successfulLoginRequest = new LoginRequest(
		        "assertThatGatewayServerChannelPipelineRespondsWithASuccessfulLoginResponseToASuccessfulLoginRequest",
		        "whatever");

		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> objectUnderTest = newGatewayServerChannelPipelineFactory();
		final ChannelPipelineEmbedder embedder = new DefaultChannelPipelineEmbedder(
		        objectUnderTest);
		embedder.connectChannel();

		embedder.receive(ReferenceableMessageContainer.wrap(messageReference,
		        successfulLoginRequest).encode());
		final MessageEvent response = embedder.downstreamMessageEvents()
		        .nextMatchingMessageEvent(new Predicate<MessageEvent>() {
			        @Override
			        public boolean apply(final MessageEvent event) {
				        if (!ChannelBuffer.class.isInstance(event.getMessage())) {
					        return false;
				        }
				        final ChannelBuffer cb = ChannelBuffer.class.cast(event
				                .getMessage());
				        final ReferenceableMessageContainer rmc = ReferenceableMessageContainer
				                .decode(cb);
				        return ((rmc != null) && (rmc.getMessage() instanceof LoginResponse))
				                && LoginResponse.class.cast(rmc.getMessage())
				                        .loginSucceeded();
			        }
		        });

		assertNotNull("GatewayServer should have returned LoginResponse",
		        response);
	}

	private GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> newGatewayServerChannelPipelineFactory() {
		final InitialChannelEventsMonitor initialChannelEventsMonitor = new InitialChannelEventsMonitor();
		initialChannelEventsMonitor
		        .setNotificationPublisher(createNiceMock(NotificationPublisher.class));
		return new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        "newObjectUnderTest",
		        ReferenceableMessageContainer.class,
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(
		                new NoopJmsTemplate()), 100, 10000,
		        new AcceptAllAuthenticationManager(), 10000,
		        new SerialIntegersMessageReferenceGenerator(), 100, 20000,
		        new MBeanExporter(), initialChannelEventsMonitor, Metrics
		                .defaultRegistry(), new DefaultChannelGroup());
	}
}
