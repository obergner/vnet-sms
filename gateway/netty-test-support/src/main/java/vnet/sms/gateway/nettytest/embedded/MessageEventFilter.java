/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import org.jboss.netty.channel.MessageEvent;

/**
 * @author obergner
 * 
 */
public interface MessageEventFilter {

	Factory	FILTERS	= new Factory();

	boolean matches(final MessageEvent event);

	public static final class Factory {

		public <T extends MessageEvent> MessageEventFilter ofType(
		        final Class<T> type) {
			return new MessageEventFilter() {
				@Override
				public boolean matches(final MessageEvent event) {
					return type.isInstance(event);
				}
			};
		}

		public MessageEventFilter payloadEquals(final Object expectedPayload) {
			return new MessageEventFilter() {
				@Override
				public boolean matches(final MessageEvent event) {
					return ((expectedPayload == null) && (event.getMessage() == null))
					        || ((expectedPayload != null) && expectedPayload
					                .equals(event.getMessage()));
				}
			};
		}

		private Factory() {
		}
	}
}
