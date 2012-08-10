/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import org.jboss.netty.channel.MessageEvent;

import com.google.common.base.Predicate;

/**
 * @author obergner
 * 
 */
public final class MessageEventFilters {

	public static <T extends MessageEvent> Predicate<MessageEvent> ofType(
	        final Class<T> type) {
		return new Predicate<MessageEvent>() {
			@Override
			public boolean apply(final MessageEvent event) {
				return type.isInstance(event);
			}
		};
	}

	public static Predicate<MessageEvent> payloadEquals(
	        final Object expectedPayload) {
		return new Predicate<MessageEvent>() {
			@Override
			public boolean apply(final MessageEvent event) {
				return ((expectedPayload == null) && (event.getMessage() == null))
				        || ((expectedPayload != null) && expectedPayload
				                .equals(event.getMessage()));
			}
		};
	}

	private MessageEventFilters() {
	}
}
