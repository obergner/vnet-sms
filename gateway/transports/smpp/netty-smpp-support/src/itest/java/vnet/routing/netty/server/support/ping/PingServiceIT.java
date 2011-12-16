/**
 * 
 */
package vnet.routing.netty.server.support.ping;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import vnet.routing.netty.server.support.test.EchoServerHandler;
import vnet.routing.netty.server.support.test.EchoTestClient;
import vnet.routing.netty.server.support.test.ResponseListener;

/**
 * @author obergner
 * 
 */
public class PingServiceIT {

	private static final int ECHO_SERVER_PORT = 12345;

	private static final ChannelGroup PING_CHANNELS = new DefaultChannelGroup(
			"Ping Channels - " + PingServiceIT.class.getName());

	private static final TestEchoServer ECHO_SERVER = new TestEchoServer(
			PING_CHANNELS);

	@BeforeClass
	public static void startEchoServer() {
		ECHO_SERVER.start();
	}

	@AfterClass
	public static void stopEchoServer() {
		ECHO_SERVER.stop();
	}

	private final EchoTestClient echoClient = new EchoTestClient("localhost",
			ECHO_SERVER_PORT);

	private final PingService<String> objectUnderTest = new PingService<String>(
			PING_CHANNELS, new PingFactory<String>() {
				@Override
				public String newPing() {
					return "PING";
				}
			}, Executors.newScheduledThreadPool(1), 1);

	@Before
	public void startEchoClient() throws Exception {
		this.echoClient.start();
	}

	@After
	public void stopPingService() {
		this.objectUnderTest.stop();
	}

	@After
	public void stopEchoClient() throws Exception {
		this.echoClient.stop();
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.ping.PingService#start()}.
	 * 
	 * @throws Exception
	 */
	@Test(timeout = 20000)
	public final void assertThatStartSendsPingOverChannel() throws Exception {
		final CountDownLatch threePingsReceived = new CountDownLatch(3);
		final ResponseListener pingListener = new ResponseListener() {
			@Override
			public void responseReceived(final String response) {
				if (response.equals("PING")) {
					threePingsReceived.countDown();
				}
			}
		};
		this.echoClient.sendRequest("DUMMY", pingListener);

		this.objectUnderTest.start();

		threePingsReceived.await();
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.ping.PingService#pause()}.
	 * 
	 * @throws Exception
	 */
	@Test(timeout = 20000)
	public final void assertThatPauseStopsPingSender() throws Exception {
		final CountDownLatch twoPingsReceived = new CountDownLatch(2);
		final ResponseListener pingListener = new ResponseListener() {
			@Override
			public void responseReceived(final String response) {
				if (response.equals("PING")) {
					twoPingsReceived.countDown();
				}
			}
		};
		this.echoClient.sendRequest("DUMMY", pingListener);

		this.objectUnderTest.start();

		twoPingsReceived.await();

		this.objectUnderTest.pause();

		final CountDownLatch anyPingReceived = new CountDownLatch(1);
		final ResponseListener pingErrorListener = new ResponseListener() {
			@Override
			public void responseReceived(final String response) {
				if (response.equals("PING")) {
					anyPingReceived.countDown();
				}
			}
		};
		this.echoClient.sendRequest("PAUSE", pingErrorListener);

		assertFalse("pause() did not stop ping",
				anyPingReceived.await(2, TimeUnit.SECONDS));
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.ping.PingService#reschedule(int)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test(timeout = 20000)
	public final void assertThatRescheduleAltersPingInterval() throws Exception {
		final CountDownLatch twoPingsReceived = new CountDownLatch(2);
		final ResponseListener pingListener = new ResponseListener() {
			@Override
			public void responseReceived(final String response) {
				if (response.equals("PING")) {
					twoPingsReceived.countDown();
				}
			}
		};
		this.echoClient.sendRequest("DUMMY", pingListener);

		this.objectUnderTest.start();

		twoPingsReceived.await();

		final int newPingIntervalSeconds = 3;
		this.objectUnderTest.reschedule(newPingIntervalSeconds);

		final CountDownLatch twoPingsAfterRescheduleReceived = new CountDownLatch(
				2);
		final List<Long> pingTimes = new ArrayList<Long>(2);
		final ResponseListener pingRescheduledListener = new ResponseListener() {
			@Override
			public void responseReceived(final String response) {
				if (response.equals("PING")) {
					pingTimes.add(System.currentTimeMillis());
					twoPingsAfterRescheduleReceived.countDown();
				}
			}
		};
		this.echoClient.sendRequest("RESCHEDULED", pingRescheduledListener);
		twoPingsAfterRescheduleReceived.await();

		final long intervalMillis = pingTimes.get(1) - pingTimes.get(0);
		assertTrue("reschedule(" + newPingIntervalSeconds
				+ ") did not alter ping interval",
				intervalMillis > (newPingIntervalSeconds * 1000L - 300L));
	}

	/**
	 * Test method for
	 * {@link vnet.routing.netty.server.support.ping.PingService#stop()}.
	 * 
	 * @throws Exception
	 */
	@Test(timeout = 20000)
	public final void assertThatStopDoesStopPingSender() throws Exception {
		final CountDownLatch twoPingsReceived = new CountDownLatch(2);
		final ResponseListener pingListener = new ResponseListener() {
			@Override
			public void responseReceived(final String response) {
				if (response.equals("PING")) {
					twoPingsReceived.countDown();
				}
			}
		};
		this.echoClient.sendRequest("DUMMY", pingListener);

		this.objectUnderTest.start();

		twoPingsReceived.await();

		this.objectUnderTest.stop();

		final CountDownLatch anyPingReceived = new CountDownLatch(1);
		final ResponseListener pingErrorListener = new ResponseListener() {
			@Override
			public void responseReceived(final String response) {
				if (response.equals("PING")) {
					anyPingReceived.countDown();
				}
			}
		};
		this.echoClient.sendRequest("STOP", pingErrorListener);

		assertFalse("stop() did not stop ping",
				anyPingReceived.await(2, TimeUnit.SECONDS));
	}

	private static class TestEchoServer {

		private final ChannelGroup pingChannels;

		private ServerBootstrap bootstrap;

		TestEchoServer(final ChannelGroup pingChannels) {
			this.pingChannels = pingChannels;
		}

		void start() {
			this.bootstrap = new ServerBootstrap(
					new NioServerSocketChannelFactory(
							Executors.newCachedThreadPool(),
							Executors.newCachedThreadPool()));
			this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					return Channels.pipeline(new StringEncoder(),
							new StringDecoder(), new EchoServerHandler(
									TestEchoServer.this.pingChannels));
				}
			});

			final Channel serverSocketChannel = this.bootstrap
					.bind(new InetSocketAddress(ECHO_SERVER_PORT));
			this.pingChannels.add(serverSocketChannel);
		}

		void stop() {
			final ChannelGroupFuture close = this.pingChannels.close();
			close.awaitUninterruptibly();

			this.bootstrap.releaseExternalResources();
		}
	}
}
