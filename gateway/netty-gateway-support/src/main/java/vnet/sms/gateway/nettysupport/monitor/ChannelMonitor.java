package vnet.sms.gateway.nettysupport.monitor;

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
}
