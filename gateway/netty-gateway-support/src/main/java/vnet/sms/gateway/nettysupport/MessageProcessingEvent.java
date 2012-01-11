/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.net.SocketAddress;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public interface MessageProcessingEvent<M extends Message> extends
        IdentifiableChannelEvent {

	M getMessage();

	SocketAddress getRemoteAddress();
}
