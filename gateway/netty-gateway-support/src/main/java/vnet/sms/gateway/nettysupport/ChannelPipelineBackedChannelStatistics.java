/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;
import java.util.Date;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;

import vnet.sms.gateway.nettysupport.monitor.ChannelInfoChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.incoming.IncomingBytesCountingChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.incoming.IncomingMessagesMonitoringChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.incoming.IncomingPdusCountingChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.outgoing.OutgoingBytesCountingChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.outgoing.OutgoingMessagesMonitoringChannelHandler;
import vnet.sms.gateway.nettysupport.monitor.outgoing.OutgoingPdusCountingChannelHandler;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;

/**
 * @author obergner
 * 
 */
class ChannelPipelineBackedChannelStatistics implements ChannelStatistics {

	private final Channel	channel;

	ChannelPipelineBackedChannelStatistics(final Channel channel) {
		notNull(channel, "Argument 'channel' must not be null");
		this.channel = channel;
	}

	@Override
	public Gauge<Integer> getId() {
		return channelHandler(ChannelInfoChannelHandler.class).getId();
	}

	@Override
	public Gauge<Date> getConnectedSince() {
		return channelHandler(ChannelInfoChannelHandler.class)
		        .getConnectedSince();
	}

	@Override
	public Gauge<SocketAddress> getLocalAddress() {
		return channelHandler(ChannelInfoChannelHandler.class)
		        .getLocalAddress();
	}

	@Override
	public Gauge<SocketAddress> getRemoteAddress() {
		return channelHandler(ChannelInfoChannelHandler.class)
		        .getRemoteAddress();
	}

	@Override
	public Gauge<Integer> getConnectTimeoutMillis() {
		return channelHandler(ChannelInfoChannelHandler.class)
		        .getConnectTimeoutMillis();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfReceivedBytes()
	 */
	@Override
	public final Histogram getNumberOfReceivedBytes() {
		return channelHandler(IncomingBytesCountingChannelHandler.class)
		        .getNumberOfReceivedBytes();
	}

	private <T extends ChannelHandler> T channelHandler(
	        final Class<T> handlerType) {
		return this.channel.getPipeline().get(handlerType);
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getTotalNumberOfReceivedBytes()
	 */
	@Override
	public final Counter getTotalNumberOfReceivedBytes() {
		return channelHandler(IncomingBytesCountingChannelHandler.class)
		        .getTotalNumberOfReceivedBytes();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfReceivedPdus()
	 */
	@Override
	public final Meter getNumberOfReceivedPdus() {
		return channelHandler(IncomingPdusCountingChannelHandler.class)
		        .getNumberOfReceivedPdus();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfReceivedLoginRequests()
	 */
	@Override
	public final Meter getNumberOfReceivedLoginRequests() {
		return channelHandler(IncomingMessagesMonitoringChannelHandler.class)
		        .getNumberOfReceivedLoginRequests();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfReceivedLoginResponses()
	 */
	@Override
	public final Meter getNumberOfReceivedLoginResponses() {
		return channelHandler(IncomingMessagesMonitoringChannelHandler.class)
		        .getNumberOfReceivedLoginResponses();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfReceivedPingRequests()
	 */
	@Override
	public final Meter getNumberOfReceivedPingRequests() {
		return channelHandler(IncomingMessagesMonitoringChannelHandler.class)
		        .getNumberOfReceivedPingRequests();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfReceivedPingResponses()
	 */
	@Override
	public final Meter getNumberOfReceivedPingResponses() {
		return channelHandler(IncomingMessagesMonitoringChannelHandler.class)
		        .getNumberOfReceivedPingResponses();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfReceivedSms()
	 */
	@Override
	public final Meter getNumberOfReceivedSms() {
		return channelHandler(IncomingMessagesMonitoringChannelHandler.class)
		        .getNumberOfReceivedSms();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfAcceptedLoginRequests()
	 */
	@Override
	public final Meter getNumberOfAcceptedLoginRequests() {
		return channelHandler(OutgoingMessagesMonitoringChannelHandler.class)
		        .getNumberOfAcceptedLoginRequests();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfRejectedLoginRequests()
	 */
	@Override
	public final Meter getNumberOfRejectedLoginRequests() {
		return channelHandler(OutgoingMessagesMonitoringChannelHandler.class)
		        .getNumberOfRejectedLoginRequests();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfSentBytes()
	 */
	@Override
	public final Histogram getNumberOfSentBytes() {
		return channelHandler(OutgoingBytesCountingChannelHandler.class)
		        .getNumberOfSentBytes();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getTotalNumberOfSentBytes()
	 */
	@Override
	public final Counter getTotalNumberOfSentBytes() {
		return channelHandler(OutgoingBytesCountingChannelHandler.class)
		        .getTotalNumberOfSentBytes();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfSentPdus()
	 */
	@Override
	public final Meter getNumberOfSentPdus() {
		return channelHandler(OutgoingPdusCountingChannelHandler.class)
		        .getNumberOfSentPdus();
	}

	/**
	 * @see vnet.sms.gateway.nettysupport.ChannelStatistics#getNumberOfSentPingRequests()
	 */
	@Override
	public final Meter getNumberOfSentPingRequests() {
		return channelHandler(OutgoingMessagesMonitoringChannelHandler.class)
		        .getNumberOfSentPingRequests();
	}

	@Override
	public String toString() {
		return "ChannelPipelineBackedChannelStatistics@" + this.hashCode()
		        + "[channel: " + this.channel + "]";
	}
}
