package vnet.sms.gateway.server.framework.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class LocalClient {

	public interface MessageListener {

		void messageReceived(MessageEvent e);
	}

	private final Logger	   log	= LoggerFactory.getLogger(getClass());

	private final LocalAddress	serverAddress;

	private ClientBootstrap	   bootstrap;

	private Channel	           serverConnection;

	/**
	 * @param serverAddress
	 */
	public LocalClient(final LocalAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	public void connect() throws Throwable {
		this.log.info("Connecting to {} ...", this.serverAddress);

		this.bootstrap = new ClientBootstrap(
		        new DefaultLocalClientChannelFactory());
		this.bootstrap
		        .setPipelineFactory(new LocalClientChannelPipelineFactory());

		final ChannelFuture channelConnected = this.bootstrap
		        .connect(this.serverAddress);
		this.serverConnection = channelConnected.awaitUninterruptibly()
		        .getChannel();
		if (!channelConnected.isSuccess()) {
			this.log.error("Failed to connect to " + this.serverAddress + ": "
			        + channelConnected.getCause().getMessage(),
			        channelConnected.getCause());
			this.bootstrap.releaseExternalResources();
			this.bootstrap = null;

			throw channelConnected.getCause();
		}

		this.log.info("Connected to {}", this.serverAddress);
	}

	public void sendMessage(final int messageReference, final GsmPdu gsmPdu)
	        throws Throwable {
		sendMessage(messageReference, gsmPdu, null);
	}

	public void sendMessage(final int messageReference, final GsmPdu gsmPdu,
	        final MessageListener responseListener) throws Throwable {
		this.log.debug("Sending message {} to {} ...", gsmPdu,
		        this.serverAddress);

		maybeInstallMessageListener(responseListener);

		final ChannelFuture writeCompleted = getMandatoryServerConnection()
		        .write(ReferenceableMessageContainer.wrap(messageReference,
		                gsmPdu));
		writeCompleted.awaitUninterruptibly();
		if (!writeCompleted.isSuccess()) {
			this.log.error("Failed to send " + gsmPdu + ": "
			        + writeCompleted.getCause().getMessage(),
			        writeCompleted.getCause());

			throw writeCompleted.getCause();
		}

		this.log.debug("Successfully sent message {} to {}", gsmPdu,
		        this.serverAddress);
	}

	private void maybeInstallMessageListener(
	        final MessageListener messageListener) {
		if (messageListener != null) {
			installMessageListener(messageListener);
		}
	}

	private void installMessageListener(final MessageListener messageListener) {
		if (getMandatoryServerConnection().getPipeline().get(
		        ResponseListenerChannelHandler.NAME) != null) {
			getMandatoryServerConnection().getPipeline().remove(
			        ResponseListenerChannelHandler.NAME);
		}
		getMandatoryServerConnection().getPipeline().addLast(
		        ResponseListenerChannelHandler.NAME,
		        new ResponseListenerChannelHandler(messageListener));
	}

	public ReferenceableMessageContainer sendMessageAndWaitForResponse(
	        final int messageReference, final GsmPdu gsmPdu) throws Throwable {
		final CountDownLatch responseReceived = new CountDownLatch(1);
		final AtomicReference<MessageEvent> receivedResponse = new AtomicReference<MessageEvent>();
		final MessageListener responseListener = new MessageListener() {
			@Override
			public void messageReceived(final MessageEvent e) {
				receivedResponse.set(e);
				responseReceived.countDown();
			}
		};

		sendMessage(messageReference, gsmPdu, responseListener);
		responseReceived.await();

		return ReferenceableMessageContainer.class.cast(receivedResponse.get()
		        .getMessage());
	}

	public void listen(final MessageListener messageListener) {
		installMessageListener(messageListener);
	}

	public void login(final int messageReference, final String username,
	        final String password) throws Throwable {
		final LoginRequest loginRequest = new LoginRequest(username, password);
		final ReferenceableMessageContainer loginResponseContainer = sendMessageAndWaitForResponse(
		        messageReference, loginRequest);
		final GsmPdu response = loginResponseContainer.getMessage();
		if (!(response instanceof LoginResponse)) {
			throw new RuntimeException("Unexpected response to " + loginRequest
			        + ": " + response);
		}
		final LoginResponse loginResponse = LoginResponse.class.cast(response);
		if (!loginResponse.loginSucceeded()) {
			throw new BadCredentialsException(
			        "Failed to login using username = " + username
			                + " and password = " + password);
		}
	}

	public void disconnect() throws Throwable {
		this.log.info("Disconnecting from {} ...", this.serverAddress);

		final ChannelFuture channelDisconnected = getMandatoryServerConnection()
		        .disconnect();
		this.bootstrap.releaseExternalResources();
		this.bootstrap = null;
		if (!channelDisconnected.isSuccess()) {
			this.log.error("Failed to disconnect from " + this.serverAddress
			        + ": " + channelDisconnected.getCause().getMessage(),
			        channelDisconnected.getCause());

			throw channelDisconnected.getCause();
		}

		this.log.info("Disconnected from {}", this.serverAddress);
	}

	private Channel getMandatoryServerConnection() {
		if (this.serverConnection == null) {
			throw new IllegalStateException(
			        "No server connection - did you remember to call connect()?");
		}
		return this.serverConnection;
	}

	private final class LocalClientChannelPipelineFactory implements
	        ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			final ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("encoder", new ObjectEncoder());
			pipeline.addLast("decoder",
			        new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

			return pipeline;
		}
	}

	private final class ResponseListenerChannelHandler extends
	        SimpleChannelUpstreamHandler {

		public static final String		NAME	= "test:response-listener";

		private final MessageListener	listener;

		ResponseListenerChannelHandler(final MessageListener listener) {
			this.listener = listener;
		}

		@Override
		public void messageReceived(final ChannelHandlerContext ctx,
		        final MessageEvent e) throws Exception {
			LocalClient.this.log.info("Received response {}", e);
			this.listener.messageReceived(e);
			super.messageReceived(ctx, e);
		}
	}
}
