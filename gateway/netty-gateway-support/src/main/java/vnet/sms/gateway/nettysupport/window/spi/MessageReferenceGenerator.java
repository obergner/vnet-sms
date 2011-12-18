/**
 * 
 */
package vnet.sms.gateway.nettysupport.window.spi;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public interface MessageReferenceGenerator<ID extends Serializable> {

	ID nextMessageReference();
}
