/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.nettysupport.AbstractIdentifiableChannelEvent;

/**
 * @author obergner
 * 
 */
public class PendingWindowedMessagesDiscardedEvent<ID extends Serializable>
        extends AbstractIdentifiableChannelEvent {

	private final Map<ID, Message>	discardedMessages	= new HashMap<ID, Message>();

	PendingWindowedMessagesDiscardedEvent(final Channel channel,
	        final Map<ID, Message> discardedMessages) {
		super(channel);
		this.discardedMessages.putAll(discardedMessages);
	}

	public Map<ID, Message> getDiscardedMessages() {
		return Collections.unmodifiableMap(this.discardedMessages);
	}

	@Override
	public String toString() {
		return "PendingWindowedMessagesDiscardedEvent@" + hashCode() + " [id: "
		        + getId() + "|channel: " + getChannel()
		        + "|discardedMessages: " + this.discardedMessages
		        + "|creationTimestamp: " + getCreationTimestamp() + "]";
	}
}
