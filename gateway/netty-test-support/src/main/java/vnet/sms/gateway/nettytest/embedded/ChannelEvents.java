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
public interface ChannelEvents extends Iterable<ChannelEvent> {

	boolean isEmpty();

	ChannelEvent nextChannelEvent();

	ChannelEvent nextMatchingChannelEvent(Predicate<ChannelEvent> predicate);

	ChannelEvent[] allChannelEvents();
}
