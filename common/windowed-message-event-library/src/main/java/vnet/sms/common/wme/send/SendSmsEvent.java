/**
 * 
 */
package vnet.sms.common.wme.send;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;

import vnet.sms.common.messages.Sms;

/**
 * @author obergner
 * 
 */
public final class SendSmsEvent extends DownstreamMessageEvent {

	public static final SendSmsEvent convert(
	        final MessageEvent sendSmsMessageEvent) {
		notNull(sendSmsMessageEvent,
		        "Argument 'sendSmsMessageEvent' must not be null");
		isTrue(sendSmsMessageEvent.getMessage() instanceof SendSmsContainer,
		        "Can only conver MessageEvents having a SendSmsContainer as their payload. Got: "
		                + sendSmsMessageEvent.getMessage());
		return new SendSmsEvent(sendSmsMessageEvent, SendSmsContainer.class
		        .cast(sendSmsMessageEvent.getMessage()).getMessage());
	}

	private SendSmsEvent(final MessageEvent sendSmsMessageEvent,
	        final Sms message) {
		super(sendSmsMessageEvent.getChannel(),
		        sendSmsMessageEvent.getFuture(), message, sendSmsMessageEvent
		                .getRemoteAddress());
	}

	@Override
	public Sms getMessage() {
		return (Sms) super.getMessage();
	}
}
