/**
 * 
 */
package vnet.routing.netty.server.support.window.spi;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public interface WindowStoreCapacityExhaustedHandler {

	void onMessageRejected(final long messageId, final Serializable message);
}
