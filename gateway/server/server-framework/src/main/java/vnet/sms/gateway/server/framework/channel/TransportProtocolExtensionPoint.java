/**
 * 
 */
package vnet.sms.gateway.server.framework.channel;

import java.io.Serializable;

import vnet.sms.gateway.server.framework.spi.TransportProtocolPlugin;

/**
 * @author obergner
 * 
 */
public interface TransportProtocolExtensionPoint<ID extends Serializable, TP> {

	void plugin(TransportProtocolPlugin<ID, TP> plugin);
}
