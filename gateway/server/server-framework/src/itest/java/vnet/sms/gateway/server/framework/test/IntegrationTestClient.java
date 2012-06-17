package vnet.sms.gateway.server.framework.test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class IntegrationTestClient {

	private final Logger	        log	= LoggerFactory.getLogger(getClass());

	private final InetSocketAddress	serverAddress;

	private ClientBootstrap	        bootstrap;

	private Channel	                serverConnection;

	/**
	 * @param serverAddress
	 */
	public IntegrationTestClient(final String host, final int port) {
		this.serverAddress = new InetSocketAddress(host, port);
	}

	public void connect() throws Exception {
		connect(false);
	}

	public void connect(final boolean respondToPing) throws Exception {
		this.log.info("Connecting to {} ...", this.serverAddress);

		this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
		        Executors.newCachedThreadPool(),
		        Executors.newCachedThreadPool()));
		this.bootstrap
		        .setPipelineFactory(new IntegrationTestClientChannelPipelineFactory(
		                respondToPing));

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

			throw new RuntimeException(channelConnected.getCause());
		}

		this.log.info("Connected to {}", this.serverAddress);
	}

	public void sendMessage(final int messageReference, final GsmPdu gsmPdu)
	        throws Throwable {
		sendMessage(messageReference, gsmPdu, null);
	}

	public void sendMessage(final int messageReference, final GsmPdu gsmPdu,
	        final MessageEventListener responseListener) throws Throwable {
		this.log.debug("Sending message {} to {} via channel {}...",
		        new Object[] { gsmPdu, this.serverAddress,
		                this.serverConnection });

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

		this.log.debug("Successfully sent message {} to {} via channel {}",
		        new Object[] { gsmPdu, this.serverAddress,
		                this.serverConnection });
	}

	private void maybeInstallMessageListener(
	        final MessageEventListener messageListener) {
		if (messageListener != null) {
			installMessageListener(messageListener);
		}
	}

	private void installMessageListener(
	        final MessageEventListener messageListener) {
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
		final MessageEventListener responseListener = new MessageEventListener() {
			@Override
			public void messageEventReceived(final MessageEvent e) {
				receivedResponse.set(e);
				responseReceived.countDown();
			}
		};

		sendMessage(messageReference, gsmPdu, responseListener);
		this.log.debug("Waiting for response to message {} sent to {}",
		        gsmPdu, this.serverAddress);
		responseReceived.await();
		this.log.debug("Received response {} to message {}",
		        receivedResponse.get(), gsmPdu);

		return ReferenceableMessageContainer.class.cast(receivedResponse.get()
		        .getMessage());
	}

	public ReferenceableMessageContainer sendMessageAndWaitForMatchingResponse(
	        final int messageReference, final GsmPdu gsmPdu,
	        final MessageEventPredicate messageEventPredicate) throws Throwable {
		final CountDownLatch responseReceived = new CountDownLatch(1);
		final AtomicReference<MessageEvent> receivedResponse = new AtomicReference<MessageEvent>();
		final MessageEventListener responseListener = new MessageEventListener() {
			@Override
			public void messageEventReceived(final MessageEvent e) {
				if (messageEventPredicate.evaluate(e)) {
					receivedResponse.set(e);
					responseReceived.countDown();
				}
			}
		};

		sendMessage(messageReference, gsmPdu, responseListener);
		this.log.debug("Waiting for response to message {} sent to {}",
		        gsmPdu, this.serverAddress);
		responseReceived.await();
		this.log.debug("Received response {} to message {}",
		        receivedResponse.get(), gsmPdu);

		return ReferenceableMessageContainer.class.cast(receivedResponse.get()
		        .getMessage());
	}

	public void listen(final MessageEventListener messageListener) {
		installMessageListener(messageListener);
	}

	public CountDownLatch listen(
	        final MessageEventPredicate messageEventPredicate) {
		final CountDownLatch matchingMessageEventReceived = new CountDownLatch(
		        1);
		final MessageEventListener listenForMatchingEvent = new MessageEventListener() {

			@Override
			public void messageEventReceived(final MessageEvent e) {
				if (messageEventPredicate.evaluate(e)) {
					matchingMessageEventReceived.countDown();
				}
			}
		};
		installMessageListener(listenForMatchingEvent);
		return matchingMessageEventReceived;
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

	public void disconnect() throws Exception {
		this.log.info("Disconnecting from {} ...", this.serverAddress);

		final ChannelFuture channelDisconnected = getMandatoryServerConnection()
		        .disconnect();
		this.bootstrap.releaseExternalResources();
		this.bootstrap = null;
		if (!channelDisconnected.isSuccess()) {
			this.log.error("Failed to disconnect from " + this.serverAddress
			        + ": " + channelDisconnected.getCause().getMessage(),
			        channelDisconnected.getCause());

			throw new RuntimeException(channelDisconnected.getCause());
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

	private final class IntegrationTestClientChannelPipelineFactory implements
	        ChannelPipelineFactory {

		private final boolean		             respondToPing;

		private final PingResponseChannelHandler	pingResponseHandler	= new PingResponseChannelHandler();

		IntegrationTestClientChannelPipelineFactory(final boolean respondToPing) {
			this.respondToPing = respondToPing;
		}

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			final ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("encoder", new ObjectEncoder());
			pipeline.addLast("decoder",
			        new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
			if (this.respondToPing) {
				pipeline.addLast(PingResponseChannelHandler.NAME,
				        this.pingResponseHandler);
			}

			return pipeline;
		}
	}

	private final class PingResponseChannelHandler extends
	        SimpleChannelUpstreamHandler {

		static final String		    NAME		   = "itest:ping-response";

		private final AtomicInteger	nextMessageRef	= new AtomicInteger(
		                                                   10000000);

		@Override
		public void messageReceived(final ChannelHandlerContext ctx,
		        final MessageEvent e) throws Exception {
			try {
				final Object message = e.getMessage();
				if (ReferenceableMessageContainer.class.isInstance(message)
				        && PingRequest.class
				                .isInstance(ReferenceableMessageContainer.class
				                        .cast(message).getMessage())) {
					final PingRequest pingRequest = PingRequest.class
					        .cast(ReferenceableMessageContainer.class.cast(
					                message).getMessage());
					final PingResponse pingResponse = PingResponse
					        .accept(pingRequest);
					sendMessage(this.nextMessageRef.incrementAndGet(),
					        pingResponse);
					IntegrationTestClient.this.log.debug(
					        "Sent {} in response to {}", pingResponse, message);
				} else {
					super.messageReceived(ctx, e);
				}
			} catch (final Throwable e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	private final class ResponseListenerChannelHandler extends
	        SimpleChannelUpstreamHandler {

		static final String		           NAME	= "itest:response-listener";

		private final MessageEventListener	listener;

		ResponseListenerChannelHandler(final MessageEventListener listener) {
			this.listener = listener;
		}

		@Override
		public void messageReceived(final ChannelHandlerContext ctx,
		        final MessageEvent e) throws Exception {
			IntegrationTestClient.this.log.info("Received response {}", e);
			this.listener.messageEventReceived(e);
			super.messageReceived(ctx, e);
		}
	}
}
