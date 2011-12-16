package vnet.routing.netty.server.support.window;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public interface WindowStoreMBean {

	String getOwnerUid();

	int getMaximumCapacity();

	int getCurrentMessageCount();

	boolean storeMessage(final long messageId, final Serializable message)
			throws IllegalArgumentException;

	Serializable releaseMessage(final long messageId)
			throws IllegalArgumentException;

	void shutDown();

}