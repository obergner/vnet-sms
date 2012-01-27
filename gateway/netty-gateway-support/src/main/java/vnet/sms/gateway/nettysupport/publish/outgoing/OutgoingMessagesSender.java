/**
 * 
 */
package vnet.sms.gateway.nettysupport.publish.outgoing;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelFuture;

import vnet.sms.common.wme.send.SendSmsContainer;

/**
 * @author obergner
 * 
 */
public interface OutgoingMessagesSender<ID extends Serializable> {

	// ------------------------------------------------------------------------
	// Listener
	// ------------------------------------------------------------------------

	public interface Listener {

		void sendSmsFailed(final SendSmsContainer failedSms,
		        final Throwable error);
	}

	// ------------------------------------------------------------------------
	// Managing listeners
	// ------------------------------------------------------------------------

	boolean addListener(Listener listener);

	boolean removeListener(Listener listener);

	void clearListeners();

	// ------------------------------------------------------------------------
	//
	// ------------------------------------------------------------------------

	/**
	 * @param sms
	 * @return
	 * @throws Exception
	 */
	ChannelFuture sendSms(SendSmsContainer sms) throws Exception;
}
