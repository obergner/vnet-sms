/**
 * 
 */
package vnet.sms.gateway.nettysupport.window.spi;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public interface WindowIdGenerator<ID extends Serializable> {

	ID nextWindowId();
}
