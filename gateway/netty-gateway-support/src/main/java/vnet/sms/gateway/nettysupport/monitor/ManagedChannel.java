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
public class ManagedChannel {

	private final HistogramMetric	numberOfReceivedBytes;

	private final HistogramMetric	numberOfReceivedPdus;

	private final HistogramMetric	numberOfReceivedLoginRequests;

	private final HistogramMetric	numberOfReceivedLoginResponses;

	private final HistogramMetric	numberOfReceivedPingRequests;

	private final HistogramMetric	numberOfReceivedPingResponses;

	private final HistogramMetric	numberOfReceivedSms;

	private final HistogramMetric	numberOfAcceptedLoginRequests;

	private final HistogramMetric	numberOfRejectedLoginRequests;

	private final HistogramMetric	numberOfSentBytes;

	private final HistogramMetric	numberOfSentPdus;

	private final HistogramMetric	numberOfSentPingRequests;

	private final HistogramMetric	numberOfSentPingResponses;

	private final Channel	      channel;

	private final ChannelMonitor	listener;

	public ManagedChannel(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");

		this.channel = channel;
		this.listener = this.new Listener();
		// Incoming metrics
		this.numberOfReceivedBytes = Metrics.newHistogram(Channel.class,
		        "received-bytes", channel.getId().toString());
		this.numberOfReceivedPdus = Metrics.newHistogram(Channel.class,
		        "received-pdus", channel.getId().toString());
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
		this.numberOfSentPdus = Metrics.newHistogram(Channel.class,
		        "sent-pdus", channel.getId().toString());
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

	public ChannelMonitor getMonitor() {
		return this.listener;
	}

	public HistogramMetric getNumberOfReceivedBytes() {
		return this.numberOfReceivedBytes;
	}

	public HistogramMetric getNumberOfReceivedPdus() {
		return this.numberOfReceivedPdus;
	}

	public HistogramMetric getNumberOfAcceptedLoginRequests() {
		return this.numberOfAcceptedLoginRequests;
	}

	public HistogramMetric getNumberOfRejectedLoginRequests() {
		return this.numberOfRejectedLoginRequests;
	}

	public HistogramMetric getNumberOfSentBytes() {
		return this.numberOfSentBytes;
	}

	public HistogramMetric getNumberOfSentPdus() {
		return this.numberOfSentPdus;
	}

	public HistogramMetric getNumberOfSentPingRequests() {
		return this.numberOfSentPingRequests;
	}

	public HistogramMetric getNumberOfSentPingResponses() {
		return this.numberOfSentPingResponses;
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
		final ManagedChannel other = (ManagedChannel) obj;
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
		return "ManagedChannel@" + hashCode() + "[channel: " + this.channel
		        + "|numberOfReceivedBytes: " + this.numberOfReceivedBytes
		        + "|numberOfReceivedPdus: " + this.numberOfReceivedPdus
		        + "|numberOfReceivedLoginRequests: "
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
		        + this.numberOfRejectedLoginRequests + "|numberOfSentBytes: "
		        + this.numberOfSentBytes + "|numberOfSentPdus: "
		        + this.numberOfSentPdus + "|numberOfSentPingRequests: "
		        + this.numberOfSentPingRequests
		        + "|numberOfSentPingResponses: "
		        + this.numberOfSentPingResponses + "]";
	}

	private class Listener implements ChannelMonitor {

		@Override
		public void bytesReceived(final long numberOfBytes) {
			ManagedChannel.this.numberOfReceivedBytes.update(numberOfBytes);
		}

		@Override
		public void pduReceived() {
			ManagedChannel.this.numberOfReceivedPdus.update(1);
		}

		@Override
		public void loginRequestReceived() {
			ManagedChannel.this.numberOfReceivedLoginRequests.update(1);
		}

		@Override
		public void loginResponseReceived() {
			ManagedChannel.this.numberOfReceivedLoginResponses.update(1);
		}

		@Override
		public void pingRequestReceived() {
			ManagedChannel.this.numberOfReceivedPingRequests.update(1);
		}

		@Override
		public void pingResponseReceived() {
			ManagedChannel.this.numberOfReceivedPingResponses.update(1);
		}

		@Override
		public void smsReceived() {
			ManagedChannel.this.numberOfReceivedSms.update(1);
		}

		@Override
		public void sendLoginRequestAccepted() {
			ManagedChannel.this.numberOfAcceptedLoginRequests.update(1);
		}

		@Override
		public void sendLoginRequestRejected() {
			ManagedChannel.this.numberOfRejectedLoginRequests.update(1);
		}

		@Override
		public void sendPingRequest() {
			ManagedChannel.this.numberOfSentPingRequests.update(1);
		}

		@Override
		public void sendPingResponse() {
			ManagedChannel.this.numberOfSentPingResponses.update(1);
		}

		@Override
		public void sendSms() {
			// TODO Auto-generated method stub
		}

		@Override
		public void sendPdu() {
			ManagedChannel.this.numberOfSentPdus.update(1);
		}

		@Override
		public void sendBytes(final long numberOfBytes) {
			ManagedChannel.this.numberOfSentBytes.update(numberOfBytes);
		}
	}
}
