/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Message;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public class DownstreamReceivedMessageAcknowledgedEvent<ID extends Serializable, M extends Message>
        extends DownstreamMessageEvent implements
        ReceivedMessageAcknowledgedEvent<ID, M> {

	private final ID	          acknowledgedMessageReference;

	private final MessageType	  acknowledgedMessageType;

	private final Acknowledgement	acknowledgement;

	protected DownstreamReceivedMessageAcknowledgedEvent(
	        final ID acknowledgedMessageReference,
	        final MessageType acknowledgedMessageType, final Channel channel,
	        final M message, final Acknowledgement acknowledgement) {
		this(acknowledgedMessageReference, acknowledgedMessageType, channel,
		        Channels.future(channel, false), message, acknowledgement);
	}

	private DownstreamReceivedMessageAcknowledgedEvent(
	        final ID acknowledgedMessageReference,
	        final MessageType acknowledgedMessageType, final Channel channel,
	        final ChannelFuture future, final Object message,
	        final Acknowledgement acknowledgement) {
		super(channel, future, message, channel.getRemoteAddress());
		notNull(acknowledgedMessageReference,
		        "Argument 'acknowledgedMessageReference' must not be null");
		notNull(acknowledgedMessageType,
		        "Argument 'acknowledgedMessageType' must not be null");
		notNull(channel, "Argument 'channel' must not be null");
		notNull(future, "Argument 'future' must not be null");
		notNull(message, "Argument 'message' must not be null");
		notNull(acknowledgement, "Argument 'acknowledgement' must not be null");
		this.acknowledgedMessageReference = acknowledgedMessageReference;
		this.acknowledgedMessageType = acknowledgedMessageType;
		this.acknowledgement = acknowledgement;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.ReceivedMessageAcknowledgedEvent#getAcknowledgedMessageType()
	 */
	@Override
	public MessageType getAcknowledgedMessageType() {
		return this.acknowledgedMessageType;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.ReceivedMessageAcknowledgedEvent#getAcknowledgedMessageReference()
	 */
	@Override
	public ID getAcknowledgedMessageReference() {
		return this.acknowledgedMessageReference;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.ReceivedMessageAcknowledgedEvent#getAcknowledgement
	 *      ()
	 */
	@Override
	public Acknowledgement getAcknowledgement() {
		return this.acknowledgement;
	}

	/**
	 * @see vnet.sms.common.wme.acknowledge.ReceivedMessageAcknowledgedEvent#isAccepted()
	 */
	@Override
	public boolean isAccepted() {
		return this.acknowledgement.is(Acknowledgement.Status.ACK);
	}

	/**
	 * @see org.jboss.netty.channel.DownstreamMessageEvent#getMessage()
	 */
	@Override
	public M getMessage() {
		return (M) super.getMessage();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + this.hashCode()
		        + "[acknowledgedMessageReference: "
		        + getAcknowledgedMessageReference() + "|message: "
		        + getMessage() + "|acknowledgement: " + this.acknowledgement
		        + "|channel: " + getChannel() + "|future: " + getFuture()
		        + "|remoteAddress: " + getRemoteAddress() + "]";
	}
}
