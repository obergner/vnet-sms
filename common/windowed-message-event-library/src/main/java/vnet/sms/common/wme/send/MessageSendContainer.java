/**
 * 
 */
package vnet.sms.common.wme.send;

import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.MessageEventType;

/**
 * @author obergner
 * 
 */
public interface MessageSendContainer<M extends Message> {

	MessageEventType getMessageType();

	M getMessage();
}
