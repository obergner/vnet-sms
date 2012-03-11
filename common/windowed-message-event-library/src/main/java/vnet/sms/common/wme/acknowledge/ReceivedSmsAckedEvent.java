/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.MessageEventType;
import vnet.sms.common.messages.Sms;

/**
 * @author obergner
 * 
 */
public class ReceivedSmsAckedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAcknowledgedEvent<ID, Sms> {

	public static final <I extends Serializable> ReceivedSmsAckedEvent<I> convert(
	        final MessageEvent receivedSmsAckedMessageEvent) {
		notNull(receivedSmsAckedMessageEvent,
		        "Argument 'receivedSmsAckedMessageEvent' must not be null");
		isTrue(receivedSmsAckedMessageEvent.getMessage() instanceof ReceivedSmsAckedContainer,
		        "Can only convert MessageEvents having a ReceivedSmsAckedContainer as their payload. Got: "
		                + receivedSmsAckedMessageEvent.getMessage());
		final ReceivedSmsAckedContainer<I> ackContainer = ReceivedSmsAckedContainer.class
		        .cast(receivedSmsAckedMessageEvent.getMessage());
		return new ReceivedSmsAckedEvent<I>(
		        ackContainer.getAcknowledgedMessageReference(),
		        receivedSmsAckedMessageEvent.getChannel(),
		        receivedSmsAckedMessageEvent.getFuture(),
		        ackContainer.getAcknowledgedMessage());
	}

	private ReceivedSmsAckedEvent(final ID messageReference,
	        final Channel channel, final ChannelFuture future, final Sms message) {
		super(messageReference, MessageEventType.RECEIVED_SMS_ACKED, channel,
		        message, Acknowledgement.ack());
	}
}
