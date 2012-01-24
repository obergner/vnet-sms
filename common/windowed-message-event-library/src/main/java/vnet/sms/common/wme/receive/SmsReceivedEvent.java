/**
 * 
 */
package vnet.sms.common.wme.receive;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public class SmsReceivedEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, Sms> {

	public SmsReceivedEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent, final Sms sms) {
		super(messageReference, MessageType.SMS_RECEIVED, upstreamMessageEvent,
		        sms);
	}
}
