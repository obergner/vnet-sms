/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.channel.Channel;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HistogramMetric;

/**
 * @author obergner
 * 
 */
public class ChannelMonitor {

	public interface Callback {

		Callback	NULL	= new DefaultChannelMonitorCallback();

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

	private final HistogramMetric	      numberOfReceivedBytes;

	private final HistogramMetric	      numberOfReceivedLoginRequests;

	private final HistogramMetric	      numberOfReceivedLoginResponses;

	private final HistogramMetric	      numberOfReceivedPingRequests;

	private final HistogramMetric	      numberOfReceivedPingResponses;

	private final HistogramMetric	      numberOfReceivedSms;

	private final HistogramMetric	      numberOfAcceptedLoginRequests;

	private final HistogramMetric	      numberOfRejectedLoginRequests;

	private final HistogramMetric	      numberOfSentBytes;

	private final HistogramMetric	      numberOfSentPingRequests;

	private final HistogramMetric	      numberOfSentPingResponses;

	private final Channel	              channel;

	private final ChannelMonitor.Callback	listener;

	public ChannelMonitor(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");

		this.channel = channel;
		this.listener = this.new Listener();
		// Incoming metrics
		this.numberOfReceivedBytes = Metrics.newHistogram(Channel.class,
		        "received-bytes", channel.getId().toString());
		this.numberOfReceivedLoginRequests = Metrics.newHistogram(
		        Channel.class, "received-login-requests", channel.getId()
		                .toString());
		this.numberOfReceivedLoginResponses = Metrics.newHistogram(
		        Channel.class, "received-login-responses", channel.getId()
		                .toString());
		this.numberOfReceivedPingRequests = Metrics.newHistogram(Channel.class,
		        "received-ping-requests", channel.getId().toString());
		this.numberOfReceivedPingResponses = Metrics.newHistogram(
		        Channel.class, "received-ping-responses", channel.getId()
		                .toString());
		this.numberOfReceivedSms = Metrics.newHistogram(Channel.class,
		        "received-sms", channel.getId().toString());
		// Outgoing metrics
		this.numberOfSentBytes = Metrics.newHistogram(Channel.class,
		        "sent-bytes", channel.getId().toString());
		this.numberOfAcceptedLoginRequests = Metrics.newHistogram(
		        Channel.class, "accepted-login-requests", channel.getId()
		                .toString());
		this.numberOfRejectedLoginRequests = Metrics.newHistogram(
		        Channel.class, "rejected-login-requests", channel.getId()
		                .toString());
		this.numberOfSentPingRequests = Metrics.newHistogram(Channel.class,
		        "sent-ping-requests", channel.getId().toString());
		this.numberOfSentPingResponses = Metrics.newHistogram(Channel.class,
		        "sent-ping-responses", channel.getId().toString());
	}

	public ChannelMonitor.Callback getCallback() {
		return this.listener;
	}

	public HistogramMetric getNumberOfReceivedLoginRequests() {
		return this.numberOfReceivedLoginRequests;
	}

	public HistogramMetric getNumberOfReceivedLoginResponses() {
		return this.numberOfReceivedLoginResponses;
	}

	public HistogramMetric getNumberOfReceivedPingRequests() {
		return this.numberOfReceivedPingRequests;
	}

	public HistogramMetric getNumberOfReceivedPingResponses() {
		return this.numberOfReceivedPingResponses;
	}

	public HistogramMetric getNumberOfReceivedSms() {
		return this.numberOfReceivedSms;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.channel == null) ? 0 : this.channel.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ChannelMonitor other = (ChannelMonitor) obj;
		if (this.channel == null) {
			if (other.channel != null) {
				return false;
			}
		} else if (!this.channel.equals(other.channel)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ChannelMonitor@" + this.hashCode() + " [channel: "
		        + this.channel + "|numberOfSentBytes: "
		        + this.numberOfReceivedBytes + "|numberOfSentBytes: "
		        + this.numberOfReceivedLoginRequests
		        + "|numberOfReceivedLoginResponses: "
		        + this.numberOfReceivedLoginResponses
		        + "|numberOfReceivedPingRequests: "
		        + this.numberOfReceivedPingRequests
		        + "|numberOfReceivedPingResponses: "
		        + this.numberOfReceivedPingResponses + "|numberOfReceivedSms: "
		        + this.numberOfReceivedSms + "|numberOfAcceptedLoginRequests: "
		        + this.numberOfAcceptedLoginRequests
		        + "|numberOfRejectedLoginRequests: "
		        + this.numberOfRejectedLoginRequests
		        + "|numberOfSentPingRequests: " + this.numberOfSentPingRequests
		        + "|numberOfSentPingResponses: "
		        + this.numberOfSentPingResponses + "]";
	}

	private class Listener implements ChannelMonitor.Callback {

		@Override
		public void bytesReceived(final long numberOfBytes) {
			ChannelMonitor.this.numberOfReceivedBytes.update(numberOfBytes);
		}

		@Override
		public void pduReceived() {
			// TODO Auto-generated method stub
		}

		@Override
		public void loginRequestReceived() {
			ChannelMonitor.this.numberOfReceivedLoginRequests.update(1);
		}

		@Override
		public void loginResponseReceived() {
			ChannelMonitor.this.numberOfReceivedLoginResponses.update(1);
		}

		@Override
		public void pingRequestReceived() {
			ChannelMonitor.this.numberOfReceivedPingRequests.update(1);
		}

		@Override
		public void pingResponseReceived() {
			ChannelMonitor.this.numberOfReceivedPingResponses.update(1);
		}

		@Override
		public void smsReceived() {
			ChannelMonitor.this.numberOfReceivedSms.update(1);
		}

		@Override
		public void sendLoginRequestAccepted() {
			ChannelMonitor.this.numberOfAcceptedLoginRequests.update(1);
		}

		@Override
		public void sendLoginRequestRejected() {
			ChannelMonitor.this.numberOfRejectedLoginRequests.update(1);
		}

		@Override
		public void sendPingRequest() {
			ChannelMonitor.this.numberOfSentPingRequests.update(1);
		}

		@Override
		public void sendPingResponse() {
			ChannelMonitor.this.numberOfSentPingResponses.update(1);
		}

		@Override
		public void sendSms() {
			// TODO Auto-generated method stub
		}

		@Override
		public void sendPdu() {
			// TODO Auto-generated method stub
		}

		@Override
		public void sendBytes(final long numberOfBytes) {
			ChannelMonitor.this.numberOfSentBytes.update(numberOfBytes);
		}
	}
}
