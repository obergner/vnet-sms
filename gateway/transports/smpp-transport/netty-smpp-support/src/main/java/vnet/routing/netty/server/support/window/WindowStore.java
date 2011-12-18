package vnet.routing.netty.server.support.window;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import vnet.routing.netty.server.support.window.spi.UnreleasedMessagesHandler;

/**
 * @author obergner
 * 
 */
public class WindowStore implements WindowStoreMBean {

	private final String ownerUid;

	private final int maximumCapacity;

	private final Map<Long, Serializable> messageIdToMessage;

	private final UnreleasedMessagesHandler unreleasedMessagesHandler;

	private final ReentrantLock capacityEnsuringLock = new ReentrantLock();

	private volatile boolean shutDown = false;

	public WindowStore(final String ownerUid, final int maximumCapacity,
			final UnreleasedMessagesHandler unreleasedMessagesHandler) {
		notEmpty(ownerUid,
				"Argument 'ownerUid' must be neither null nor empty. Got: "
						+ ownerUid);
		this.ownerUid = ownerUid;
		this.maximumCapacity = maximumCapacity;
		this.messageIdToMessage = new HashMap<Long, Serializable>(
				maximumCapacity);
		this.unreleasedMessagesHandler = unreleasedMessagesHandler;
	}

	/**
	 * @see vnet.routing.netty.server.support.window.WindowStoreMBean#getOwnerUid()
	 */
	@Override
	public final String getOwnerUid() {
		return this.ownerUid;
	}

	public final String getObjectName() {
		return "service=WindowStore,owner=" + this.ownerUid;
	}

	/**
	 * @see vnet.routing.netty.server.support.window.WindowStoreMBean#getMaximumCapacity()
	 */
	@Override
	public final int getMaximumCapacity() {
		return this.maximumCapacity;
	}

	/**
	 * @see vnet.routing.netty.server.support.window.WindowStoreMBean#getCurrentMessageCount()
	 */
	@Override
	public final int getCurrentMessageCount() {
		return this.messageIdToMessage.size();
	}

	/**
	 * @see vnet.routing.netty.server.support.window.WindowStoreMBean#storeMessage(long,
	 *      java.io.Serializable)
	 */
	@Override
	public boolean storeMessage(final long messageId, final Serializable message)
			throws IllegalArgumentException {
		notNull(message, "Cannot store a null message");
		ensureNotShutDown();
		try {
			this.capacityEnsuringLock.lock();
			return storeMessageHoldingLock(messageId, message);
		} finally {
			this.capacityEnsuringLock.unlock();
		}
	}

	private void ensureNotShutDown() throws IllegalArgumentException {
		if (this.shutDown) {
			throw new IllegalArgumentException(
					"This WindowStore has already been shut down");
		}
	}

	private boolean storeMessageHoldingLock(final long messageId,
			final Serializable message) throws IllegalArgumentException {
		if (this.messageIdToMessage.size() >= this.maximumCapacity) {
			return false;
		}

		final Serializable storedMessageHavingSameId = this.messageIdToMessage
				.get(messageId);
		if (storedMessageHavingSameId != null) {
			throw new IllegalArgumentException("Another message ["
					+ storedMessageHavingSameId + "] having the same ID ["
					+ messageId + "] has already been stored");
		}

		this.messageIdToMessage.put(messageId, message);

		return true;
	}

	/**
	 * @see vnet.routing.netty.server.support.window.WindowStoreMBean#releaseMessage(long)
	 */
	@Override
	public Serializable releaseMessage(final long messageId)
			throws IllegalArgumentException {
		ensureNotShutDown();
		try {
			this.capacityEnsuringLock.lock();
			return releaseMessageHoldingLock(messageId);
		} finally {
			this.capacityEnsuringLock.unlock();
		}
	}

	private Serializable releaseMessageHoldingLock(final long messageId)
			throws IllegalArgumentException {
		final Serializable releasedMessage = this.messageIdToMessage
				.remove(messageId);
		if (releasedMessage == null) {
			throw new IllegalArgumentException(
					"Illegal attempt to release a non-existing message: no message having the supplied ID ["
							+ messageId + "] is stored");
		}

		return releasedMessage;
	}

	/**
	 * @see vnet.routing.netty.server.support.window.WindowStoreMBean#shutDown()
	 */
	@Override
	public void shutDown() {
		if (this.shutDown) {
			return;
		}
		try {
			this.capacityEnsuringLock.lock();

			this.unreleasedMessagesHandler.onShutDown(Collections
					.unmodifiableMap(this.messageIdToMessage));

			this.shutDown = true; // Volatile write
		} finally {
			this.capacityEnsuringLock.unlock();
		}
	}

	@Override
	public String toString() {
		return "WindowStore@" + hashCode() + "[ownerUid = " + this.ownerUid
				+ "|maximumCapacity = " + this.maximumCapacity
				+ "|messageIdToMessage = " + this.messageIdToMessage
				+ "|unreleasedMessagesHandler = "
				+ this.unreleasedMessagesHandler + "|capacityEnsuringLock = "
				+ this.capacityEnsuringLock + "|shutDown = " + this.shutDown
				+ "]";
	}
}
