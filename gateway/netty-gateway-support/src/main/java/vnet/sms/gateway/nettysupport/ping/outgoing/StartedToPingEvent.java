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
public class StartedToPingEvent extends AbstractIdentifiableChannelEvent {

	private final int	pingIntervalSeconds;

	private final long	pingResponseTimeoutMillis;

	StartedToPingEvent(final Channel channel, final int pingIntervalSeconds,
	        final long pingResponseTimeoutMillis) {
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
		return "StartedToPingEvent@" + this.hashCode() + " [id: " + getId()
		        + "|channel: " + getChannel() + "|pingIntervalSeconds: "
		        + this.pingIntervalSeconds + "|pingResponseTimeoutMillis: "
		        + this.pingResponseTimeoutMillis + "|creationTimestamp: "
		        + getCreationTimestamp() + "]";
	}
}
