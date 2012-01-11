/**
 * 
 */
package vnet.sms.gateway.nettysupport.ping.outgoing;

import org.jboss.netty.channel.Channel;

import vnet.sms.gateway.nettysupport.AbstractIdentifiableChannelEvent;

/**
 * @author obergner
 * 
 */
public final class PingResponseTimeoutExpiredEvent extends
        AbstractIdentifiableChannelEvent {

	private final int	pingIntervalSeconds;

	private final long	pingResponseTimeoutMillis;

	public PingResponseTimeoutExpiredEvent(final Channel channel,
	        final int pingIntervalSeconds, final long pingResponseTimeoutMillis) {
		super(channel);
		this.pingIntervalSeconds = pingIntervalSeconds;
		this.pingResponseTimeoutMillis = pingResponseTimeoutMillis;
	}

	public int getPingIntervalSeconds() {
		return this.pingIntervalSeconds;
	}

	public long getPingResponseTimeoutMillis() {
		return this.pingResponseTimeoutMillis;
	}

	@Override
	public String toString() {
		return "PingResponseTimeoutExpiredEvent@" + this.hashCode() + " [id: "
		        + getId() + "|channel: " + getChannel()
		        + "|pingIntervalSeconds: " + this.pingIntervalSeconds
		        + "|pingResponseTimeoutMillis: "
		        + this.pingResponseTimeoutMillis + "|creationTimestamp: "
		        + getCreationTimestamp() + "]";
	}
}
