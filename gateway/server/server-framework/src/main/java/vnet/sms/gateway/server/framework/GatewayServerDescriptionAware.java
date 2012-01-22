/**
 * 
 */
package vnet.sms.gateway.server.framework;

import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;

/**
 * @author obergner
 * 
 */
public interface GatewayServerDescriptionAware {

	void setGatewayServerDescription(
	        GatewayServerDescription gatewayServerDescription);
}
