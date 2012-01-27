/**
 * 
 */
package vnet.sms.gateway.nettysupport.test;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class TestClient {

	private final Logger	   log	              = LoggerFactory
	                                                      .getLogger(getClass());

	private ClientBootstrap	   bootstrap;

	private final ChannelGroup	serverConnections	= new DefaultChannelGroup(
	                                                      "TestClient");

	public void start() throws Exception {
		this.log.info("Starting echo client {} ...", this);

		setUp();

		this.log.info("Echo client {} started", this);
	}

	private void setUp() {
		// Configure the client.
		this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
		        Executors.newCachedThreadPool(),
		        Executors.newCachedThreadPool()));
		// Set up the event pipeline factory.
		this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new ObjectEncoder(),
				        new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
			}
		});
	}

	public ChannelGroup connect(final int numberOfChannels, final int port)
	        throws RuntimeException {
		this.log.info("Connecting {} channels to port {} ...",
		        numberOfChannels, port);
		for (int i = 0; i < numberOfChannels; i++) {
			// Start the connection attempt.
			final ChannelFuture channelHasBeenConnected = this.bootstrap
			        .connect(new InetSocketAddress(port));
			// Wait until the connection attempt succeeds or fails.
			channelHasBeenConnected.awaitUninterruptibly().getChannel();
			if (!channelHasBeenConnected.isSuccess()) {
				this.log.error("Connection attempt failed: "
				        + channelHasBeenConnected.getCause().getMessage(),
				        channelHasBeenConnected.getCause());
				this.bootstrap.releaseExternalResources();

				throw new RuntimeException(channelHasBeenConnected.getCause());
			}
			this.serverConnections.add(channelHasBeenConnected.getChannel());
			this.log.debug("Connected channel {}",
			        channelHasBeenConnected.getChannel());
		}
		this.log.info("Connected {} channels to port {}", numberOfChannels,
		        port);

		return this.serverConnections;
	}

	public void stop() throws Exception {
		this.log.info("Stopping echo client {} ...", this);

		final ChannelGroupFuture channelsHaveBeenClosed = this.serverConnections
		        .close();
		channelsHaveBeenClosed.awaitUninterruptibly();

		// Shut down executor threads to exit.
		this.bootstrap.releaseExternalResources();
		this.log.info("Echo client {} stopped", this);
	}
}
