package vnet.sms.gateway.nettysupport.publish.incoming;

import java.io.Serializable;

import vnet.sms.gateway.nettysupport.LoginRequestReceivedEvent;
import vnet.sms.gateway.nettysupport.LoginResponseReceivedEvent;
import vnet.sms.gateway.nettysupport.PingRequestReceivedEvent;
import vnet.sms.gateway.nettysupport.PingResponseReceivedEvent;
import vnet.sms.gateway.nettysupport.SmsReceivedEvent;

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
