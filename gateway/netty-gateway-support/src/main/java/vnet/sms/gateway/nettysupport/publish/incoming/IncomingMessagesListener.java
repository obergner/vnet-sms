package vnet.sms.gateway.nettysupport.publish.incoming;

import java.io.Serializable;

import vnet.sms.common.wme.receive.LoginRequestReceivedEvent;
import vnet.sms.common.wme.receive.LoginResponseReceivedEvent;
import vnet.sms.common.wme.receive.PingRequestReceivedEvent;
import vnet.sms.common.wme.receive.PingResponseReceivedEvent;
import vnet.sms.common.wme.receive.SmsReceivedEvent;

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
