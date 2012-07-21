/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import org.springframework.jmx.export.annotation.ManagedOperation;

/**
 * @author obergner
 * 
 */
public interface ChannelController {

	@ManagedOperation(description = "Closes this channel - this channel cannot be reused afterwards")
	void close();
}
