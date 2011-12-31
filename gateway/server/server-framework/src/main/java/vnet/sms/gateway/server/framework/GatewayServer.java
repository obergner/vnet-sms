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

import vnet.sms.gateway.nettysupport.monitor.ChannelMonitorRegistry;
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

	private final State	                                      stopped	   = new Stopped();

	private final State	                                      starting	   = new Starting();

	private final State	                                      running	   = new Running();

	private final State	                                      stopping	   = new Stopping();

	private final AtomicReference<State>	                  currentState	= new AtomicReference<State>(
	                                                                               this.stopped);

	GatewayServer(
	        final String instanceId,
	        final int listenPort,
	        final GatewayServerChannelPipelineFactory<ID, TP> channelPipelineFactory,
	        final Executor bossExecutor, final Executor workerExecutor) {
		this(
		        instanceId,
		        new InetSocketAddress(listenPort),
		        new NioServerSocketChannelFactory(bossExecutor, workerExecutor),
		        channelPipelineFactory);
		notNull(bossExecutor,
		        "Argument 'bossExecutor' may be neither null nor empty");
		notNull(workerExecutor,
		        "Argument 'workerExecutor' may be neither null nor empty");
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
		        "Argument 'channelPipelineFactory' may be neither null nor empty");
		notNull(localAddress,
		        "Argument 'localAddress' may be neither null nor empty");
		notNull(channelFactory,
		        "Argument 'channelFactory' may be neither null nor empty");
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

	ChannelMonitorRegistry getChannelMonitorRegistry() {
		return this.channelPipelineFactory.getChannelMonitorRegistry();
	}

	State getCurrentState() {
		return this.currentState.get();
	}

	@Override
	public String toString() {
		return "GatewayServer@" + hashCode() + "[instanceId: "
		        + this.instanceId + "|localAddress: " + this.localAddress + "]";
	}

	public abstract class State {

		private final String	name;

		State(final String name) {
			this.name = name;
		}

		public final String getName() {
			return this.name;
		}

		abstract void start() throws Exception;

		abstract void stop() throws Exception;

		@Override
		public abstract String toString();
	}

	private final class Stopped extends State {

		Stopped() {
			super("STOPPED");
		}

		@Override
		void start() throws Exception {
			GatewayServer.this.log.info("Starting {} ...", GatewayServer.this);
			final long start = System.currentTimeMillis();

			if (!GatewayServer.this.currentState.compareAndSet(
			        GatewayServer.this.stopped, GatewayServer.this.starting)) {
				GatewayServer.this.log
				        .warn("{} is not in state INITIAL - ignoring attempt to start",
				                GatewayServer.this);
				return;
			}

			final ServerBootstrap bootstrap = new ServerBootstrap(
			        GatewayServer.this.channelFactory);
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
			GatewayServer.this.log.info("{} started in [] ms", this, end
			        - start);
		}

		@Override
		void stop() throws Exception {
			GatewayServer.this.log
			        .warn("Ignoring request to stop {} - this GatewayServer has not been started yet",
			                GatewayServer.this);
		}

		@Override
		public String toString() {
			return "Stopped@" + hashCode() + "[name: " + getName() + "]";
		}
	}

	private final class Starting extends State {

		Starting() {
			super("STARTING");
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
			return "Starting@" + hashCode() + "[name: " + getName() + "]";
		}
	}

	private final class Running extends State {

		Running() {
			super("Running");
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
			GatewayServer.this.log.info("{} stopped in [] ms", this, end
			        - start);
		}

		@Override
		public String toString() {
			return "Running@" + hashCode() + "[name: " + getName() + "]";
		}
	}

	private final class Stopping extends State {

		Stopping() {
			super("STOPPING");
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
			return "Stopping@" + hashCode() + "[name: " + getName() + "]";
		}
	}
}
