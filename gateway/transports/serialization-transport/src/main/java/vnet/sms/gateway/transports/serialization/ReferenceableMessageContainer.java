/**
 * 
 */
package vnet.sms.gateway.transports.serialization;

import static org.apache.commons.lang.Validate.notNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoderInputStream;
import org.jboss.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import vnet.sms.common.messages.GsmPdu;

/**
 * @author obergner
 * 
 */
public final class ReferenceableMessageContainer implements Serializable {

	private static final long	serialVersionUID	= -278633444989175439L;

	public static final ReferenceableMessageContainer wrap(
	        final int messageReference, final GsmPdu gsmPdu) {
		return new ReferenceableMessageContainer(messageReference, gsmPdu);
	}

	public static final ReferenceableMessageContainer decode(
	        final ChannelBuffer channelBuffer) {
		try {
			notNull(channelBuffer, "Argument 'channelBuffer' must not be null");
			final byte[] serializedForm = new byte[channelBuffer
			        .readableBytes()];
			channelBuffer.getBytes(0, serializedForm);
			final Object message = new ObjectDecoderInputStream(
			        new ByteArrayInputStream(serializedForm)).readObject();
			if (!ReferenceableMessageContainer.class.isInstance(message)) {
				return null;
			}
			return ReferenceableMessageContainer.class.cast(message);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to decode [" + channelBuffer
			        + "]: " + e.getMessage(), e);
		}
	}

	private final int	 messageReference;

	private final GsmPdu	gsmPdu;

	private ReferenceableMessageContainer(final int messageReference,
	        final GsmPdu gsmPdu) {
		notNull(gsmPdu, "Argument 'gsmPdu' must not be null");
		this.messageReference = messageReference;
		this.gsmPdu = gsmPdu;
	}

	public int getMessageReference() {
		return this.messageReference;
	}

	public GsmPdu getMessage() {
		return this.gsmPdu;
	}

	public <M extends GsmPdu> M getMessage(final Class<M> expectedType)
	        throws IllegalArgumentException {
		if (!expectedType.isInstance(this.gsmPdu)) {
			throw new IllegalArgumentException(
			        "Contained gsmPdu is not of expected type "
			                + expectedType.getName() + " but of type "
			                + this.gsmPdu.getClass().getName());
		}
		return expectedType.cast(this.gsmPdu);
	}

	public ChannelBuffer encode() {
		ObjectEncoderOutputStream oos = null;
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectEncoderOutputStream(baos);
			oos.writeObject(this);
			oos.flush();
			return ChannelBuffers.copiedBuffer(baos.toByteArray());
		} catch (final IOException e) {
			throw new RuntimeException("Failed to serialize [" + this + "]: "
			        + e.getMessage(), e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (final IOException e) {
					// Ignore
				}
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.gsmPdu == null) ? 0 : this.gsmPdu.hashCode());
		result = prime * result + this.messageReference;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReferenceableMessageContainer other = (ReferenceableMessageContainer) obj;
		if (this.gsmPdu == null) {
			if (other.gsmPdu != null) {
				return false;
			}
		} else if (!this.gsmPdu.equals(other.gsmPdu)) {
			return false;
		}
		if (this.messageReference != other.messageReference) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ReferenceableMessageContainer@" + hashCode()
		        + " [messageReference: " + this.messageReference + "|gsmPdu: "
		        + this.gsmPdu + "]";
	}
}
