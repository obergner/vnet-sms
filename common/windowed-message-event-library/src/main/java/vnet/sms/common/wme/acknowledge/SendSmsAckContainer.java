/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public final class SendSmsAckContainer<ID extends Serializable> extends
        AbstractMessageAcknowledgementContainer<ID, Sms> {

	private static final long	serialVersionUID	= -8271142626779985541L;

	public SendSmsAckContainer(final ID acknowledgedMessageReference,
	        final int receivingChannelId, final Sms acknowledgedMessage) {
		super(MessageEventType.SEND_SMS_ACK, Acknowledgement.ack(),
		        acknowledgedMessageReference, receivingChannelId,
		        acknowledgedMessage);
	}
}
