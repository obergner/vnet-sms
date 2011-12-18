/**
 * 
 */
package vnet.routing.netty.server.support.window.spi;

import java.io.Serializable;
import java.util.Map;

/**
 * @author obergner
 * 
 */
public interface UnreleasedMessagesHandler {

	void onShutDown(final Map<Long, Serializable> unreleasedMessages);
}
