/**
 * 
 */
package vnet.sms.gateway.nettysupport.publish.outgoing;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelFuture;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.acknowledge.MessageAcknowledgementContainer;
import vnet.sms.common.wme.acknowledge.SendSmsAckContainer;
import vnet.sms.common.wme.acknowledge.SendSmsNackContainer;
import vnet.sms.common.wme.send.SendSmsContainer;

/**
 * @author obergner
 * 
 */
public interface OutgoingMessagesSender<ID extends Serializable> {

	// ------------------------------------------------------------------------
	// Listener
	// ------------------------------------------------------------------------

	public interface Listener<ID extends Serializable> {

		void sendSmsFailed(final SendSmsContainer failedSms,
		        final Throwable error);

		void acknowldgeReceivedSmsFailed(
		        final MessageAcknowledgementContainer<ID, ? extends GsmPdu> acknowledgement,
		        final Throwable error);
	}

	// ------------------------------------------------------------------------
	// Managing listeners
	// ------------------------------------------------------------------------

	boolean addListener(Listener<ID> listener);

	boolean removeListener(Listener<ID> listener);

	void clearListeners();

	// ------------------------------------------------------------------------
	// Sending messages
	// ------------------------------------------------------------------------

	/**
	 * @param sms
	 * @return
	 * @throws Exception
	 */
	ChannelFuture sendSms(SendSmsContainer sms) throws Exception;

	/**
	 * @param ack
	 * @return
	 * @throws Exception
	 */
	ChannelFuture ackReceivedSms(SendSmsAckContainer<ID> ack) throws Exception;

	/**
	 * @param nack
	 * @return
	 * @throws Exception
	 */
	ChannelFuture nackReceivedSms(SendSmsNackContainer<ID> nack)
	        throws Exception;

	// ------------------------------------------------------------------------
	// Resource management
	// ------------------------------------------------------------------------

	/**
	 * 
	 */
	void close();
}
