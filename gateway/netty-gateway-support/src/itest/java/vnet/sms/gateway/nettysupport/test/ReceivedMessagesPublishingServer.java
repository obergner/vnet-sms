/**
 * 
 */
package vnet.sms.gateway.nettysupport.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class ReceivedMessagesPublishingServer {

	private final Logger	                              log	                      = LoggerFactory
	                                                                                          .getLogger(getClass());

	private final AtomicInteger	                          port	                      = new AtomicInteger(
	                                                                                          -1);

	private final ServerBootstrap	                      serverBootstrap;

	private final ReceivedMessagesPublishingServerHandler	messagesPublishingHandler	= new ReceivedMessagesPublishingServerHandler();

	private final ChannelGroup	                          allConnectedChannels	      = new DefaultChannelGroup(
	                                                                                          "ReceivedMessagesPublishingServer");

	private final class ChannelRecorder extends SimpleChannelUpstreamHandler {

		/**
		 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
		 *      org.jboss.netty.channel.ChannelStateEvent)
		 */
		@Override
		public void channelConnected(final ChannelHandlerContext ctx,
		        final ChannelStateEvent e) throws Exception {
			ReceivedMessagesPublishingServer.this.log.info(
			        "Channel {} has been connected", e.getChannel());
			ReceivedMessagesPublishingServer.this.allConnectedChannels.add(e
			        .getChannel());
			super.channelConnected(ctx, e);
		}
	}

	/**
	 * @param port
	 */
	public ReceivedMessagesPublishingServer() {
		this.serverBootstrap = new ServerBootstrap(
		        new NioServerSocketChannelFactory(
		                Executors.newCachedThreadPool(),
		                Executors.newCachedThreadPool()));
	}

	public void addListener(final ReceivedMessagesListener listener) {
		this.messagesPublishingHandler.addListener(listener);
	}

	public void clearListeners() {
		this.messagesPublishingHandler.clear();
	}

	public int getPort() {
		final int listenPort = this.port.get();
		if (listenPort == -1) {
			throw new IllegalStateException("Not yet connected");
		}
		return listenPort;
	}

	public void start() throws IOException {
		this.port.set(getNextAvailablePort());
		this.log.info("Starting server on port {} ...", this.port);
		this.serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels
				        .pipeline(
				                new ChannelRecorder(),
				                new ObjectDecoder(ClassResolvers
				                        .cacheDisabled(null)),
				                new ObjectEncoder(),
				                ReceivedMessagesPublishingServer.this.messagesPublishingHandler);
			}
		});
		this.serverBootstrap.bind(new InetSocketAddress(this.port.get()));
		this.log.info("Server started on port {}", this.port);
	}

	private int getNextAvailablePort() throws IOException {
		final ServerSocket socket = new ServerSocket(0);
		final int unusedPort = socket.getLocalPort();
		socket.close();
		this.log.info("Will use as yet unused port {}", unusedPort);

		return unusedPort;
	}

	public void stop() {
		clearListeners();
		this.allConnectedChannels.close().awaitUninterruptibly();
		this.serverBootstrap.releaseExternalResources();
	}
}
