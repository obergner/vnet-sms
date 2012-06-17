/**
 * 
 */
package vnet.sms.common.wme.send;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public interface MessageSendContainer<M extends GsmPdu> {

	MessageEventType getMessageType();

	M getMessage();
}
