/**
 * 
 */
package vnet.sms.common.wme;

import java.io.Serializable;

import org.jboss.netty.channel.MessageEvent;

import vnet.sms.common.messages.GsmPdu;

/**
 * @author obergner
 * 
 */
public interface WindowedMessageEvent<ID extends Serializable, M extends GsmPdu>
        extends MessageEvent {

	ID getMessageReference();

	MessageEventType getMessageType();

	@Override
	M getMessage();
}
