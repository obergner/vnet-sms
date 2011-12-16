/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public interface ReceivedMessageAckedEvent<ID extends Serializable, M extends Message>
        extends WindowedMessageEvent<ID, M> {

	Acknowledgement getAcknowledgement();

	boolean isAccepted();
}
