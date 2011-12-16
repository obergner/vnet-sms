/**
 * 
 */
package vnet.routing.netty.server.support.window.spi;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public interface MessageReleaseFailureHandler {

	void onMessageReleaseFailed(final long messageId, final Serializable message);
}
