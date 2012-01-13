package vnet.sms.gateway.nettysupport.monitor;

import vnet.sms.gateway.nettysupport.login.incoming.ChannelAuthenticationFailedEvent;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.PingResponseTimeoutExpiredEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.StartedToPingEvent;
import vnet.sms.gateway.nettysupport.window.NoWindowForIncomingMessageAvailableEvent;
import vnet.sms.gateway.nettysupport.window.PendingWindowedMessagesDiscardedEvent;

public interface ChannelMonitor {

	ChannelMonitor	NULL	= new DefaultChannelMonitor();

	void bytesReceived(long numberOfBytes);

	void pduReceived();

	void loginRequestReceived();

	void loginResponseReceived();

	void pingRequestReceived();

	void pingResponseReceived();

	void smsReceived();

	void sendLoginRequestAccepted();

	void sendLoginRequestRejected();

	void sendPingRequest();

	void sendPingResponse();

	void sendSms();

	void sendPdu();

	void sendBytes(long numberOfBytes);

	void channelAuthenticated(ChannelSuccessfullyAuthenticatedEvent e);

	void channelAuthenticationFailed(ChannelAuthenticationFailedEvent e);

	void startedToPing(StartedToPingEvent e);

	void pingResponseTimeoutExpired(PingResponseTimeoutExpiredEvent e);

	void noWindowForIncomingMessageAvailable(
	        NoWindowForIncomingMessageAvailableEvent e);

	void pendingWindowedMessagesDiscarded(
	        PendingWindowedMessagesDiscardedEvent<?> e);
}
