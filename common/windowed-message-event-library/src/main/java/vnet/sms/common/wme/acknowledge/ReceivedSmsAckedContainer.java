/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.messages.Sms;

/**
 * @author obergner
 * 
 */
public final class ReceivedSmsAckedContainer<ID extends Serializable> extends
        AbstractMessageAcknowledgementContainer<ID, Sms> {

	private static final long	serialVersionUID	= -8271142626779985541L;

	public ReceivedSmsAckedContainer(final ID acknowledgedMessageReference,
	        final int receivingChannelId, final Sms acknowledgedMessage) {
		super(MessageEventType.RECEIVED_SMS_ACKED, Acknowledgement.ack(),
		        acknowledgedMessageReference, receivingChannelId,
		        acknowledgedMessage);
	}
}
