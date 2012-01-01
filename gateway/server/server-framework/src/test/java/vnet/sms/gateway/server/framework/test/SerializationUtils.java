package vnet.sms.gateway.server.framework.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.serialization.ObjectDecoderInputStream;
import org.jboss.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import vnet.sms.common.messages.Message;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public final class SerializationUtils {

	public static ChannelBuffer serialize(final int messageRef,
	        final Message message) throws IOException {
		final ByteArrayOutputStream serializedContainer = new ByteArrayOutputStream();
		final ObjectEncoderOutputStream objectEncoderOut = new ObjectEncoderOutputStream(
		        serializedContainer);
		objectEncoderOut.writeObject(ReferenceableMessageContainer.wrap(
		        messageRef, message));
		objectEncoderOut.flush();
		objectEncoderOut.close();

		return ChannelBuffers.copiedBuffer(serializedContainer.toByteArray());
	}

	public static Message deserialize(final MessageEvent messageEvent)
	        throws ClassNotFoundException, IOException {
		if (!(messageEvent.getMessage() instanceof ChannelBuffer)) {
			throw new IllegalArgumentException(
			        "Expected payload of type ChannelBuffer. Got: "
			                + messageEvent.getMessage());
		}
		final ChannelBuffer encodedMessage = ChannelBuffer.class
		        .cast(messageEvent.getMessage());
		final ByteArrayInputStream encodedMessageBytes = new ByteArrayInputStream(
		        encodedMessage.array());
		final ObjectDecoderInputStream objectDecoderIn = new ObjectDecoderInputStream(
		        encodedMessageBytes);
		final Object readObject = objectDecoderIn.readObject();
		if (!(readObject instanceof ReferenceableMessageContainer)) {
			throw new IllegalArgumentException("Expected message of type "
			        + ReferenceableMessageContainer.class.getName()
			        + " after deserialization. Got: " + readObject);
		}

		return Message.class.cast(ReferenceableMessageContainer.class.cast(
		        readObject).getMessage());
	}

	private SerializationUtils() {
	}
}
