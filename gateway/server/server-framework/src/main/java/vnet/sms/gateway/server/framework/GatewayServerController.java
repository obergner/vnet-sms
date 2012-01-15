/**
 * 
 */
package vnet.sms.gateway.server.framework;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.net.SocketAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @author obergner
 * 
 */
@ManagedResource(objectName = "vnet.sms.gateway.server:service=GatewayServerController", description = "A controller for starting, stopping and monitoring a GatewayServer instance")
public class GatewayServerController<ID extends Serializable, TP> {

	private final GatewayServer<ID, TP>	gatewayServer;

	/**
	 * @param gatewayServer
	 */
	public GatewayServerController(final GatewayServer<ID, TP> gatewayServer) {
		notNull(gatewayServer, "Argument 'gatewayServer' must not be null");
		this.gatewayServer = gatewayServer;
	}

	@PostConstruct
	@ManagedOperation(description = "Start the GatewayServer")
	public void start() throws Exception {
		this.gatewayServer.start();
	}

	@PreDestroy
	@ManagedOperation(description = "Stop the GatewayServer")
	public void stop() throws Exception {
		this.gatewayServer.stop();
	}

	@ManagedAttribute(description = "This GatewayServer's current state: stopped, starting, running or stopping")
	public String getCurrentStatus() {
		return this.gatewayServer.getCurrentStatus().name();
	}

	@ManagedAttribute(description = "This GatewayServer's instance ID, uniquely identifying this instance among all GatewayServers")
	public String getInstanceId() {
		return this.gatewayServer.getInstanceId();
	}

	@ManagedAttribute(description = "The socket address this GatewayServer is listening on")
	public SocketAddress getLocalAddress() {
		return this.gatewayServer.getLocalAddress();
	}
}
