/**
 * 
 */
package vnet.sms.gateway.server.framework;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
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

	public static final int	                                  DEFAULT_LISTEN_PORT	= 5634;

	private final Logger	                                  log	              = LoggerFactory
	                                                                                      .getLogger(getClass());

	private final String	                                  instanceId;

	private final int	                                      listenPort;

	private final ChannelFactory	                          channelFactory;

	private final GatewayServerChannelPipelineFactory<ID, TP>	channelPipelineFactory;

	private final ChannelMonitorRegistry	                  channelMonitorRegistry;

	GatewayServer(
	        final String instanceId,
	        final int listenPort,
	        final GatewayServerChannelPipelineFactory<ID, TP> channelPipelineFactory,
	        final Executor bossExecutor, final Executor workerExecutor,
	        final ChannelMonitorRegistry channelMonitorRegistry) {
		notEmpty(instanceId,
		        "Argument 'instanceId' may be neither null nor empty");
		notNull(channelPipelineFactory,
		        "Argument 'channelPipelineFactory' may be neither null nor empty");
		notNull(bossExecutor,
		        "Argument 'bossExecutor' may be neither null nor empty");
		notNull(workerExecutor,
		        "Argument 'workerExecutor' may be neither null nor empty");
		notNull(channelMonitorRegistry,
		        "Argument 'channelMonitorRegistry' may be neither null nor empty");
		this.instanceId = instanceId;
		this.listenPort = listenPort;
		this.channelPipelineFactory = channelPipelineFactory;
		this.channelMonitorRegistry = channelMonitorRegistry;
		this.channelFactory = new NioServerSocketChannelFactory(bossExecutor,
		        workerExecutor);
	}

	void start() throws Exception {
		this.log.info("Starting {} ...", this);
		final long start = System.currentTimeMillis();

		final ServerBootstrap bootstrap = new ServerBootstrap(
		        this.channelFactory);
		bootstrap.bind(new InetSocketAddress(this.listenPort));

		final long end = System.currentTimeMillis();
		this.log.info("{} started in [] ms", this, end - start);
	}

	void stop() throws Exception {
		this.log.info("Stopping {} ...", this);
		final long start = System.currentTimeMillis();

		final int numberOfChannels = this.channelPipelineFactory
		        .getAllConnectedChannels().size();
		this.log.info("Closing [{}] open channels ...", numberOfChannels);
		this.channelPipelineFactory.getAllConnectedChannels().close()
		        .awaitUninterruptibly();
		this.log.info("Closed [{}] open channels", numberOfChannels);

		this.channelFactory.releaseExternalResources();

		final long end = System.currentTimeMillis();
		this.log.info("{} stopped in [] ms", this, end - start);
	}

	ChannelMonitorRegistry getChannelMonitorRegistry() {
		return this.channelMonitorRegistry;
	}

	@Override
	public String toString() {
		return "GatewayServer@" + hashCode() + " [instanceId: "
		        + this.instanceId + "|listenPort: " + this.listenPort + "]";
	}
}
