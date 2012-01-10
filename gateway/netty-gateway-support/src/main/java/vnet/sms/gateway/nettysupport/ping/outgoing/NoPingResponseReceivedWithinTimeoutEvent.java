/**
 * 
 */
package vnet.sms.gateway.nettysupport.ping.outgoing;

import java.util.Date;
import java.util.UUID;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

/**
 * @author obergner
 * 
 */
public final class NoPingResponseReceivedWithinTimeoutEvent implements
        ChannelEvent {

	private final UUID	        id	              = UUID.randomUUID();

	private final Channel	    channel;

	private final ChannelFuture	future;

	private final int	        pingIntervalSeconds;

	private final long	        pingResponseTimeoutMillis;

	private final long	        creationTimestamp	= System
	                                                      .currentTimeMillis();

	public NoPingResponseReceivedWithinTimeoutEvent(final Channel channel,
	        final int pingIntervalSeconds, final long pingResponseTimeoutMillis) {
		this.channel = channel;
		this.future = Channels.succeededFuture(channel);
		this.pingIntervalSeconds = pingIntervalSeconds;
		this.pingResponseTimeoutMillis = pingResponseTimeoutMillis;
	}

	public UUID getId() {
		return this.id;
	}

	public int getPingIntervalSeconds() {
		return this.pingIntervalSeconds;
	}

	public long getPingResponseTimeoutMillis() {
		return this.pingResponseTimeoutMillis;
	}

	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public Date getCreationDate() {
		return new Date(this.creationTimestamp);
	}

	@Override
	public Channel getChannel() {
		return this.channel;
	}

	@Override
	public ChannelFuture getFuture() {
		return this.future;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
		final NoPingResponseReceivedWithinTimeoutEvent other = (NoPingResponseReceivedWithinTimeoutEvent) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "NoPingResponseReceivedWithinTimeoutEvent@" + this.hashCode()
		        + " [id: " + this.id + "|channel: " + this.channel
		        + "|future: " + this.future + "|pingIntervalSeconds: "
		        + this.pingIntervalSeconds + "|pingResponseTimeoutMillis: "
		        + this.pingResponseTimeoutMillis + "|creationTimestamp: "
		        + this.creationTimestamp + "]";
	}
}
