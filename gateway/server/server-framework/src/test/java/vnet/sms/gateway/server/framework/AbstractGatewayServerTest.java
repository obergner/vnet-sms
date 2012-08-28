package vnet.sms.gateway.server.framework;

import java.lang.management.ManagementFactory;

import javax.management.Notification;

import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.util.HashedWheelTimer;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.UnableToSendNotificationException;
import org.springframework.security.authentication.AuthenticationManager;

import vnet.sms.common.wme.jmsbridge.WindowedMessageEventToJmsMessageConverter;
import vnet.sms.gateway.nettysupport.monitor.incoming.InitialChannelEventsMonitor;
import vnet.sms.gateway.server.framework.internal.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.internal.jmsbridge.IncomingMessagesForwardingJmsBridge;
import vnet.sms.gateway.server.framework.test.SerialIntegersMessageReferenceGenerator;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

import com.mockrunner.jms.ConfigurationManager;
import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.MockConnectionFactory;
import com.yammer.metrics.Metrics;

public class AbstractGatewayServerTest {

	private static final String	DEFAULT_QUEUE_NAME	= "queue.test.defaultDestination";

	protected final JmsTemplate newJmsTemplate() {
		final DestinationManager destinationManager = new DestinationManager();
		destinationManager.createQueue(DEFAULT_QUEUE_NAME);

		final ConfigurationManager configurationManager = new ConfigurationManager();

		final MockConnectionFactory mockConnectionFactory = new MockConnectionFactory(
		        destinationManager, configurationManager);

		final JmsTemplate jmsTemplate = new JmsTemplate(mockConnectionFactory);
		jmsTemplate
		        .setMessageConverter(new WindowedMessageEventToJmsMessageConverter());
		jmsTemplate.setDefaultDestinationName(DEFAULT_QUEUE_NAME);

		return jmsTemplate;
	}

	protected final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> newGatewayServerChannelPipelineFactory(
	        final String instanceId, final int availableIncomingWindows,
	        final long incomingWindowWaitTimeMillis,
	        final long failedLoginResponseMillis,
	        final int pingIntervalSeconds,
	        final long pingResponseTimeoutMillis,
	        final AuthenticationManager authenticationManager,
	        final JmsTemplate jmsTemplate) {
		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());

		final NotificationPublisher notPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
			}
		};
		final InitialChannelEventsMonitor initialChannelEventsMonitor = new InitialChannelEventsMonitor(
		        notPublisher);

		return new GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer>(
		        instanceId,
		        ReferenceableMessageContainer.class,
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new IncomingMessagesForwardingJmsBridge<Integer>(jmsTemplate),
		        availableIncomingWindows, incomingWindowWaitTimeMillis,
		        authenticationManager, failedLoginResponseMillis,
		        new SerialIntegersMessageReferenceGenerator(),
		        pingIntervalSeconds, pingResponseTimeoutMillis, mbeanExporter,
		        initialChannelEventsMonitor, Metrics.defaultRegistry(),
		        new HashedWheelTimer(), new DefaultChannelGroup());
	}
}
