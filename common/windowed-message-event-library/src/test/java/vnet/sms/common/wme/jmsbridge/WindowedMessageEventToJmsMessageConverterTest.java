package vnet.sms.common.wme.jmsbridge;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.easymock.Capture;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;
import org.springframework.jms.support.converter.MessageConversionException;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.LoginRequestAcceptedEvent;
import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginRequestRejectedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestAcknowledgedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;

public class WindowedMessageEventToJmsMessageConverterTest {

	private final WindowedMessageEventToJmsMessageConverter	objectUnderTest	= new WindowedMessageEventToJmsMessageConverter();

	@Test
	public final void assertThatToMessageCorrectlyConvertsPingRequestReceivedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final PingRequest message = new PingRequest(sender, receiver);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, message.getSender());
		final PingRequestReceivedEvent<Integer> windowedMessageEvent = new PingRequestReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        message, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	private static final class RecordingJmsObjectMessage implements
	        ObjectMessage {

		private final Map<String, Object>	properties	= new HashMap<String, Object>();

		private final Capture<Serializable>	body;

		RecordingJmsObjectMessage(final Capture<Serializable> message) {
			this.body = message;
		}

		@Override
		public void acknowledge() throws JMSException {
		}

		@Override
		public void clearBody() throws JMSException {
			this.body.reset();
		}

		@Override
		public void clearProperties() throws JMSException {
			this.properties.clear();
		}

		@Override
		public boolean getBooleanProperty(final String arg0)
		        throws JMSException {
			return Boolean.class.cast(this.properties.get(arg0));
		}

		@Override
		public byte getByteProperty(final String arg0) throws JMSException {
			return Byte.class.cast(this.properties.get(arg0));
		}

		@Override
		public double getDoubleProperty(final String arg0) throws JMSException {
			return Double.class.cast(this.properties.get(arg0));
		}

		@Override
		public float getFloatProperty(final String arg0) throws JMSException {
			return Float.class.cast(this.properties.get(arg0));
		}

		@Override
		public int getIntProperty(final String arg0) throws JMSException {
			return Integer.class.cast(this.properties.get(arg0));
		}

		@Override
		public String getJMSCorrelationID() throws JMSException {
			return null;
		}

		@Override
		public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
			return null;
		}

		@Override
		public int getJMSDeliveryMode() throws JMSException {
			return 0;
		}

		@Override
		public Destination getJMSDestination() throws JMSException {
			return null;
		}

		@Override
		public long getJMSExpiration() throws JMSException {
			return 0;
		}

		@Override
		public String getJMSMessageID() throws JMSException {
			return null;
		}

		@Override
		public int getJMSPriority() throws JMSException {
			return 0;
		}

		@Override
		public boolean getJMSRedelivered() throws JMSException {
			return false;
		}

		@Override
		public Destination getJMSReplyTo() throws JMSException {
			return null;
		}

		@Override
		public long getJMSTimestamp() throws JMSException {
			return 0;
		}

		@Override
		public String getJMSType() throws JMSException {
			return null;
		}

		@Override
		public long getLongProperty(final String arg0) throws JMSException {
			return Long.class.cast(this.properties.get(arg0));
		}

		@Override
		public Object getObjectProperty(final String arg0) throws JMSException {
			return this.properties.get(arg0);
		}

		@Override
		public Enumeration getPropertyNames() throws JMSException {
			return null;
		}

		@Override
		public short getShortProperty(final String arg0) throws JMSException {
			return Short.class.cast(this.properties.get(arg0));
		}

		@Override
		public String getStringProperty(final String arg0) throws JMSException {
			return String.class.cast(this.properties.get(arg0));
		}

		@Override
		public boolean propertyExists(final String arg0) throws JMSException {
			return this.properties.containsKey(arg0);
		}

		@Override
		public void setBooleanProperty(final String arg0, final boolean arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setByteProperty(final String arg0, final byte arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setDoubleProperty(final String arg0, final double arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setFloatProperty(final String arg0, final float arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setIntProperty(final String arg0, final int arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setJMSCorrelationID(final String arg0) throws JMSException {
		}

		@Override
		public void setJMSCorrelationIDAsBytes(final byte[] arg0)
		        throws JMSException {
		}

		@Override
		public void setJMSDeliveryMode(final int arg0) throws JMSException {
		}

		@Override
		public void setJMSDestination(final Destination arg0)
		        throws JMSException {
		}

		@Override
		public void setJMSExpiration(final long arg0) throws JMSException {
		}

		@Override
		public void setJMSMessageID(final String arg0) throws JMSException {
		}

		@Override
		public void setJMSPriority(final int arg0) throws JMSException {
		}

		@Override
		public void setJMSRedelivered(final boolean arg0) throws JMSException {
		}

		@Override
		public void setJMSReplyTo(final Destination arg0) throws JMSException {
		}

		@Override
		public void setJMSTimestamp(final long arg0) throws JMSException {
		}

		@Override
		public void setJMSType(final String arg0) throws JMSException {
		}

		@Override
		public void setLongProperty(final String arg0, final long arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setObjectProperty(final String arg0, final Object arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setShortProperty(final String arg0, final short arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public void setStringProperty(final String arg0, final String arg1)
		        throws JMSException {
			this.properties.put(arg0, arg1);
		}

		@Override
		public Serializable getObject() throws JMSException {
			return this.body.getValue();
		}

		@Override
		public void setObject(final Serializable arg0) throws JMSException {
			this.body.setValue(arg0);
		}
	}

	@Test
	public final void assertThatToMessageCorrectlyConvertsPingRequestAcknowledgedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final PingRequest message = new PingRequest(sender, receiver);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, message.getSender());
		final PingRequestReceivedEvent<Integer> pingRequestReceived = new PingRequestReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);
		final PingRequestAcknowledgedEvent<Integer> windowedMessageEvent = PingRequestAcknowledgedEvent
		        .acknowledge(pingRequestReceived);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        message, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test
	public final void assertThatToMessageCorrectlyConvertsPingResponseReceivedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final PingRequest pingRequest = new PingRequest(sender, receiver);
		final PingResponse message = PingResponse.accept(pingRequest);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, message.getSender());
		final PingResponseReceivedEvent<Integer> windowedMessageEvent = new PingResponseReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        message, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test
	public final void assertThatToMessageCorrectlyConvertsLoginRequestReceivedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final LoginRequest message = new LoginRequest(
		        "assertThatToMessageCorrectlyConvertsLoginRequestReceivedEvent",
		        "assertThatToMessageCorrectlyConvertsLoginRequestReceivedEvent",
		        sender, receiver);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, message.getSender());
		final LoginRequestReceivedEvent<Integer> windowedMessageEvent = new LoginRequestReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        message, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test
	public final void assertThatToMessageCorrectlyConvertsLoginRequestAcceptedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatToMessageCorrectlyConvertsLoginRequestAcceptedEvent",
		        "assertThatToMessageCorrectlyConvertsLoginRequestAcceptedEvent",
		        sender, receiver);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, loginRequest, loginRequest.getSender());
		final LoginRequestReceivedEvent<Integer> loginRequestReceived = new LoginRequestReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, loginRequest);
		final LoginRequestAcceptedEvent<Integer> windowedMessageEvent = LoginRequestAcceptedEvent
		        .accept(loginRequestReceived);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        loginRequest, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test
	public final void assertThatToMessageCorrectlyConvertsLoginRequestRejectedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatToMessageCorrectlyConvertsLoginRequestRejectedEvent",
		        "assertThatToMessageCorrectlyConvertsLoginRequestRejectedEvent",
		        sender, receiver);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, loginRequest, loginRequest.getSender());
		final LoginRequestReceivedEvent<Integer> loginRequestReceived = new LoginRequestReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, loginRequest);
		final LoginRequestRejectedEvent<Integer> windowedMessageEvent = LoginRequestRejectedEvent
		        .reject(loginRequestReceived);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        loginRequest, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test
	public final void assertThatToMessageCorrectlyConvertsLoginResponseReceivedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatToMessageCorrectlyConvertsLoginRequestRejectedEvent",
		        "assertThatToMessageCorrectlyConvertsLoginRequestRejectedEvent",
		        sender, receiver);
		final LoginResponse message = LoginResponse.reject(loginRequest);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, message.getSender());
		final LoginResponseReceivedEvent<Integer> windowedMessageEvent = new LoginResponseReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        message, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test
	public final void assertThatToMessageCorrectlyConvertsSmsReceivedEvent()
	        throws JMSException {
		final InetSocketAddress sender = new InetSocketAddress(1);
		final InetSocketAddress receiver = new InetSocketAddress(2);
		final Integer channelId = 123;
		final Integer messageReference = 78;

		final Session jmsSession = createNiceMock(Session.class);
		final Capture<Serializable> capturedMessage = new Capture<Serializable>();
		expect(jmsSession.createObjectMessage(capture(capturedMessage)))
		        .andReturn(new RecordingJmsObjectMessage(capturedMessage));

		final Channel receivingChannel = createNiceMock(Channel.class);
		expect(receivingChannel.getRemoteAddress()).andReturn(sender)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final Sms message = new Sms(
		        "assertThatToMessageCorrectlyConvertsSmsReceivedEvent", sender,
		        receiver);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, message.getSender());
		final SmsReceivedEvent<Integer> windowedMessageEvent = new SmsReceivedEvent<Integer>(
		        messageReference, upstreamMessageEvent, message);

		replay(jmsSession, receivingChannel);

		final javax.jms.ObjectMessage convertedMessage = (ObjectMessage) this.objectUnderTest
		        .toMessage(windowedMessageEvent, jmsSession);

		assertNotNull("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned null", convertedMessage);
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected payload",
		        message, convertedMessage.getObject());
		// Verify headers
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getType().toString(),
		        convertedMessage.getStringProperty(Headers.EVENT_TYPE));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receive timestamp",
		        windowedMessageEvent.getMessage().getCreationTimestamp(),
		        convertedMessage.getLongProperty(Headers.RECEIVE_TIMESTAMP));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiver socket address",
		        windowedMessageEvent.getMessage().getReceiver().toString(),
		        convertedMessage
		                .getStringProperty(Headers.RECEIVER_SOCKET_ADDRESS));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected receiving channel id",
		        (int) windowedMessageEvent.getChannel().getId(),
		        convertedMessage.getIntProperty(Headers.RECEIVING_CHANNEL_ID));
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected sender socket address",
		        windowedMessageEvent.getMessage().getSender().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatFromMessageThrowsUnsupportedOperationException()
	        throws MessageConversionException, JMSException {
		this.objectUnderTest.fromMessage(null);
	}
}
