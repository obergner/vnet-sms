package vnet.sms.gateway.nettysupport.publish.outgoing;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.group.ChannelGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer;
import vnet.sms.common.wme.acknowledge.SendSmsAckContainer;
import vnet.sms.common.wme.acknowledge.SendSmsNackContainer;
import vnet.sms.common.wme.send.SendSmsContainer;
import vnet.sms.gateway.nettysupport.test.ReceivedMessagesListener;
import vnet.sms.gateway.nettysupport.test.ReceivedMessagesPublishingServer;
import vnet.sms.gateway.nettysupport.test.TestClient;

public class DefaultOutgoingMessagesSenderIT {

	private final ReceivedMessagesPublishingServer	testServer	= new ReceivedMessagesPublishingServer();

	private final TestClient	                   testClient	= new TestClient();

	@Before
	public void startTestServer() throws Exception {
		this.testServer.start();
	}

	@Before
	public void startTestClient() throws Exception {
		this.testClient.start();
	}

	@After
	public void stopTestClient() throws Exception {
		this.testClient.stop();
	}

	@After
	public void stopTestServer() throws Exception {
		this.testServer.stop();
	}

	@Test(timeout = 2000)
	public final void assertThatSendSmsDoesSendSmsToConnectedServer()
	        throws Exception {
		final Sms sms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"), "assertThatSendSmsDoesSendSmsToConnectedServer");
		final SendSmsContainer smsContainer = new SendSmsContainer(sms);

		final AtomicReference<Object> receivedMessage = new AtomicReference<Object>();
		final CountDownLatch messageReceived = new CountDownLatch(1);
		final ReceivedMessagesListener listener = new ReceivedMessagesListener() {

			@Override
			public void messageReceived(final Object message) {
				receivedMessage.set(message);
				messageReceived.countDown();
			}
		};
		this.testServer.addListener(listener);

		final ChannelGroup connectedChannels = this.testClient.connect(10,
		        this.testServer.getPort());

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.sendSms(smsContainer);
		messageReceived.await();

		assertEquals("sendSms(" + smsContainer
		        + ") did not send expected sms container", smsContainer,
		        receivedMessage.get());
	}

	@Test(timeout = 2000, expected = IllegalStateException.class)
	public final void assertThatSendSmsThrowsIllegalStateExceptionIfNoChannelIsConnected()
	        throws Exception {
		final Sms sms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatSendSmsThrowsIllegalStateExceptionIfNoChannelIsConnected");
		final SendSmsContainer smsContainer = new SendSmsContainer(sms);

		final ChannelGroup connectedChannels = this.testClient.connect(0,
		        this.testServer.getPort());

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.sendSms(smsContainer);
	}

	@Test(timeout = 2000, expected = IllegalStateException.class)
	public final void assertThatSendSmsCallsListenerIfNoChannelIsConnected()
	        throws Exception {
		final Sms sms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatSendSmsCallsListenerIfNoChannelIsConnected");
		final SendSmsContainer smsContainer = new SendSmsContainer(sms);

		final CountDownLatch listenerCalled = new CountDownLatch(1);
		final OutgoingMessagesSender.Listener<String> listener = new OutgoingMessagesSender.Listener<String>() {
			@Override
			public void sendSmsFailed(final SendSmsContainer failedSms,
			        final Throwable error) {
				listenerCalled.countDown();
			}

			@Override
			public void acknowldgeReceivedSmsFailed(
			        final MessageAcknowledgementContainer<String, ? extends GsmPdu> acknowledgement,
			        final Throwable error) {
				// Noop
			}
		};

		final ChannelGroup connectedChannels = this.testClient.connect(0,
		        this.testServer.getPort());

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.addListener(listener);
		objectUnderTest.sendSms(smsContainer);

		listenerCalled.await();
	}

	@Test(timeout = 2000)
	public final void assertThatAckReceivedSmsDoesSendAckToConnectedServer()
	        throws Exception {
		final Sms sms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatAckReceivedSmsDoesSendAckToConnectedServer");

		final AtomicReference<Object> receivedMessage = new AtomicReference<Object>();
		final CountDownLatch messageReceived = new CountDownLatch(1);
		final ReceivedMessagesListener listener = new ReceivedMessagesListener() {

			@Override
			public void messageReceived(final Object message) {
				receivedMessage.set(message);
				messageReceived.countDown();
			}
		};
		this.testServer.addListener(listener);

		final ChannelGroup connectedChannels = this.testClient.connect(10,
		        this.testServer.getPort());

		final SendSmsAckContainer<String> ackContainer = new SendSmsAckContainer<String>(
		        "1", connectedChannels.iterator().next().getId(), sms);

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.ackReceivedSms(ackContainer);
		messageReceived.await();

		assertEquals("ackReceivedSms(" + ackContainer
		        + ") did not send expected ack container", ackContainer,
		        receivedMessage.get());
	}

	@Test(timeout = 2000, expected = IllegalStateException.class)
	public final void assertThatAckReceivedmsThrowsIllegalStateExceptionIfReceivingChannelIsNotConnectedAnymore()
	        throws Exception {
		final Sms sms = new Sms(
		        new Msisdn("01686754432"),
		        new Msisdn("01686754432"),
		        "assertThatAckReceivedmsThrowsIllegalStateExceptionIfReceivingChannelIsNotConnectedAnymore");
		final SendSmsAckContainer<String> ackContainer = new SendSmsAckContainer<String>(
		        "1", -1, sms);

		final ChannelGroup connectedChannels = this.testClient.connect(0,
		        this.testServer.getPort());

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.ackReceivedSms(ackContainer);
	}

	@Test(timeout = 2000, expected = IllegalStateException.class)
	public final void assertThatAckReceivedSmsCallsListenerIfReceivingChannelIsNotConnectedAnymore()
	        throws Exception {
		final Sms sms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatAckReceivedSmsCallsListenerIfReceivingChannelIsNotConnectedAnymore");
		final SendSmsAckContainer<String> ackContainer = new SendSmsAckContainer<String>(
		        "1", -1, sms);

		final CountDownLatch listenerCalled = new CountDownLatch(1);
		final OutgoingMessagesSender.Listener<String> listener = new OutgoingMessagesSender.Listener<String>() {
			@Override
			public void sendSmsFailed(final SendSmsContainer failedSms,
			        final Throwable error) {
				// Noop
			}

			@Override
			public void acknowldgeReceivedSmsFailed(
			        final MessageAcknowledgementContainer<String, ? extends GsmPdu> acknowledgement,
			        final Throwable error) {
				listenerCalled.countDown();
			}
		};

		final ChannelGroup connectedChannels = this.testClient.connect(0,
		        this.testServer.getPort());

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.addListener(listener);
		objectUnderTest.ackReceivedSms(ackContainer);

		listenerCalled.await();
	}

	@Test(timeout = 2000)
	public final void assertThatNackReceivedSmsDoesSendNackToConnectedServer()
	        throws Exception {
		final Sms sms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatNackReceivedSmsDoesSendNackToConnectedServer");

		final AtomicReference<Object> receivedMessage = new AtomicReference<Object>();
		final CountDownLatch messageReceived = new CountDownLatch(1);
		final ReceivedMessagesListener listener = new ReceivedMessagesListener() {

			@Override
			public void messageReceived(final Object message) {
				receivedMessage.set(message);
				messageReceived.countDown();
			}
		};
		this.testServer.addListener(listener);

		final ChannelGroup connectedChannels = this.testClient.connect(10,
		        this.testServer.getPort());

		final SendSmsNackContainer<String> ackContainer = new SendSmsNackContainer<String>(
		        1, "error", "1", connectedChannels.iterator().next().getId(),
		        sms);

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.nackReceivedSms(ackContainer);
		messageReceived.await();

		assertEquals("ackReceivedSms(" + ackContainer
		        + ") did not send expected ack container", ackContainer,
		        receivedMessage.get());
	}

	@Test(timeout = 2000, expected = IllegalStateException.class)
	public final void assertThatNackReceivedmsThrowsIllegalStateExceptionIfReceivingChannelIsNotConnectedAnymore()
	        throws Exception {
		final Sms sms = new Sms(
		        new Msisdn("01686754432"),
		        new Msisdn("01686754432"),
		        "assertThatNackReceivedmsThrowsIllegalStateExceptionIfReceivingChannelIsNotConnectedAnymore");
		final SendSmsNackContainer<String> ackContainer = new SendSmsNackContainer<String>(
		        1, "error", "1", -1, sms);

		final ChannelGroup connectedChannels = this.testClient.connect(0,
		        this.testServer.getPort());

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.nackReceivedSms(ackContainer);
	}

	@Test(timeout = 2000, expected = IllegalStateException.class)
	public final void assertThatNackReceivedSmsCallsListenerIfReceivingChannelIsNotConnectedAnymore()
	        throws Exception {
		final Sms sms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatNackReceivedSmsCallsListenerIfReceivingChannelIsNotConnectedAnymore");
		final SendSmsNackContainer<String> ackContainer = new SendSmsNackContainer<String>(
		        1, "error", "1", -1, sms);

		final CountDownLatch listenerCalled = new CountDownLatch(1);
		final OutgoingMessagesSender.Listener<String> listener = new OutgoingMessagesSender.Listener<String>() {
			@Override
			public void sendSmsFailed(final SendSmsContainer failedSms,
			        final Throwable error) {
				// Noop
			}

			@Override
			public void acknowldgeReceivedSmsFailed(
			        final MessageAcknowledgementContainer<String, ? extends GsmPdu> acknowledgement,
			        final Throwable error) {
				listenerCalled.countDown();
			}
		};

		final ChannelGroup connectedChannels = this.testClient.connect(0,
		        this.testServer.getPort());

		final DefaultOutgoingMessagesSender<String> objectUnderTest = new DefaultOutgoingMessagesSender<String>(
		        connectedChannels);
		objectUnderTest.addListener(listener);
		objectUnderTest.nackReceivedSms(ackContainer);

		listenerCalled.await();
	}
}
