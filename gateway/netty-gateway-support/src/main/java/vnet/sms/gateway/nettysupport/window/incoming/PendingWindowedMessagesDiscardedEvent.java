/**
 * 
 */
package vnet.sms.gateway.nettysupport.window.incoming;

import static org.jboss.netty.channel.Channels.succeededFuture;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public class PendingWindowedMessagesDiscardedEvent<ID extends Serializable>
        implements ChannelEvent {

	private final Channel	       channel;

	private final Map<ID, Message>	discardedMessages	= new HashMap<ID, Message>();

	private final long	           creationTimestamp;

	PendingWindowedMessagesDiscardedEvent(final Channel channel,
	        final Map<ID, Message> discardedMessages) {
		this.channel = channel;
		this.discardedMessages.putAll(discardedMessages);
		this.creationTimestamp = System.currentTimeMillis();
	}

	/**
	 * @see org.jboss.netty.channel.ChannelEvent#getChannel()
	 */
	@Override
	public Channel getChannel() {
		return this.channel;
	}

	/**
	 * @see org.jboss.netty.channel.ChannelEvent#getFuture()
	 */
	@Override
	public ChannelFuture getFuture() {
		return succeededFuture(getChannel());
	}

	public Map<ID, Message> getDiscardedMessages() {
		return Collections.unmodifiableMap(this.discardedMessages);
	}

	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public Date getCreationTime() {
		return new Date(this.creationTimestamp);
	}

	@Override
	public String toString() {
		return "PendingWindowedMessagesDiscardedEvent@" + hashCode()
		        + " [channel: " + this.channel + "|discardedMessages: "
		        + this.discardedMessages + "|creationTimestamp: "
		        + this.creationTimestamp + "]";
	}
}
