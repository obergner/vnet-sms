/**
 * 
 */
package vnet.sms.common.wme;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public abstract class DownstreamReceivedMessageAckedEvent<ID extends Serializable, M extends Message>
        extends DownstreamWindowedMessageEvent<ID, M> implements
        ReceivedMessageAckedEvent<ID, M> {

	private final Acknowledgement	acknowledgement;

	protected DownstreamReceivedMessageAckedEvent(final ID messageReference,
	        final Type type, final Channel channel, final M message,
	        final Acknowledgement acknowledgement) {
		this(messageReference, type, channel, Channels.future(channel, false),
		        message, acknowledgement);
	}

	private DownstreamReceivedMessageAckedEvent(final ID messageReference,
	        final Type type, final Channel channel, final ChannelFuture future,
	        final Object message, final Acknowledgement acknowledgement) {
		super(messageReference, type, channel, future, message);
		notNull(messageReference,
		        "Argument 'messageReference' must not be null");
		notNull(acknowledgement, "Argument 'acknowledgement' must not be null");
		notNull(channel, "Argument 'channel' must not be null");
		notNull(message, "Argument 'message' must not be null");
		this.acknowledgement = acknowledgement;
	}

	/**
	 * @see vnet.sms.common.wme.ReceivedMessageAckedEvent#getAcknowledgement ()
	 */
	@Override
	public Acknowledgement getAcknowledgement() {
		return this.acknowledgement;
	}

	/**
	 * @see vnet.sms.common.wme.ReceivedMessageAckedEvent#isAccepted()
	 */
	@Override
	public boolean isAccepted() {
		return this.acknowledgement.is(Acknowledgement.Status.ACK);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + this.hashCode()
		        + "[messageReference: " + getMessageReference() + "|message: "
		        + getMessage() + "|acknowledgement: " + this.acknowledgement
		        + "|channel: " + getChannel() + "|future: " + getFuture()
		        + "|remoteAddress: " + getRemoteAddress() + "]";
	}
}
