/**
 * 
 */
package vnet.sms.gateway.nettysupport;

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

	@Override
	M getMessage();
}
