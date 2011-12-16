/**
 * 
 */
package vnet.sms.gateway.nettytest;

import org.jboss.netty.channel.ChannelEvent;

/**
 * @author obergner
 * 
 */
public interface ChannelEventFilter {

	Factory	FILTERS	= new Factory();

	boolean matches(final ChannelEvent event);

	public static final class Factory {

		public <T extends ChannelEvent> ChannelEventFilter ofType(
		        final Class<T> type) {
			return new ChannelEventFilter() {
				@Override
				public boolean matches(final ChannelEvent event) {
					return type.isInstance(event);
				}
			};
		}

		private Factory() {
		}
	}
}
