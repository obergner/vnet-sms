/**
 * 
 */
package vnet.sms.common.wme;

import java.io.Serializable;

import org.jboss.netty.channel.MessageEvent;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public interface WindowedMessageEvent<ID extends Serializable, M extends Message>
        extends MessageEvent {

	public enum Type {

		LOGIN_REQUEST_RECEIVED,

		LOGIN_REQUEST_ACCEPTED,

		LOGIN_REQUEST_REJECTED,

		LOGIN_RESPONSE_RECEIVED,

		PING_REQUEST_RECEIVED,

		PING_REQUEST_ACKNOWLEDGED,

		SEND_PING_REQUEST,

		PING_RESPONSE_RECEIVED,

		SMS_RECEIVED,

		NON_LOGIN_MESSAGE_RECEIVED_ON_UNAUTHENTICATED_CHANNEL,
	}

	ID getMessageReference();

	Type getType();

	@Override
	M getMessage();
}
