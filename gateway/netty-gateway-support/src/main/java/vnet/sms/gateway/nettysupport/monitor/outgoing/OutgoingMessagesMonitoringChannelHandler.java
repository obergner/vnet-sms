/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.outgoing;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedLoginRequestNackedEvent;
import vnet.sms.common.wme.send.SendPingRequestEvent;
import vnet.sms.gateway.nettysupport.ChannelUtils;
import vnet.sms.gateway.nettysupport.WindowedChannelHandler;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class OutgoingMessagesMonitoringChannelHandler<ID extends Serializable>
        extends WindowedChannelHandler<ID> {

	public static final String	  NAME	= "vnet.sms.gateway:outgoing-messages-monitoring-handler";

	private final MetricsRegistry	metricsRegistry;

	private Meter	              numberOfAcceptedLoginRequests;

	private Meter	              numberOfRejectedLoginRequests;

	private Meter	              numberOfSentPingRequests;

	public OutgoingMessagesMonitoringChannelHandler(
	        final MetricsRegistry metricsRegistry) {
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.metricsRegistry = metricsRegistry;
	}

	/**
	 * @return the numberOfAcceptedLoginRequests
	 */
	public final Meter getNumberOfAcceptedLoginRequests() {
		return this.numberOfAcceptedLoginRequests;
	}

	/**
	 * @return the numberOfRejectedLoginRequests
	 */
	public final Meter getNumberOfRejectedLoginRequests() {
		return this.numberOfRejectedLoginRequests;
	}

	/**
	 * @return the numberOfSentPingRequests
	 */
	public final Meter getNumberOfSentPingRequests() {
		return this.numberOfSentPingRequests;
	}

	@Override
	protected void writePingRequestRequested(final ChannelHandlerContext ctx,
	        final SendPingRequestEvent<ID> e) throws Exception {
		this.numberOfSentPingRequests.mark();
		super.writePingRequestRequested(ctx, e);
	}

	@Override
	protected void writeLoginRequestAcceptedRequested(
	        final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestAckedEvent<ID> e) throws Exception {
		this.numberOfAcceptedLoginRequests.mark();
		super.writeLoginRequestAcceptedRequested(ctx, e);
	}

	@Override
	protected void writeLoginRequestRejectedRequested(
	        final ChannelHandlerContext ctx,
	        final ReceivedLoginRequestNackedEvent<ID> e) throws Exception {
		this.numberOfRejectedLoginRequests.mark();
		super.writeLoginRequestRejectedRequested(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Channel channel = ctx.getChannel();
		this.numberOfAcceptedLoginRequests = this.metricsRegistry.newMeter(
		        numberOfAcceptedLoginRequestsMetricName(channel),
		        "login-request-accepted", TimeUnit.SECONDS);
		this.numberOfRejectedLoginRequests = this.metricsRegistry.newMeter(
		        numberOfRejectedLoginRequestsMetricName(channel),
		        "login-request-rejected", TimeUnit.SECONDS);
		this.numberOfSentPingRequests = this.metricsRegistry.newMeter(
		        numberOfSentPingRequestsMetricName(channel),
		        "ping-request-sent", TimeUnit.SECONDS);

		super.channelOpen(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Channel channel = ctx.getChannel();
		this.metricsRegistry
		        .removeMetric(numberOfAcceptedLoginRequestsMetricName(channel));
		this.metricsRegistry
		        .removeMetric(numberOfRejectedLoginRequestsMetricName(channel));
		this.metricsRegistry
		        .removeMetric(numberOfSentPingRequestsMetricName(channel));

		super.channelClosed(ctx, e);
	}

	private MetricName numberOfAcceptedLoginRequestsMetricName(
	        final Channel channel) {
		return new MetricName(Channel.class, "accepted-login-requests",
		        ChannelUtils.toString(channel));
	}

	private MetricName numberOfSentPingRequestsMetricName(final Channel channel) {
		return new MetricName(Channel.class, "sent-ping-requests",
		        ChannelUtils.toString(channel));
	}

	private MetricName numberOfRejectedLoginRequestsMetricName(
	        final Channel channel) {
		return new MetricName(Channel.class, "rejected-login-requests",
		        ChannelUtils.toString(channel));
	}
}
