/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.WindowedMessageEvent;
import vnet.sms.common.wme.receive.LoginRequestReceivedEvent;
import vnet.sms.common.wme.receive.LoginResponseReceivedEvent;
import vnet.sms.common.wme.receive.PingRequestReceivedEvent;
import vnet.sms.common.wme.receive.PingResponseReceivedEvent;
import vnet.sms.common.wme.receive.SmsReceivedEvent;

/**
 * @author obergner
 * 
 */
class UpstreamMessageEventToWindowedMessageEventConverter {

	static final UpstreamMessageEventToWindowedMessageEventConverter	INSTANCE	= new UpstreamMessageEventToWindowedMessageEventConverter();

	private UpstreamMessageEventToWindowedMessageEventConverter() {
		// Singleton
	}

	<ID extends Serializable> WindowedMessageEvent<ID, ? extends Message> convert(
	        final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final Message message) {
		notNull(messageReference,
		        "Argument 'messageReference' must not be null");
		notNull(upstreamMessageEvent,
		        "Cannot convert a null upstreamMessageEvent");
		notNull(message, "Cannot convert a null message");
		final WindowedMessageEvent<ID, ? extends Message> converted;
		if (message instanceof LoginRequest) {
			converted = new LoginRequestReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (LoginRequest) message);
		} else if (message instanceof LoginResponse) {
			converted = new LoginResponseReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (LoginResponse) message);
		} else if (message instanceof PingRequest) {
			converted = new PingRequestReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (PingRequest) message);
		} else if (message instanceof PingResponse) {
			converted = new PingResponseReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (PingResponse) message);
		} else if (message instanceof Sms) {
			converted = new SmsReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (Sms) message);
		} else {
			throw new IllegalArgumentException("Unsupported message type: "
			        + message.getClass());
		}

		return converted;
	}
}
