/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public interface MessageAcknowledgementContainer<ID extends Serializable, M extends GsmPdu> {

	MessageEventType getAcknowledgedMessageType();

	Acknowledgement getAcknowledgement();

	boolean isAccepted();

	ID getAcknowledgedMessageReference();

	int getReceivingChannelId();

	M getAcknowledgedMessage();
}
