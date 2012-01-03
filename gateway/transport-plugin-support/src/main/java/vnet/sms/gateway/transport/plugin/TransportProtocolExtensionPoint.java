/**
 * 
 */
package vnet.sms.gateway.transport.plugin;

import java.io.Serializable;

import vnet.sms.gateway.transport.spi.TransportProtocolPlugin;

/**
 * @author obergner
 * 
 */
public interface TransportProtocolExtensionPoint<ID extends Serializable, TP> {

	void plugin(TransportProtocolPlugin<ID, TP> plugin);
}
