package vnet.sms.gateway.server.framework.jmsbridge;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.InetSocketAddress;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.jmsbridge.WindowedMessageEventToJmsMessageConverter;
import vnet.sms.common.wme.receive.LoginRequestReceivedEvent;
import vnet.sms.common.wme.receive.LoginResponseReceivedEvent;
import vnet.sms.common.wme.receive.PingRequestReceivedEvent;
import vnet.sms.common.wme.receive.PingResponseReceivedEvent;
import vnet.sms.common.wme.receive.SmsReceivedEvent;

import com.mockrunner.jms.ConfigurationManager;
import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.MockQueueConnectionFactory;

public class MessageForwardingJmsBridgeTest {

	private static final String	DEFAULT_QUEUE_NAME	= "queue.test.defaultDestination";

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullJmsTemplate() {
		new MessageForwardingJmsBridge<Integer>(null);
	}

	@Test
	public final void assertThatSmsReceivedForwardsSmsToJmsQueue()
	        throws JMSException {
		final DestinationManager destinationManager = new DestinationManager();
		destinationManager.createQueue(DEFAULT_QUEUE_NAME);
		final ConfigurationManager configurationManager = new ConfigurationManager();
		final ConnectionFactory mockConnFactory = new MockQueueConnectionFactory(
		        destinationManager, configurationManager);

		final JmsTemplate jmsTemplate = new JmsTemplate(mockConnFactory);
		jmsTemplate.setDefaultDestinationName(DEFAULT_QUEUE_NAME);
		jmsTemplate
		        .setMessageConverter(new WindowedMessageEventToJmsMessageConverter());

		final MessageForwardingJmsBridge<Integer> objectUnderTest = new MessageForwardingJmsBridge<Integer>(
		        jmsTemplate);

		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();
		replay(receivingChannel);

		final Sms message = new Sms(
		        "assertThatLoginRequestReceivedForwardsLoginRequestToJmsQueue");
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, receivingChannel.getRemoteAddress());
		final SmsReceivedEvent<Integer> windowedMessageEvent = new SmsReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		objectUnderTest.smsReceived(windowedMessageEvent);

		final ObjectMessage forwardedMessage = ObjectMessage.class
		        .cast(jmsTemplate.receive());

		assertNotNull(
		        "MessageForwardingJmsBridge did not forward SMS to JMS queue",
		        forwardedMessage);
		assertEquals(
		        "MessageForwardingJmsBridge did not forward SMS but rather a different message",
		        message, forwardedMessage.getObject());
	}

	@Test
	public final void assertThatLoginRequestReceivedForwardsLoginRequestToJmsQueue()
	        throws JMSException {
		final DestinationManager destinationManager = new DestinationManager();
		destinationManager.createQueue(DEFAULT_QUEUE_NAME);
		final ConfigurationManager configurationManager = new ConfigurationManager();
		final ConnectionFactory mockConnFactory = new MockQueueConnectionFactory(
		        destinationManager, configurationManager);

		final JmsTemplate jmsTemplate = new JmsTemplate(mockConnFactory);
		jmsTemplate.setDefaultDestinationName(DEFAULT_QUEUE_NAME);
		jmsTemplate
		        .setMessageConverter(new WindowedMessageEventToJmsMessageConverter());

		final MessageForwardingJmsBridge<Integer> objectUnderTest = new MessageForwardingJmsBridge<Integer>(
		        jmsTemplate);

		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();
		replay(receivingChannel);

		final LoginRequest message = new LoginRequest(
		        "assertThatLoginRequestReceivedForwardsLoginRequestToJmsQueue",
		        "assertThatLoginRequestReceivedForwardsLoginRequestToJmsQueue");
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, receivingChannel.getRemoteAddress());
		final LoginRequestReceivedEvent<Integer> windowedMessageEvent = new LoginRequestReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		objectUnderTest.loginRequestReceived(windowedMessageEvent);

		final ObjectMessage forwardedMessage = ObjectMessage.class
		        .cast(jmsTemplate.receive());

		assertNotNull(
		        "MessageForwardingJmsBridge did not forward LoginRequest to JMS queue",
		        forwardedMessage);
		assertEquals(
		        "MessageForwardingJmsBridge did not forward LoginRequest but rather a different message",
		        message, forwardedMessage.getObject());
	}

	@Test
	public final void assertThatLoginResponseReceivedForwardsLoginResponseToJmsQueue()
	        throws JMSException {
		final DestinationManager destinationManager = new DestinationManager();
		destinationManager.createQueue(DEFAULT_QUEUE_NAME);
		final ConfigurationManager configurationManager = new ConfigurationManager();
		final ConnectionFactory mockConnFactory = new MockQueueConnectionFactory(
		        destinationManager, configurationManager);

		final JmsTemplate jmsTemplate = new JmsTemplate(mockConnFactory);
		jmsTemplate.setDefaultDestinationName(DEFAULT_QUEUE_NAME);
		jmsTemplate
		        .setMessageConverter(new WindowedMessageEventToJmsMessageConverter());

		final MessageForwardingJmsBridge<Integer> objectUnderTest = new MessageForwardingJmsBridge<Integer>(
		        jmsTemplate);

		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();
		replay(receivingChannel);

		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatLoginRequestReceivedForwardsLoginRequestToJmsQueue",
		        "assertThatLoginRequestReceivedForwardsLoginRequestToJmsQueue");
		final LoginResponse message = LoginResponse.accept(loginRequest);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, receivingChannel.getRemoteAddress());
		final LoginResponseReceivedEvent<Integer> windowedMessageEvent = new LoginResponseReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		objectUnderTest.loginResponseReceived(windowedMessageEvent);

		final ObjectMessage forwardedMessage = ObjectMessage.class
		        .cast(jmsTemplate.receive());

		assertNotNull(
		        "MessageForwardingJmsBridge did not forward LoginResponse to JMS queue",
		        forwardedMessage);
		assertEquals(
		        "MessageForwardingJmsBridge did not forward LoginResponse but rather a different message",
		        message, forwardedMessage.getObject());
	}

	@Test
	public final void assertThatPingRequestReceivedDoesNotForwardPingRequestToJmsQueue()
	        throws JMSException {
		final DestinationManager destinationManager = new DestinationManager();
		destinationManager.createQueue(DEFAULT_QUEUE_NAME);
		final ConfigurationManager configurationManager = new ConfigurationManager();
		final ConnectionFactory mockConnFactory = new MockQueueConnectionFactory(
		        destinationManager, configurationManager);

		final JmsTemplate jmsTemplate = new JmsTemplate(mockConnFactory);
		jmsTemplate.setDefaultDestinationName(DEFAULT_QUEUE_NAME);
		jmsTemplate
		        .setMessageConverter(new WindowedMessageEventToJmsMessageConverter());

		final MessageForwardingJmsBridge<Integer> objectUnderTest = new MessageForwardingJmsBridge<Integer>(
		        jmsTemplate);

		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();
		replay(receivingChannel);

		final PingRequest message = new PingRequest();
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, receivingChannel.getRemoteAddress());
		final PingRequestReceivedEvent<Integer> windowedMessageEvent = new PingRequestReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		objectUnderTest.pingRequestReceived(windowedMessageEvent);

		final ObjectMessage forwardedMessage = ObjectMessage.class
		        .cast(jmsTemplate.receive());

		assertNull(
		        "MessageForwardingJmsBridge DID forward PingRequest to JMS queue - it shouldn't have done so",
		        forwardedMessage);
	}

	@Test
	public final void assertThatPingResponseReceivedDoesNotForwardPingResponseToJmsQueue()
	        throws JMSException {
		final DestinationManager destinationManager = new DestinationManager();
		destinationManager.createQueue(DEFAULT_QUEUE_NAME);
		final ConfigurationManager configurationManager = new ConfigurationManager();
		final ConnectionFactory mockConnFactory = new MockQueueConnectionFactory(
		        destinationManager, configurationManager);

		final JmsTemplate jmsTemplate = new JmsTemplate(mockConnFactory);
		jmsTemplate.setDefaultDestinationName(DEFAULT_QUEUE_NAME);
		jmsTemplate
		        .setMessageConverter(new WindowedMessageEventToJmsMessageConverter());

		final MessageForwardingJmsBridge<Integer> objectUnderTest = new MessageForwardingJmsBridge<Integer>(
		        jmsTemplate);

		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();
		replay(receivingChannel);

		final PingRequest pingRequest = new PingRequest();
		final PingResponse message = PingResponse.reject(pingRequest);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, receivingChannel.getRemoteAddress());
		final PingResponseReceivedEvent<Integer> windowedMessageEvent = new PingResponseReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		objectUnderTest.pingResponseReceived(windowedMessageEvent);

		final ObjectMessage forwardedMessage = ObjectMessage.class
		        .cast(jmsTemplate.receive());

		assertNull(
		        "MessageForwardingJmsBridge DID forward PingResponse to JMS queue - it shouldn't have done so",
		        forwardedMessage);
	}
}
