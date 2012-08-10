/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import org.jboss.netty.channel.ChannelEvent;

import com.google.common.base.Predicate;

/**
 * @author obergner
 * 
 */
public final class ChannelEventFilters {

	public static <T extends ChannelEvent> Predicate<ChannelEvent> ofType(
	        final Class<T> type) {
		return new Predicate<ChannelEvent>() {
			@Override
			public boolean apply(final ChannelEvent event) {
				return type.isInstance(event);
			}
		};
	}

	private ChannelEventFilters() {
	}
}
