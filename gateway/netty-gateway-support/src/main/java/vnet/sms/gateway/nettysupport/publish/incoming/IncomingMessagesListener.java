package vnet.sms.gateway.nettysupport.publish.incoming;

import java.io.Serializable;

import vnet.sms.common.wme.receive.ReceivedLoginRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedLoginRequestEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestAcknowledgementEvent;
import vnet.sms.common.wme.receive.ReceivedPingRequestEvent;
import vnet.sms.common.wme.receive.ReceivedSmsEvent;

/**
 * @author obergner
 * 
 * @param <ID>
 */
public interface IncomingMessagesListener<ID extends Serializable> {

	void smsReceived(ReceivedSmsEvent<ID> smsReceived);

	void loginRequestReceived(ReceivedLoginRequestEvent<ID> loginRequestReceived);

	void loginResponseReceived(
	        ReceivedLoginRequestAcknowledgementEvent<ID> loginResponseReceived);

	void pingRequestReceived(ReceivedPingRequestEvent<ID> pingRequestReceived);

	void pingResponseReceived(
	        ReceivedPingRequestAcknowledgementEvent<ID> pingResponseReceived);
}
