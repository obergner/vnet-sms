package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.Notification;

import org.jboss.netty.channel.Channel;
import org.junit.Test;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.UnableToSendNotificationException;

public class InitialChannelEventsMonitorTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatOneArgumentConstructorRejectsNullNotificationPublisher() {
		new InitialChannelEventsMonitor(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetNotificationPublisherRejectsNullNotificationPublisher() {
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor();
		objectUnderTest.setNotificationPublisher(null);
	}

	@Test
	public final void assertThatChannelOpenedPublishesNotificationOfTypeOPENED() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);

		objectUnderTest.channelOpened(createNiceMock(Channel.class));
		final Notification result = publishedNotification.get();

		assertNotNull(
		        "channelOpened(channel) should have published a Notification, yet it didn't",
		        result);
		assertEquals(
		        "channelOpened(channel) should have published a Notification of type OPENED",
		        InitialChannelEventsMonitor.Events.OPENED, result.getType());
	}

	@Test
	public final void assertThatChannelOpenedPublishesNotificationWithTheChannelPassedInAsSource() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);
		final Channel channel = createNiceMock(Channel.class);

		objectUnderTest.channelOpened(channel);
		final Notification result = publishedNotification.get();

		assertEquals(
		        "channelOpened(channel) should have published a Notification having the Channel passed in as its source",
		        channel, result.getSource());
	}

	@Test
	public final void assertThatChannelBoundPublishesNotificationOfTypeBOUND() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);

		objectUnderTest.channelBound(createNiceMock(Channel.class),
		        new InetSocketAddress(1));
		final Notification result = publishedNotification.get();

		assertNotNull(
		        "channelBound(channel, localAddress) should have published a Notification, yet it didn't",
		        result);
		assertEquals(
		        "channelBound(channel, localAddress) should have published a Notification of type BOUND",
		        InitialChannelEventsMonitor.Events.BOUND, result.getType());
	}

	@Test
	public final void assertThatChannelBoundPublishesNotificationWithTheChannelPassedInAsSource() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);
		final Channel channel = createNiceMock(Channel.class);

		objectUnderTest.channelBound(channel, new InetSocketAddress(2));
		final Notification result = publishedNotification.get();

		assertEquals(
		        "channelBound(channel, localAddress) should have published a Notification having the Channel passed in as its source",
		        channel, result.getSource());
	}

	@Test
	public final void assertThatChannelBoundPublishesNotificationWithTheSocketAddressPassedInAsUserData() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);
		final Channel channel = createNiceMock(Channel.class);
		final InetSocketAddress localAddress = new InetSocketAddress(3);

		objectUnderTest.channelBound(channel, localAddress);
		final Notification result = publishedNotification.get();

		assertEquals(
		        "channelBound(channel, localAddress) should have published a Notification having the SocketAddress passed in as UserData",
		        localAddress, result.getUserData());
	}

	@Test
	public final void assertThatChannelConnectedPublishesNotificationOfTypeCONNECTED() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);

		objectUnderTest.channelConnected(createNiceMock(Channel.class),
		        new InetSocketAddress(1));
		final Notification result = publishedNotification.get();

		assertNotNull(
		        "channelConnected(channel, remoteAddress) should have published a Notification, yet it didn't",
		        result);
		assertEquals(
		        "channelConnected(channel, remoteAddress) should have published a Notification of type CONNECTED",
		        InitialChannelEventsMonitor.Events.CONNECTED, result.getType());
	}

	@Test
	public final void assertThatChannelConnectedPublishesNotificationWithTheChannelPassedInAsSource() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);
		final Channel channel = createNiceMock(Channel.class);

		objectUnderTest.channelConnected(channel, new InetSocketAddress(2));
		final Notification result = publishedNotification.get();

		assertEquals(
		        "channelConnected(channel, remoteAddress) should have published a Notification having the Channel passed in as its source",
		        channel, result.getSource());
	}

	@Test
	public final void assertThatChannelConnectedPublishesNotificationWithTheSocketAddressPassedInAsUserData() {
		final AtomicReference<Notification> publishedNotification = new AtomicReference<Notification>();
		final NotificationPublisher notificationPublisher = new NotificationPublisher() {
			@Override
			public void sendNotification(final Notification notification)
			        throws UnableToSendNotificationException {
				publishedNotification.set(notification);
			}
		};
		final InitialChannelEventsMonitor objectUnderTest = new InitialChannelEventsMonitor(
		        notificationPublisher);
		final Channel channel = createNiceMock(Channel.class);
		final InetSocketAddress remoteAddress = new InetSocketAddress(3);

		objectUnderTest.channelConnected(channel, remoteAddress);
		final Notification result = publishedNotification.get();

		assertEquals(
		        "channelConnected(channel, remoteAddress) should have published a Notification having the SocketAddress passed in as UserData",
		        remoteAddress, result.getUserData());
	}
}
