/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public interface MessageAcknowledgementContainer<ID extends Serializable, M extends Message> {

	MessageType getAcknowledgedMessageType();

	Acknowledgement getAcknowledgement();

	boolean isAccepted();

	ID getAcknowledgedMessageReference();

	int getReceivingChannelId();

	M getAcknowledgedMessage();
}
