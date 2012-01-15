/**
 * 
 */
package vnet.sms.gateway.server.serialization.minimal;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import vnet.sms.gateway.server.framework.Context;
import vnet.sms.gateway.server.framework.GatewayServerController;

/**
 * @author obergner
 * 
 */
public class Main<ID extends Serializable, TP> {

	private final Logger	log	= LoggerFactory.getLogger(getClass());

	public static <ID extends Serializable, TP> void main(final String[] args)
	        throws Exception {
		new Main<ID, TP>().run();
	}

	public void run() throws Exception {
		this.log.info("Booting GatewayServer ...");

		final ApplicationContext serverContext = loadServerContextFromClasspath();

		final GatewayServerController<ID, TP> serverController = lookupGatewayServerControllerIn(serverContext);

		enableHangupSupportFor(serverController);

		serverController.start();

		this.log.info("GatewayServer is up and running");
	}

	private ApplicationContext loadServerContextFromClasspath()
	        throws BeansException {
		this.log.info("Loading server context from locations [{}] ...",
		        Context.ALL_CONTEXTS);
		final ApplicationContext serverContext = new ClassPathXmlApplicationContext(
		        Context.ALL_CONTEXTS);
		this.log.info("Server context [{}] loaded from locations [{}]",
		        serverContext, Context.ALL_CONTEXTS);
		return serverContext;
	}

	private GatewayServerController<ID, TP> lookupGatewayServerControllerIn(
	        final ApplicationContext serverContext) throws BeansException {
		this.log.info(
		        "Looking up GatewayServerController in server context [{}] ...",
		        serverContext);
		final GatewayServerController<ID, TP> serverController = serverContext
		        .getBean(GatewayServerController.class);
		this.log.info(
		        "Obtained GatewayServerController [{}] from server context [{}]",
		        serverController, serverContext);
		return serverController;
	}

	private void enableHangupSupportFor(
	        final GatewayServerController<ID, TP> serverController) {
		this.log.info("Enabling hang up support ...");
		final HangupInterceptor<ID, TP> hangupInterceptor = new HangupInterceptor<ID, TP>(
		        serverController);
		Runtime.getRuntime().addShutdownHook(hangupInterceptor);
		this.log.info("Enabled hang up support");
	}

	private Main() {
		// Noop
	}

	private static final class HangupInterceptor<ID extends Serializable, TP>
	        extends Thread {

		private final Logger		                  log	= LoggerFactory
		                                                          .getLogger(this
		                                                                  .getClass());

		private final GatewayServerController<ID, TP>	serverController;

		HangupInterceptor(final GatewayServerController<ID, TP> serverController) {
			this.serverController = serverController;
		}

		@Override
		public void run() {
			try {
				this.log.info(
				        "Received hang up signal - stopping GatewayServer instance {} ...",
				        this.serverController);
				this.serverController.stop();
				this.log.info("GatewayServer instance {} stopped",
				        this.serverController);
			} catch (final Exception ex) {
				this.log.warn("Error during stopping GatewayServer instance "
				        + this.serverController, ex);
			}
		}
	}
}
