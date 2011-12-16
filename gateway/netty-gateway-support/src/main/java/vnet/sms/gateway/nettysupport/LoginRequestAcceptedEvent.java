/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import java.io.Serializable;

import org.jboss.netty.channel.Channel;

import vnet.sms.common.messages.Acknowledgement;
import vnet.sms.common.messages.LoginRequest;

/**
 * @author obergner
 * 
 */
public class LoginRequestAcceptedEvent<ID extends Serializable> extends
        DownstreamReceivedMessageAckedEvent<ID, LoginRequest> {

	private LoginRequestAcceptedEvent(final ID messageReference,
	        final Channel channel, final LoginRequest message) {
		super(messageReference, channel, message, channel.getRemoteAddress(),
		        Acknowledgement.ack());
	}
}
