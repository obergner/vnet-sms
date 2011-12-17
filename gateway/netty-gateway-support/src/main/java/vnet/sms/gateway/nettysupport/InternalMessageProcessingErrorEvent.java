/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.net.SocketAddress;
import java.util.Date;
import java.util.UUID;

import org.jboss.netty.channel.ChannelEvent;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public interface InternalMessageProcessingErrorEvent<M extends Message> extends
        ChannelEvent {

	UUID getId();

	M getFailedMessage();

	SocketAddress getRemoteAddress();

	long getCreationTimestamp();

	Date getCreationTime();
}
