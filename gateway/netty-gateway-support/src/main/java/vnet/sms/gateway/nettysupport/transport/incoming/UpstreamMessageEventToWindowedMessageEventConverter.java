/**
 * 
 */
package vnet.sms.gateway.nettysupport.transport.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.channel.UpstreamMessageEvent;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.GsmPdu;
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

	<ID extends Serializable> WindowedMessageEvent<ID, ? extends GsmPdu> convert(
	        final ID messageReference,
	        final UpstreamMessageEvent upstreamMessageEvent,
	        final GsmPdu gsmPdu) {
		notNull(messageReference,
		        "Argument 'messageReference' must not be null");
		notNull(upstreamMessageEvent,
		        "Cannot convert a null upstreamMessageEvent");
		notNull(gsmPdu, "Cannot convert a null message");
		final WindowedMessageEvent<ID, ? extends GsmPdu> converted;
		if (gsmPdu instanceof LoginRequest) {
			converted = new LoginRequestReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (LoginRequest) gsmPdu);
		} else if (gsmPdu instanceof LoginResponse) {
			converted = new LoginResponseReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (LoginResponse) gsmPdu);
		} else if (gsmPdu instanceof PingRequest) {
			converted = new PingRequestReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (PingRequest) gsmPdu);
		} else if (gsmPdu instanceof PingResponse) {
			converted = new PingResponseReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (PingResponse) gsmPdu);
		} else if (gsmPdu instanceof Sms) {
			converted = new SmsReceivedEvent<ID>(messageReference,
			        upstreamMessageEvent, (Sms) gsmPdu);
		} else {
			throw new IllegalArgumentException("Unsupported message type: "
			        + gsmPdu.getClass());
		}

		return converted;
	}
}
