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
interface ChannelListener<T extends ChannelEvent> {

	void onEvent(T e);

	void onExceptionEvent(ExceptionEvent e);
}
