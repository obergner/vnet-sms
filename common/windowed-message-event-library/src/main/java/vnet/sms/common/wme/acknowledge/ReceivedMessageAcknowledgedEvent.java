/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import org.jboss.netty.channel.MessageEvent;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.MessageEventType;

/**
 * @author obergner
 * 
 */
public interface ReceivedMessageAcknowledgedEvent<ID extends Serializable, M extends Message>
        extends MessageEvent {

	MessageEventType getAcknowledgedMessageType();

	Acknowledgement getAcknowledgement();

	boolean isAccepted();

	ID getAcknowledgedMessageReference();

	@Override
	M getMessage();
}
