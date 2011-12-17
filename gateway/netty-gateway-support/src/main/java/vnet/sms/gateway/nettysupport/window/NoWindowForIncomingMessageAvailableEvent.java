/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.AbstractInternalMessageProcessingErrorEvent;

/**
 * @author obergner
 * 
 */
public class NoWindowForIncomingMessageAvailableEvent extends
        AbstractInternalMessageProcessingErrorEvent<Message> {

	private final int	maximumWindowCapacity;

	private final long	waitTimeMillis;

	NoWindowForIncomingMessageAvailableEvent(
	        final UpstreamMessageEvent rejectedMessage,
	        final int maximumWindowCapacity, final long waitTimeMillis) {
		super(rejectedMessage.getChannel(), (Message) rejectedMessage
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
		        + " [maximumWindowCapacity: " + this.maximumWindowCapacity
		        + "|waitTimeMillis: " + this.waitTimeMillis
		        + "|creationTimestamp: " + getCreationTimestamp()
		        + "|channel: " + getChannel() + "|rejectedMessage: "
		        + getFailedMessage() + "|remoteAddress: " + getRemoteAddress()
		        + "]";
	}
}
