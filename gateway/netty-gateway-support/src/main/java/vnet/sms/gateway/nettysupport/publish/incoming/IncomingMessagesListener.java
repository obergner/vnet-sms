package vnet.sms.gateway.nettysupport.publish.incoming;

import java.io.Serializable;

import vnet.sms.common.wme.LoginRequestReceivedEvent;
import vnet.sms.common.wme.LoginResponseReceivedEvent;
import vnet.sms.common.wme.PingRequestReceivedEvent;
import vnet.sms.common.wme.PingResponseReceivedEvent;
import vnet.sms.common.wme.SmsReceivedEvent;

/**
 * @author obergner
 * 
 * @param <ID>
 */
public interface IncomingMessagesListener<ID extends Serializable> {

	void smsReceived(SmsReceivedEvent<ID> smsReceived);

	void loginRequestReceived(LoginRequestReceivedEvent<ID> loginRequestReceived);

	void loginResponseReceived(
	        LoginResponseReceivedEvent<ID> loginResponseReceived);

	void pingRequestReceived(PingRequestReceivedEvent<ID> pingRequestReceived);

	void pingResponseReceived(PingResponseReceivedEvent<ID> pingResponseReceived);
}
