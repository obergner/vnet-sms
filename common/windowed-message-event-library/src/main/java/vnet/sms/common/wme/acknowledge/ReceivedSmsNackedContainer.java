/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public final class ReceivedSmsNackedContainer<ID extends Serializable> extends
        AbstractMessageNackContainer<ID, Sms> {

	private static final long	serialVersionUID	= -6876251129317808230L;

	public ReceivedSmsNackedContainer(final int errorKey,
	        final String errorDescription,
	        final ID acknowledgedMessageReference,
	        final int receivingChannelId, final Sms acknowledgedMessage) {
		super(errorKey, errorDescription, MessageEventType.RECEIVED_SMS_NACKED,
		        acknowledgedMessageReference, receivingChannelId,
		        acknowledgedMessage);
	}
}
