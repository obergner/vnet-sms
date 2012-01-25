/**
 * 
 */
package vnet.sms.common.wme.send;

import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public interface MessageSendContainer<M extends Message> {

	MessageType getMessageType();

	M getMessage();
}
