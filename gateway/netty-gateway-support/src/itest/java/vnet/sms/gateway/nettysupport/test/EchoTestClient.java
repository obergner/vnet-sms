/**
 * 
 */
package vnet.sms.gateway.nettysupport.test;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class EchoTestClient {

	private final Logger	                 log	             = LoggerFactory
	                                                                     .getLogger(getClass());

	private final DelegatingResponseListener	responseListener	= new DelegatingResponseListener();

	private static class DelegatingResponseListener implements ResponseListener {

		private ResponseListener	delegate;

		void setDelegate(final ResponseListener delegate) {
			this.delegate = delegate;
		}

		@Override
		public void responseReceived(final String telnetResponse) {
			if (this.delegate != null) {
				this.delegate.responseReceived(telnetResponse);
			}
		}
	}

	private final String	host;

	private final int	    port;

	private ClientBootstrap	bootstrap;

	private Channel	        serverConnection;

	public EchoTestClient(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws Exception {
		this.log.info("Starting echo client {} [host = {}|port = {}] ...",
		        new Object[] { this, this.host, this.port });

		setUp();

		connect();

		this.log.info("Echo client {} [host = {}|port = {}] started",
		        new Object[] { this, this.host, this.port });
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
				return Channels.pipeline(new StringEncoder(),
				        new StringDecoder(), new EchoTestClientResponseHandler(
				                EchoTestClient.this.responseListener));
			}
		});
	}

	private void connect() throws RuntimeException {
		// Start the connection attempt.
		final ChannelFuture channelHasBeenConnected = this.bootstrap
		        .connect(new InetSocketAddress(this.host, this.port));
		// Wait until the connection attempt succeeds or fails.
		this.serverConnection = channelHasBeenConnected.awaitUninterruptibly()
		        .getChannel();
		if (!channelHasBeenConnected.isSuccess()) {
			this.log.error("Connection attempt failed: "
			        + channelHasBeenConnected.getCause().getMessage(),
			        channelHasBeenConnected.getCause());
			this.bootstrap.releaseExternalResources();

			throw new RuntimeException(channelHasBeenConnected.getCause());
		}
	}

	public void sendRequest(final String request,
	        final ResponseListener responseListener) throws Exception {
		this.log.debug("Sending request [{}] ...", request);
		this.responseListener.setDelegate(responseListener);

		// Send the request.
		this.serverConnection.write(request);
		this.log.debug("Request [{}] sent", request);

	}

	public void stop() throws Exception {
		this.log.info("Stopping echo client {} [host = {}|port = {}] ...",
		        new Object[] { this, this.host, this.port });

		final ChannelFuture channelHasBeenClosed = this.serverConnection
		        .close();
		channelHasBeenClosed.awaitUninterruptibly();

		// Shut down executor threads to exit.
		this.bootstrap.releaseExternalResources();
		this.log.info("Echo client {} [host = {}|port = {}] stopped",
		        new Object[] { this, this.host, this.port });
	}
}
