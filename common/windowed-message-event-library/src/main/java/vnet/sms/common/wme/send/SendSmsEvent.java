/**
 * 
 */
package vnet.sms.common.wme.send;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;

import vnet.sms.common.messages.Sms;

/**
 * @author obergner
 * 
 */
public final class SendSmsEvent extends DownstreamMessageEvent {

	public SendSmsEvent(final Channel channel, final Sms message) {
		super(channel, Channels.future(channel, false), message, null);
	}
}
