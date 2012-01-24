/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public class ReceivedSmsAckedEvent<ID extends java.io.Serializable> extends
        DownstreamReceivedMessageAckedEvent<ID, Sms> {

	public ReceivedSmsAckedEvent(final ID messageReference,
	        final Channel channel, final Sms message) {
		super(messageReference, MessageType.RECEIVED_SMS_ACKED, channel,
		        message, Acknowledgement.ack());
	}
}
