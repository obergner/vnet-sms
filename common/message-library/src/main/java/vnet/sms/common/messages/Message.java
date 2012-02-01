/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author obergner
 * 
 */
public abstract class Message implements Serializable, Comparable<Message> {

	private static final long	serialVersionUID	= 2543387338512341954L;

	private final UUID	      id;

	private final long	      creationTimestamp;

	protected Message() {
		this(UUID.randomUUID(), System.currentTimeMillis());
	}

	protected Message(final UUID id, final long creationTimestamp) {
		notNull(id, "Argument 'id' must not be null");
		this.id = id;
		this.creationTimestamp = creationTimestamp;
	}

	public final UUID getId() {
		return this.id;
	}

	public final long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public final Date getCreationTime() {
		return new Date(this.creationTimestamp);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Message other) {
		return this.id.compareTo(other.id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Message other = (Message) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
