package vnet.sms.gateway.nettysupport.publish.outgoing;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.group.ChannelGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import vnet.sms.common.messages.Sms;
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
		final Sms sms = new Sms("assertThatSendSmsDoesSendSmsToConnectedServer");
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
		final Sms sms = new Sms(
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
		final Sms sms = new Sms(
		        "assertThatSendSmsCallsListenerIfNoChannelIsConnected");
		final SendSmsContainer smsContainer = new SendSmsContainer(sms);

		final CountDownLatch listenerCalled = new CountDownLatch(1);
		final OutgoingMessagesSender.Listener listener = new OutgoingMessagesSender.Listener() {
			@Override
			public void sendSmsFailed(final SendSmsContainer failedSms,
			        final Throwable error) {
				listenerCalled.countDown();
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
}
