/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import java.util.Date;

import org.jboss.netty.channel.UpstreamMessageEvent;

/**
 * @author obergner
 * 
 */
public class NoWindowForIncomingMessageAvailableEvent extends
        UpstreamMessageEvent {

	private final int	maximumWindowCapacity;

	private final long	waitTimeMillis;

	private final long	creationTimestamp;

	NoWindowForIncomingMessageAvailableEvent(
	        final UpstreamMessageEvent rejectedMessage,
	        final int maximumWindowCapacity, final long waitTimeMillis) {
		super(rejectedMessage.getChannel(), rejectedMessage.getMessage(),
		        rejectedMessage.getRemoteAddress());
		this.maximumWindowCapacity = maximumWindowCapacity;
		this.waitTimeMillis = waitTimeMillis;
		this.creationTimestamp = System.currentTimeMillis();
	}

	public int getMaximumWindowCapacity() {
		return this.maximumWindowCapacity;
	}

	public long getWaitTimeMillis() {
		return this.waitTimeMillis;
	}

	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public Date getCreationTime() {
		return new Date(this.creationTimestamp);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
		        * result
		        + (int) (this.creationTimestamp ^ (this.creationTimestamp >>> 32));
		result = prime * result + this.maximumWindowCapacity;
		result = prime * result
		        + (int) (this.waitTimeMillis ^ (this.waitTimeMillis >>> 32));
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
		final NoWindowForIncomingMessageAvailableEvent other = (NoWindowForIncomingMessageAvailableEvent) obj;
		if (this.creationTimestamp != other.creationTimestamp) {
			return false;
		}
		if (this.maximumWindowCapacity != other.maximumWindowCapacity) {
			return false;
		}
		if (this.waitTimeMillis != other.waitTimeMillis) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "NoWindowForIncomingMessageAvailableEvent@" + this.hashCode()
		        + " [maximumWindowCapacity: " + this.maximumWindowCapacity
		        + "|waitTimeMillis: " + this.waitTimeMillis
		        + "|creationTimestamp: " + this.creationTimestamp
		        + "|channel: " + this.getChannel() + "|rejectedMessage: "
		        + this.getMessage() + "|remoteAddress: "
		        + this.getRemoteAddress() + "]";
	}

}
