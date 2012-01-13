/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import vnet.sms.gateway.nettysupport.login.incoming.ChannelAuthenticationFailedEvent;
import vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.PingResponseTimeoutExpiredEvent;
import vnet.sms.gateway.nettysupport.ping.outgoing.StartedToPingEvent;
import vnet.sms.gateway.nettysupport.window.NoWindowForIncomingMessageAvailableEvent;
import vnet.sms.gateway.nettysupport.window.PendingWindowedMessagesDiscardedEvent;

/**
 * @author obergner
 * 
 */
public class DefaultChannelMonitor implements ChannelMonitor {

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#bytesReceived(long)
	 */
	@Override
	public void bytesReceived(final long numberOfBytes) {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#pduReceived()
	 */
	@Override
	public void pduReceived() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#loginRequestReceived()
	 */
	@Override
	public void loginRequestReceived() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#loginResponseReceived()
	 */
	@Override
	public void loginResponseReceived() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#pingRequestReceived()
	 */
	@Override
	public void pingRequestReceived() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#pingResponseReceived()
	 */
	@Override
	public void pingResponseReceived() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#smsReceived()
	 */
	@Override
	public void smsReceived() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#sendLoginRequestAccepted()
	 */
	@Override
	public void sendLoginRequestAccepted() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#sendLoginRequestRejected()
	 */
	@Override
	public void sendLoginRequestRejected() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#sendPingRequest()
	 */
	@Override
	public void sendPingRequest() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#sendPingResponse()
	 */
	@Override
	public void sendPingResponse() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#sendSms()
	 */
	@Override
	public void sendSms() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#sendPdu()
	 */
	@Override
	public void sendPdu() {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitorCallback#sendBytes(long)
	 */
	@Override
	public void sendBytes(final long numberOfBytes) {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitor#channelAuthenticated(vnet.sms.gateway.nettysupport.login.incoming.ChannelSuccessfullyAuthenticatedEvent)
	 */
	@Override
	public void channelAuthenticated(
	        final ChannelSuccessfullyAuthenticatedEvent e) {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitor#channelAuthenticationFailed(vnet.sms.gateway.nettysupport.login.incoming.ChannelAuthenticationFailedEvent)
	 */
	@Override
	public void channelAuthenticationFailed(
	        final ChannelAuthenticationFailedEvent e) {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitor#startedToPing(vnet.sms.gateway.nettysupport.ping.outgoing.StartedToPingEvent)
	 */
	@Override
	public void startedToPing(final StartedToPingEvent e) {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitor#pingResponseTimeoutExpired(vnet.sms.gateway.nettysupport.ping.outgoing.PingResponseTimeoutExpiredEvent)
	 */
	@Override
	public void pingResponseTimeoutExpired(
	        final PingResponseTimeoutExpiredEvent e) {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitor#noWindowForIncomingMessageAvailable(vnet.sms.gateway.nettysupport.window.NoWindowForIncomingMessageAvailableEvent)
	 */
	@Override
	public void noWindowForIncomingMessageAvailable(
	        final NoWindowForIncomingMessageAvailableEvent e) {
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.monitor.ChannelMonitor#pendingWindowedMessagesDiscarded(vnet.sms.gateway.nettysupport.window.PendingWindowedMessagesDiscardedEvent)
	 */
	@Override
	public void pendingWindowedMessagesDiscarded(
	        final PendingWindowedMessagesDiscardedEvent<?> e) {
	}
}
