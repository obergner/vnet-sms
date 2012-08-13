/**
 * 
 */
package vnet.sms.gateway.nettytest.embedded;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * @author obergner
 * 
 */
interface ChannelEventSink<T extends ChannelEvent> {

	boolean acceptsChannelEvent(T channelEvent);

	boolean acceptsExceptionEvent(ExceptionEvent e);
}
