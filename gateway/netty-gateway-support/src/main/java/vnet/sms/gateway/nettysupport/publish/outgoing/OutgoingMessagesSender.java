/**
 * 
 */
package vnet.sms.gateway.nettysupport.publish.outgoing;

import java.io.Serializable;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.WindowedMessageEvent;

/**
 * @author obergner
 * 
 */
public interface OutgoingMessagesSender<ID extends Serializable> {

	void send(WindowedMessageEvent<ID, ? extends Message> sms) throws Exception;
}
