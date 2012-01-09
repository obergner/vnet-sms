/**
 * 
 */
package vnet.sms.gateway.server.framework;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.gateway.server.framework.channel.GatewayServerChannelPipelineFactory;

/**
 * @author obergner
 * 
 */
class GatewayServer<ID extends Serializable, TP> {

	private final Logger	                                  log	       = LoggerFactory
	                                                                               .getLogger(getClass());

	private final String	                                  instanceId;

	private final SocketAddress	                              localAddress;

	private final ServerChannelFactory	                      channelFactory;

	private final GatewayServerChannelPipelineFactory<ID, TP>	channelPipelineFactory;

	private final InstanceState	                              stopped	   = new Stopped();

	private final InstanceState	                              starting	   = new Starting();

	private final InstanceState	                              running	   = new Running();

	private final InstanceState	                              stopping	   = new Stopping();

	private final AtomicReference<InstanceState>	          currentState	= new AtomicReference<InstanceState>(
	                                                                               this.stopped);

	GatewayServer(
	        final String instanceId,
	        final int listenPort,
	        final GatewayServerChannelPipelineFactory<ID, TP> channelPipelineFactory,
	        final Executor bossExecutor, final Executor workerExecutor) {
		this(instanceId, "127.0.0.1", listenPort, channelPipelineFactory,
		        bossExecutor, workerExecutor);
	}

	GatewayServer(
	        final String instanceId,
	        final String host,
	        final int listenPort,
	        final GatewayServerChannelPipelineFactory<ID, TP> channelPipelineFactory,
	        final Executor bossExecutor, final Executor workerExecutor) {
		this(
		        instanceId,
		        new InetSocketAddress(host, listenPort),
		        new NioServerSocketChannelFactory(bossExecutor, workerExecutor),
		        channelPipelineFactory);
	}

	/**
	 * Exposed for testing purposes.
	 * 
	 * @param instanceId
	 * @param localAddress
	 * @param channelFactory
	 * @param channelPipelineFactory
	 */
	GatewayServer(
	        final String instanceId,
	        final SocketAddress localAddress,
	        final ServerChannelFactory channelFactory,
	        final GatewayServerChannelPipelineFactory<ID, TP> channelPipelineFactory) {
		notEmpty(instanceId,
		        "Argument 'instanceId' may be neither null nor empty");
		notNull(channelPipelineFactory,
		        "Argument 'channelPipelineFactory' must not be null");
		notNull(localAddress, "Argument 'localAddress' must not be null");
		notNull(channelFactory, "Argument 'channelFactory' must not be null");
		this.instanceId = instanceId;
		this.localAddress = localAddress;
		this.channelFactory = channelFactory;
		this.channelPipelineFactory = channelPipelineFactory;
	}

	void start() throws Exception {
		this.currentState.get().start();
	}

	void stop() throws Exception {
		this.currentState.get().stop();
	}

	ServerStatus getCurrentStatus() {
		return this.currentState.get().getStatus();
	}

	String getInstanceId() {
		return this.instanceId;
	}

	SocketAddress getLocalAddress() {
		return this.localAddress;
	}

	@Override
	public String toString() {
		return "GatewayServer@" + hashCode() + "[instanceId: "
		        + this.instanceId + "|localAddress: " + this.localAddress + "]";
	}

	public abstract class InstanceState {

		private final ServerStatus	status;

		InstanceState(final ServerStatus name) {
			this.status = name;
		}

		public final ServerStatus getStatus() {
			return this.status;
		}

		abstract void start() throws Exception;

		abstract void stop() throws Exception;

		@Override
		public abstract String toString();
	}

	private final class Stopped extends InstanceState {

		Stopped() {
			super(ServerStatus.STOPPED);
		}

		@Override
		void start() throws Exception {
			GatewayServer.this.log.info("Starting {} ...", GatewayServer.this);
			final long start = System.currentTimeMillis();

			if (!GatewayServer.this.currentState.compareAndSet(
			        GatewayServer.this.stopped, GatewayServer.this.starting)) {
				GatewayServer.this.log
				        .warn("{} is not in state STOPPED - ignoring attempt to start",
				                GatewayServer.this);
				return;
			}

			final ServerBootstrap bootstrap = new ServerBootstrap(
			        GatewayServer.this.channelFactory);
			bootstrap
			        .setPipelineFactory(GatewayServer.this.channelPipelineFactory);
			bootstrap.bind(GatewayServer.this.localAddress);

			if (!GatewayServer.this.currentState.compareAndSet(
			        GatewayServer.this.starting, GatewayServer.this.running)) {
				throw new IllegalStateException(
				        String.format(
				                "Expected %s to be in state STARTING - in fact, it is in state %s",
				                GatewayServer.this,
				                GatewayServer.this.currentState.get()));
			}

			final long end = System.currentTimeMillis();
			GatewayServer.this.log.info(
			        "{} started in [{}] ms - listening on {}", new Object[] {
			                GatewayServer.this, end - start,
			                GatewayServer.this.localAddress });
		}

		@Override
		void stop() throws Exception {
			GatewayServer.this.log
			        .warn("Ignoring request to stop {} - this GatewayServer has not been started yet",
			                GatewayServer.this);
		}

		@Override
		public String toString() {
			return "Stopped@" + hashCode() + "[status: " + getStatus() + "]";
		}
	}

	private final class Starting extends InstanceState {

		Starting() {
			super(ServerStatus.STARTING);
		}

		@Override
		void start() throws Exception {
			GatewayServer.this.log
			        .warn("Ignoring request to start {} - this GatewayServer is already starting",
			                GatewayServer.this);
		}

		@Override
		void stop() throws Exception {
			GatewayServer.this.log
			        .warn("Ignoring request to stop {} - this GatewayServer is currently starting",
			                GatewayServer.this);
		}

		@Override
		public String toString() {
			return "Starting@" + hashCode() + "[status: " + getStatus() + "]";
		}
	}

	private final class Running extends InstanceState {

		Running() {
			super(ServerStatus.RUNNING);
		}

		@Override
		void start() throws Exception {
			GatewayServer.this.log
			        .warn("Ignoring request to start {} - this GatewayServer is already running",
			                GatewayServer.this);
		}

		@Override
		void stop() throws Exception {
			GatewayServer.this.log.info("Stopping {} ...", GatewayServer.this);
			final long start = System.currentTimeMillis();

			if (!GatewayServer.this.currentState.compareAndSet(
			        GatewayServer.this.running, GatewayServer.this.stopping)) {
				GatewayServer.this.log
				        .warn("{} is not in state RUNNING - ignoring attempt to stop",
				                GatewayServer.this);
				return;
			}

			final int numberOfChannels = GatewayServer.this.channelPipelineFactory
			        .getAllConnectedChannels().size();
			GatewayServer.this.log.info("Closing [{}] open channels ...",
			        numberOfChannels);
			GatewayServer.this.channelPipelineFactory.getAllConnectedChannels()
			        .close().awaitUninterruptibly();
			GatewayServer.this.log.info("Closed [{}] open channels",
			        numberOfChannels);

			GatewayServer.this.channelFactory.releaseExternalResources();

			if (!GatewayServer.this.currentState.compareAndSet(
			        GatewayServer.this.stopping, GatewayServer.this.stopped)) {
				throw new IllegalStateException(
				        String.format(
				                "Expected %s to be in state STOPPING - in fact, it is in state %s",
				                GatewayServer.this,
				                GatewayServer.this.currentState.get()));
			}

			final long end = System.currentTimeMillis();
			GatewayServer.this.log.info("{} stopped in [{}] ms",
			        GatewayServer.this, end - start);
		}

		@Override
		public String toString() {
			return "Running@" + hashCode() + "[status: " + getStatus() + "]";
		}
	}

	private final class Stopping extends InstanceState {

		Stopping() {
			super(ServerStatus.STOPPING);
		}

		@Override
		void start() throws Exception {
			GatewayServer.this.log
			        .warn("Ignoring request to start {} - this GatewayServer is currently stopping",
			                GatewayServer.this);
		}

		@Override
		void stop() throws Exception {
			GatewayServer.this.log
			        .warn("Ignoring request to stop {} - this GatewayServer is already stopping",
			                GatewayServer.this);
		}

		@Override
		public String toString() {
			return "Stopping@" + hashCode() + "[status: " + getStatus() + "]";
		}
	}
}
