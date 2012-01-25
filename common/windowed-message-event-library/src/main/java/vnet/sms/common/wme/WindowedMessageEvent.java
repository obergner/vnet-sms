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

	ID getMessageReference();

	MessageType getMessageType();

	@Override
	M getMessage();
}
