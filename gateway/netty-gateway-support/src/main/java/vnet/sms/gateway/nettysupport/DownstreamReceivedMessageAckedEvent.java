/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.net.SocketAddress;

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
	        final Channel channel, final M message,
	        final SocketAddress remoteAddress,
	        final Acknowledgement acknowledgement) {
		this(messageReference, channel, Channels.future(channel, false),
		        message, remoteAddress, acknowledgement);
	}

	private DownstreamReceivedMessageAckedEvent(final ID messageReference,
	        final Channel channel, final ChannelFuture future,
	        final Object message, final SocketAddress remoteAddress,
	        final Acknowledgement acknowledgement) {
		super(messageReference, channel, future, message, remoteAddress);
		notNull(messageReference,
		        "Argument 'messageReference' must not be null");
		notNull(acknowledgement, "Argument 'acknowledgement' must not be null");
		notNull(channel, "Argument 'channel' must not be null");
		notNull(message, "Argument 'message' must not be null");
		this.acknowledgement = acknowledgement;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ReceivedMessageAckedEvent#getAcknowledgement
	 *      ()
	 */
	@Override
	public Acknowledgement getAcknowledgement() {
		return this.acknowledgement;
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ReceivedMessageAckedEvent#isAccepted()
	 */
	@Override
	public boolean isAccepted() {
		return this.acknowledgement.is(Acknowledgement.Status.ACK);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + this.hashCode()
		        + " [messageReference: " + getMessageReference() + "|message: "
		        + getMessage() + "|acknowledgement: " + this.acknowledgement
		        + "|channel: " + getChannel() + "|future: " + getFuture()
		        + "|remoteAddress: " + getRemoteAddress() + "]";
	}
}
