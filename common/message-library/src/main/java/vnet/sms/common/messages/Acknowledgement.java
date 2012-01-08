/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public class Acknowledgement implements Serializable {

	private static final long	serialVersionUID	= 7954930395340440259L;

	public static final Acknowledgement ack() {
		return new Acknowledgement(Status.ACK);
	}

	public static final Acknowledgement nack() {
		return new Acknowledgement(Status.NACK);
	}

	public enum Status {
		ACK,

		NACK;
	}

	private final Status	status;

	private Acknowledgement(final Status status) {
		notNull(status, "Argument 'status' must not be null");
		this.status = status;
	}

	public boolean is(final Status status) {
		return this.status == status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.status == null) ? 0 : this.status.hashCode());
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
		final Acknowledgement other = (Acknowledgement) obj;
		if (this.status != other.status) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Acknowledgement@" + this.hashCode() + "[status: " + this.status
		        + "]";
	}
}
