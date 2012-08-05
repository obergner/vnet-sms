/**
 * 
 */
package vnet.sms.common.wme.receive;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageEventType;

/**
 * @author obergner
 * 
 */
public class ReceivedSmsEvent<ID extends Serializable> extends
        UpstreamWindowedMessageEvent<ID, Sms> {

	public ReceivedSmsEvent(final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent, final Sms sms) {
		super(messageReference, MessageEventType.RECEIVED_SMS,
		        upstreamMessageEvent, sms);
	}
}
