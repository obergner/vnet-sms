/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import org.jboss.netty.channel.MessageEvent;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public interface ReceivedMessageAcknowledgedEvent<ID extends Serializable, M extends GsmPdu>
        extends MessageEvent {

	MessageEventType getAcknowledgedMessageType();

	Acknowledgement getAcknowledgement();

	boolean isAccepted();

	ID getAcknowledgedMessageReference();

	@Override
	M getMessage();
}
