/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.gateway.nettysupport.AbstractMessageProcessingEvent;

/**
 * @author obergner
 * 
 */
public class NoWindowForIncomingMessageAvailableEvent extends
        AbstractMessageProcessingEvent<GsmPdu> {

	private final int	maximumWindowCapacity;

	private final long	waitTimeMillis;

	NoWindowForIncomingMessageAvailableEvent(
	        final UpstreamMessageEvent rejectedMessage,
	        final int maximumWindowCapacity, final long waitTimeMillis) {
		super(rejectedMessage.getChannel(), (GsmPdu) rejectedMessage
		        .getMessage(), rejectedMessage.getRemoteAddress());
		this.maximumWindowCapacity = maximumWindowCapacity;
		this.waitTimeMillis = waitTimeMillis;
	}

	public int getMaximumWindowCapacity() {
		return this.maximumWindowCapacity;
	}

	public long getWaitTimeMillis() {
		return this.waitTimeMillis;
	}

	@Override
	public String toString() {
		return "NoWindowForIncomingMessageAvailableEvent@" + this.hashCode()
		        + "[id: " + getId() + "|creationTimestamp: "
		        + getCreationTimestamp() + "|channel: " + getChannel()
		        + "|maximumWindowCapacity: " + this.maximumWindowCapacity
		        + "|waitTimeMillis: " + this.waitTimeMillis
		        + "|rejectedMessage: " + getMessage() + "|remoteAddress: "
		        + getRemoteAddress() + "]";
	}
}
