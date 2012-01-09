/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author obergner
 * 
 */
public class ChannelMonitors implements ChannelMonitor {

	private final Set<ChannelMonitor>	callbacks	= new CopyOnWriteArraySet<ChannelMonitor>();

	public void add(final ChannelMonitor callback) {
		this.callbacks.add(callback);
	}

	public void remove(final ChannelMonitor callback) {
		this.callbacks.remove(callback);
	}

	public void clear() {
		this.callbacks.clear();
	}

	@Override
	public void bytesReceived(final long numberOfBytes) {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.bytesReceived(numberOfBytes);
		}
	}

	@Override
	public void pduReceived() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.pduReceived();
		}
	}

	@Override
	public void loginRequestReceived() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.loginRequestReceived();
		}
	}

	@Override
	public void loginResponseReceived() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.loginResponseReceived();
		}
	}

	@Override
	public void pingRequestReceived() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.pingRequestReceived();
		}
	}

	@Override
	public void pingResponseReceived() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.pingResponseReceived();
		}
	}

	@Override
	public void smsReceived() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.smsReceived();
		}
	}

	@Override
	public void sendLoginRequestAccepted() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.sendLoginRequestAccepted();
		}
	}

	@Override
	public void sendLoginRequestRejected() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.sendLoginRequestRejected();
		}
	}

	@Override
	public void sendPingRequest() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.sendPingRequest();
		}
	}

	@Override
	public void sendPingResponse() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.sendPingResponse();
		}
	}

	@Override
	public void sendSms() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.sendSms();
		}
	}

	@Override
	public void sendPdu() {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.sendPdu();
		}
	}

	@Override
	public void sendBytes(final long numberOfBytes) {
		for (final ChannelMonitor cb : this.callbacks) {
			cb.sendBytes(numberOfBytes);
		}
	}
}
