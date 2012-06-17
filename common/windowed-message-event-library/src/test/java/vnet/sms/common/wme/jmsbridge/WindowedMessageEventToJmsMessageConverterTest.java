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
import javax.jms.TextMessage;

import org.easymock.Capture;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.junit.Test;

import vnet.sms.common.messages.Headers;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageEventType;
import vnet.sms.common.wme.acknowledge.ReceivedSmsAckedContainer;
import vnet.sms.common.wme.acknowledge.ReceivedSmsNackedContainer;
import vnet.sms.common.wme.receive.LoginRequestReceivedEvent;
import vnet.sms.common.wme.receive.LoginResponseReceivedEvent;
import vnet.sms.common.wme.receive.PingRequestReceivedEvent;
import vnet.sms.common.wme.receive.PingResponseReceivedEvent;
import vnet.sms.common.wme.receive.SmsReceivedEvent;
import vnet.sms.common.wme.send.SendSmsContainer;

import com.mockrunner.mock.jms.MockObjectMessage;
import com.mockrunner.mock.jms.MockTextMessage;

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
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final PingRequest message = new PingRequest();
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, sender);
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
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected message reference",
		        windowedMessageEvent.getMessageReference(),
		        convertedMessage.getObjectProperty(Headers.MESSAGE_REFERENCE));
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getMessageType().toString(),
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
		        windowedMessageEvent.getChannel().getLocalAddress().toString(),
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
		        windowedMessageEvent.getChannel().getRemoteAddress().toString(),
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
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final PingRequest pingRequest = new PingRequest();
		final PingResponse message = PingResponse.accept(pingRequest);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, sender);
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
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected message reference",
		        windowedMessageEvent.getMessageReference(),
		        convertedMessage.getObjectProperty(Headers.MESSAGE_REFERENCE));
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getMessageType().toString(),
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
		        windowedMessageEvent.getChannel().getLocalAddress().toString(),
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
		        windowedMessageEvent.getChannel().getRemoteAddress().toString(),
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
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final LoginRequest message = new LoginRequest(
		        "assertThatToMessageCorrectlyConvertsLoginRequestReceivedEvent",
		        "assertThatToMessageCorrectlyConvertsLoginRequestReceivedEvent");
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, sender);
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
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected message reference",
		        windowedMessageEvent.getMessageReference(),
		        convertedMessage.getObjectProperty(Headers.MESSAGE_REFERENCE));
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getMessageType().toString(),
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
		        windowedMessageEvent.getChannel().getLocalAddress().toString(),
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
		        windowedMessageEvent.getChannel().getRemoteAddress().toString(),
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
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatToMessageCorrectlyConvertsLoginRequestRejectedEvent",
		        "assertThatToMessageCorrectlyConvertsLoginRequestRejectedEvent");
		final LoginResponse message = LoginResponse.reject(loginRequest);
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, sender);
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
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected message reference",
		        windowedMessageEvent.getMessageReference(),
		        convertedMessage.getObjectProperty(Headers.MESSAGE_REFERENCE));
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getMessageType().toString(),
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
		        windowedMessageEvent.getChannel().getLocalAddress().toString(),
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
		        windowedMessageEvent.getChannel().getRemoteAddress().toString(),
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
		expect(receivingChannel.getLocalAddress()).andReturn(receiver)
		        .anyTimes();
		expect(receivingChannel.getId()).andReturn(channelId).anyTimes();

		final Sms message = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatToMessageCorrectlyConvertsSmsReceivedEvent");
		final UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
		        receivingChannel, message, sender);
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
		assertEquals(
		        "toMessage("
		                + windowedMessageEvent
		                + ", "
		                + jmsSession
		                + ") returned a message NOT having the expected message reference",
		        windowedMessageEvent.getMessageReference(),
		        convertedMessage.getObjectProperty(Headers.MESSAGE_REFERENCE));
		assertEquals("toMessage(" + windowedMessageEvent + ", " + jmsSession
		        + ") returned a message NOT having the expected type header",
		        windowedMessageEvent.getMessageType().toString(),
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
		        windowedMessageEvent.getChannel().getLocalAddress().toString(),
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
		        windowedMessageEvent.getChannel().getRemoteAddress().toString(),
		        convertedMessage
		                .getStringProperty(Headers.SENDER_SOCKET_ADDRESS));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsNullJmsMessage()
	        throws JMSException {
		this.objectUnderTest.fromMessage(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsTextMessage()
	        throws JMSException {
		final TextMessage textMessage = new MockTextMessage();

		this.objectUnderTest.fromMessage(textMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsObjectMessageWithoutMessageTypeHeader()
	        throws JMSException {
		final ObjectMessage objectMessage = new MockObjectMessage();

		this.objectUnderTest.fromMessage(objectMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsSmsMessageWithoutPayload()
	        throws JMSException {
		final ObjectMessage smsMessage = new MockObjectMessage();
		smsMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.SEND_SMS.name());

		this.objectUnderTest.fromMessage(smsMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsSmsMessageWithWrongPayload()
	        throws JMSException {
		final ObjectMessage smsMessage = new MockObjectMessage(
		        "assertThatFromMessageRejectsSmsMessageWithWrongPayload");
		smsMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.SEND_SMS.name());

		this.objectUnderTest.fromMessage(smsMessage);
	}

	@Test
	public final void assertThatFromMessageCorrectlyConvertsSendSmsMessage()
	        throws JMSException {
		final Sms sms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageCorrectlyConvertsSendSmsMessage");
		final ObjectMessage smsMessage = new MockObjectMessage(sms);
		smsMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.SEND_SMS.name());

		final Object converted = this.objectUnderTest.fromMessage(smsMessage);

		assertEquals("fromMessage(" + smsMessage
		        + ") did not produce converted message of expected type",
		        SendSmsContainer.class, converted.getClass());
		assertEquals(
		        "fromMessage("
		                + smsMessage
		                + ") did not produce SendSmsContainer with the original SMS as its payload",
		        sms, SendSmsContainer.class.cast(converted).getMessage());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsReceivedSmsAckMessageWithWrongPayload()
	        throws JMSException {
		final ObjectMessage receivedSmsAckedMessage = new MockObjectMessage(
		        "assertThatFromMessageRejectsReceivedSmsAckMessageWithWrongPayload");
		receivedSmsAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_ACKED.name());

		this.objectUnderTest.fromMessage(receivedSmsAckedMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsReceivedSmsAckedMessageWithoutReceivingChannelId()
	        throws JMSException {
		final Sms ackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageRejectsReceivedSmsAckMessageWithoutReceivingChannelId");
		final ObjectMessage receivedSmsAckedMessage = new MockObjectMessage(
		        ackedSms);
		receivedSmsAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_ACKED.name());
		receivedSmsAckedMessage.setObjectProperty(Headers.MESSAGE_REFERENCE,
		        "1");

		this.objectUnderTest.fromMessage(receivedSmsAckedMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsReceivedSmsAckedMessageWithoutMessageReference()
	        throws JMSException {
		final Sms ackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageRejectsReceivedSmsAckedMessageWithoutMessageReference");
		final ObjectMessage receivedSmsAckedMessage = new MockObjectMessage(
		        ackedSms);
		receivedSmsAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_ACKED.name());
		receivedSmsAckedMessage.setIntProperty(Headers.RECEIVING_CHANNEL_ID, 1);

		this.objectUnderTest.fromMessage(receivedSmsAckedMessage);
	}

	@Test
	public final void assertThatFromMessageCorrectlyConvertsReceivedSmsAckedMessage()
	        throws JMSException {
		final int receivingChannelId = 1;
		final String messageReference = "1";

		final Sms ackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageCorrectlyConvertsReceivedSmsAckedMessage");
		final ObjectMessage receivedSmsAckedMessage = new MockObjectMessage(
		        ackedSms);
		receivedSmsAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_ACKED.name());
		receivedSmsAckedMessage.setObjectProperty(Headers.MESSAGE_REFERENCE,
		        messageReference);
		receivedSmsAckedMessage.setIntProperty(Headers.RECEIVING_CHANNEL_ID,
		        receivingChannelId);

		final Object converted = this.objectUnderTest
		        .fromMessage(receivedSmsAckedMessage);

		assertEquals("fromMessage(" + receivedSmsAckedMessage
		        + ") did not produce converted message of expected type",
		        ReceivedSmsAckedContainer.class, converted.getClass());
		final ReceivedSmsAckedContainer<?> casted = ReceivedSmsAckedContainer.class
		        .cast(converted);
		assertEquals(
		        "fromMessage("
		                + receivedSmsAckedMessage
		                + ") did not produce ReceivedMessageAckedContainer with the original SMS as its payload",
		        ackedSms, casted.getAcknowledgedMessage());
		assertEquals(
		        "fromMessage("
		                + receivedSmsAckedMessage
		                + ") did not produce ReceivedMessageAckedContainer with the original GsmPdu Reference",
		        messageReference, casted.getAcknowledgedMessageReference());
		assertEquals(
		        "fromMessage("
		                + receivedSmsAckedMessage
		                + ") did not produce ReceivedMessageAckedContainer with the original receiving channel id",
		        receivingChannelId, casted.getReceivingChannelId());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutReceivingChannelId()
	        throws JMSException {
		final Sms nackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutReceivingChannelId");
		final ObjectMessage receivedSmsNAckedMessage = new MockObjectMessage(
		        nackedSms);
		receivedSmsNAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_NACKED.name());
		receivedSmsNAckedMessage.setObjectProperty(Headers.MESSAGE_REFERENCE,
		        "1");
		receivedSmsNAckedMessage.setIntProperty(Headers.ERROR_KEY, 1);
		receivedSmsNAckedMessage
		        .setObjectProperty(Headers.ERROR_DESCRIPTION,
		                "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutReceivingChannelId");

		this.objectUnderTest.fromMessage(receivedSmsNAckedMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutMessageReference()
	        throws JMSException {
		final Sms nackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutMessageReference");
		final ObjectMessage receivedSmsNAckedMessage = new MockObjectMessage(
		        nackedSms);
		receivedSmsNAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_NACKED.name());
		receivedSmsNAckedMessage
		        .setIntProperty(Headers.RECEIVING_CHANNEL_ID, 1);
		receivedSmsNAckedMessage.setIntProperty(Headers.ERROR_KEY, 1);
		receivedSmsNAckedMessage
		        .setObjectProperty(Headers.ERROR_DESCRIPTION,
		                "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutMessageReference");

		this.objectUnderTest.fromMessage(receivedSmsNAckedMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutErrorCode()
	        throws JMSException {
		final Sms nackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutErrorCode");
		final ObjectMessage receivedSmsNAckedMessage = new MockObjectMessage(
		        nackedSms);
		receivedSmsNAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_NACKED.name());
		receivedSmsNAckedMessage
		        .setIntProperty(Headers.RECEIVING_CHANNEL_ID, 1);
		receivedSmsNAckedMessage
		        .setObjectProperty(Headers.MESSAGE_REFERENCE,
		                "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutErrorCode");
		receivedSmsNAckedMessage
		        .setObjectProperty(Headers.ERROR_DESCRIPTION,
		                "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutErrorCode");

		this.objectUnderTest.fromMessage(receivedSmsNAckedMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutErrorDescription()
	        throws JMSException {
		final Sms nackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutErrorDescription");
		final ObjectMessage receivedSmsNAckedMessage = new MockObjectMessage(
		        nackedSms);
		receivedSmsNAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_NACKED.name());
		receivedSmsNAckedMessage
		        .setIntProperty(Headers.RECEIVING_CHANNEL_ID, 1);
		receivedSmsNAckedMessage
		        .setObjectProperty(Headers.MESSAGE_REFERENCE,
		                "assertThatFromMessageRejectsReceivedSmsNAckedMessageWithoutErrorDescription");
		receivedSmsNAckedMessage.setIntProperty(Headers.ERROR_KEY, 1);

		this.objectUnderTest.fromMessage(receivedSmsNAckedMessage);
	}

	@Test
	public final void assertThatFromMessageCorrectlyConvertsReceivedSmsNAckedMessage()
	        throws JMSException {
		final int receivingChannelId = 1;
		final String messageReference = "1";
		final int errorKey = 55;
		final String errorDescription = "assertThatFromMessageCorrectlyConvertsReceivedSmsNAckedMessage";

		final Sms nackedSms = new Sms(new Msisdn("01701690056"), new Msisdn(
		        "01701690056"),
		        "assertThatFromMessageCorrectlyConvertsReceivedSmsNAckedMessage");
		final ObjectMessage receivedSmsNAckedMessage = new MockObjectMessage(
		        nackedSms);
		receivedSmsNAckedMessage.setStringProperty(Headers.EVENT_TYPE,
		        MessageEventType.RECEIVED_SMS_NACKED.name());
		receivedSmsNAckedMessage.setObjectProperty(Headers.MESSAGE_REFERENCE,
		        messageReference);
		receivedSmsNAckedMessage.setIntProperty(Headers.RECEIVING_CHANNEL_ID,
		        receivingChannelId);
		receivedSmsNAckedMessage.setIntProperty(Headers.ERROR_KEY, errorKey);
		receivedSmsNAckedMessage.setObjectProperty(Headers.ERROR_DESCRIPTION,
		        errorDescription);

		final Object converted = this.objectUnderTest
		        .fromMessage(receivedSmsNAckedMessage);

		assertEquals("fromMessage(" + receivedSmsNAckedMessage
		        + ") did not produce converted message of expected type",
		        ReceivedSmsNackedContainer.class, converted.getClass());
		final ReceivedSmsNackedContainer<?> casted = ReceivedSmsNackedContainer.class
		        .cast(converted);
		assertEquals(
		        "fromMessage("
		                + receivedSmsNAckedMessage
		                + ") did not produce ReceivedMessageNackedContainer with the original SMS as its payload",
		        nackedSms, casted.getAcknowledgedMessage());
		assertEquals(
		        "fromMessage("
		                + receivedSmsNAckedMessage
		                + ") did not produce ReceivedMessageAckedContainer with the original GsmPdu Reference",
		        messageReference, casted.getAcknowledgedMessageReference());
		assertEquals(
		        "fromMessage("
		                + receivedSmsNAckedMessage
		                + ") did not produce ReceivedMessageAckedContainer with the original receiving channel id",
		        receivingChannelId, casted.getReceivingChannelId());
		assertEquals(
		        "fromMessage("
		                + receivedSmsNAckedMessage
		                + ") did not produce ReceivedMessageAckedContainer with the original error key",
		        errorKey, casted.getErrorKey());
		assertEquals(
		        "fromMessage("
		                + receivedSmsNAckedMessage
		                + ") did not produce ReceivedMessageAckedContainer with the original error description",
		        errorDescription, casted.getErrorDescription());
	}
}
