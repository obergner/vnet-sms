/**
 * 
 */
package vnet.sms.gateway.server.framework;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import vnet.sms.gateway.server.framework.channel.GatewayServerChannelPipelineFactory;

/**
 * @author obergner
 * 
 */
public class GatewayServerBuilder<ID extends java.io.Serializable, TP>
        implements FactoryBean<GatewayServer<ID, TP>>, InitializingBean,
        DisposableBean {

	private static final String	                        DEFAULT_HOST	= "127.0.0.1";

	private final Logger	                            log	           = LoggerFactory
	                                                                           .getLogger(getClass());

	private String	                                    instanceId;

	private String	                                    host	       = DEFAULT_HOST;

	private int	                                        port	       = -1;

	private Executor	                                bossExecutor	= Executors
	                                                                           .newCachedThreadPool();

	private Executor	                                workerExecutor	= Executors
	                                                                           .newCachedThreadPool();

	private GatewayServerChannelPipelineFactory<ID, TP>	channelPipelineFactory;

	private GatewayServer<ID, TP>	                    product;

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.log.info("Starting to build GatewayServer instance ...");
		if (this.product != null) {
			throw new IllegalStateException(
			        "Illegal attempt to build GatewayServer twice");
		}
		this.product = new GatewayServer<ID, TP>(this.instanceId, this.host,
		        this.port, this.channelPipelineFactory, this.bossExecutor,
		        this.workerExecutor);
		this.log.info("Finished building GatewayServer instance {}",
		        this.product);
	}

	@Override
	public GatewayServer<ID, TP> getObject() throws Exception {
		if (this.product == null) {
			throw new IllegalStateException(
			        "No GatewayServer has been built yet - did you remember to call afterPropertiesSet() when using this factory outside Spring?");
		}
		return this.product;
	}

	@Override
	public Class<?> getObjectType() {
		return this.product != null ? this.product.getClass()
		        : GatewayServer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		this.product.stop();
	}

	/**
	 * @param instanceId
	 *            the instanceId to set
	 */
	@Required
	public final void setInstanceId(final String instanceId) {
		notEmpty(instanceId,
		        "Argument 'instanceId' may be neither null nor empty");
		this.instanceId = instanceId;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public final void setHost(final String host) {
		notEmpty(host, "Argument 'host' may be neither null nor empty");
		this.host = host;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	@Required
	public final void setPort(final int port) {
		this.port = port;
	}

	/**
	 * @param bossExecutor
	 *            the bossExecutor to set
	 */
	public final void setBossExecutor(final Executor bossExecutor) {
		notNull(bossExecutor, "Argument 'bossExecutor' must not be null");
		this.bossExecutor = bossExecutor;
	}

	/**
	 * @param workerExecutor
	 *            the workerExecutor to set
	 */
	public final void setWorkerExecutor(final Executor workerExecutor) {
		notNull(workerExecutor, "Argument 'workerExecutor' must not be null");
		this.workerExecutor = workerExecutor;
	}

	/**
	 * @param channelPipelineFactory
	 *            the channelPipelineFactory to set
	 */
	@Required
	public final void setChannelPipelineFactory(
	        final GatewayServerChannelPipelineFactory<ID, TP> channelPipelineFactory) {
		notNull(channelPipelineFactory,
		        "Argument 'channelPipelineFactory' must not be null");
		this.channelPipelineFactory = channelPipelineFactory;
	}
}
