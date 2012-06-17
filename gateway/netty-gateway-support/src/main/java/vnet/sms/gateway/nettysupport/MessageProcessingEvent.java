/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.net.SocketAddress;

import vnet.sms.common.messages.GsmPdu;

/**
 * @author obergner
 * 
 */
public interface MessageProcessingEvent<M extends GsmPdu> extends
        IdentifiableChannelEvent {

	M getMessage();

	SocketAddress getRemoteAddress();
}
