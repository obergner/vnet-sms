/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor.incoming;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

import vnet.sms.common.wme.receive.LoginRequestReceivedEvent;
import vnet.sms.common.wme.receive.LoginResponseReceivedEvent;
import vnet.sms.common.wme.receive.PingRequestReceivedEvent;
import vnet.sms.common.wme.receive.PingResponseReceivedEvent;
import vnet.sms.common.wme.receive.SmsReceivedEvent;
import vnet.sms.gateway.nettysupport.ChannelUtils;
import vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public class IncomingMessagesMonitoringChannelHandler<ID extends Serializable>
        extends UpstreamWindowedChannelHandler<ID> {

	public static final String	  NAME	= "vnet.sms.gateway:incoming-messages-monitoring-handler";

	private final MetricsRegistry	metricsRegistry;

	private Meter	              numberOfReceivedLoginRequests;

	private Meter	              numberOfReceivedLoginResponses;

	private Meter	              numberOfReceivedPingRequests;

	private Meter	              numberOfReceivedPingResponses;

	private Meter	              numberOfReceivedSms;

	public IncomingMessagesMonitoringChannelHandler(
	        final MetricsRegistry metricsRegistry) {
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.metricsRegistry = metricsRegistry;
	}

	/**
	 * @return the numberOfReceivedLoginRequests
	 */
	public final Meter getNumberOfReceivedLoginRequests() {
		return this.numberOfReceivedLoginRequests;
	}

	/**
	 * @return the numberOfReceivedLoginResponses
	 */
	public final Meter getNumberOfReceivedLoginResponses() {
		return this.numberOfReceivedLoginResponses;
	}

	/**
	 * @return the numberOfReceivedPingRequests
	 */
	public final Meter getNumberOfReceivedPingRequests() {
		return this.numberOfReceivedPingRequests;
	}

	/**
	 * @return the numberOfReceivedPingResponses
	 */
	public final Meter getNumberOfReceivedPingResponses() {
		return this.numberOfReceivedPingResponses;
	}

	/**
	 * @return the numberOfReceivedSms
	 */
	public final Meter getNumberOfReceivedSms() {
		return this.numberOfReceivedSms;
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.LoginRequestReceivedEvent)
	 */
	@Override
	protected void loginRequestReceived(final ChannelHandlerContext ctx,
	        final LoginRequestReceivedEvent<ID> e) throws Exception {
		this.numberOfReceivedLoginRequests.mark();
		super.loginRequestReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#loginResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.LoginResponseReceivedEvent)
	 */
	@Override
	protected void loginResponseReceived(final ChannelHandlerContext ctx,
	        final LoginResponseReceivedEvent<ID> e) throws Exception {
		this.numberOfReceivedLoginResponses.mark();
		super.loginResponseReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingRequestReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.PingRequestReceivedEvent)
	 */
	@Override
	protected void pingRequestReceived(final ChannelHandlerContext ctx,
	        final PingRequestReceivedEvent<ID> e) throws Exception {
		this.numberOfReceivedPingRequests.mark();
		super.pingRequestReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#pingResponseReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.PingResponseReceivedEvent)
	 */
	@Override
	protected void pingResponseReceived(final ChannelHandlerContext ctx,
	        final PingResponseReceivedEvent<ID> e) throws Exception {
		this.numberOfReceivedPingResponses.mark();
		super.pingResponseReceived(ctx, e);
	}

	/**
	 * @throws Exception
	 * @see vnet.sms.gateway.nettysupport.UpstreamWindowedChannelHandler#smsReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      vnet.sms.common.wme.receive.SmsReceivedEvent)
	 */
	@Override
	protected void smsReceived(final ChannelHandlerContext ctx,
	        final SmsReceivedEvent<ID> e) throws Exception {
		this.numberOfReceivedSms.mark();
		super.smsReceived(ctx, e);
	}

	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		final Channel channel = ctx.getChannel();
		this.numberOfReceivedLoginRequests = this.metricsRegistry.newMeter(
		        numberOfReceivedLoginRequestsMetricName(channel),
		        "login-request-received", TimeUnit.SECONDS);
		this.numberOfReceivedLoginResponses = this.metricsRegistry.newMeter(
		        numberOfReceivedLoginResponsesMetricName(channel),
		        "login-response-received", TimeUnit.SECONDS);
		this.numberOfReceivedPingRequests = this.metricsRegistry.newMeter(
		        numberOfReceivedPingRequestsMetricName(channel),
		        "pdu-received", TimeUnit.SECONDS);
		this.numberOfReceivedPingResponses = this.metricsRegistry.newMeter(
		        numberOfReceivedPingResponsesMetricName(channel),
		        "ping-response-received", TimeUnit.SECONDS);
		this.numberOfReceivedSms = this.metricsRegistry.newMeter(
		        numberOfReceivedSmsMetricName(channel), "pdu-received",
		        TimeUnit.SECONDS);

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
		        .removeMetric(numberOfReceivedLoginRequestsMetricName(channel));
		this.metricsRegistry
		        .removeMetric(numberOfReceivedLoginResponsesMetricName(channel));
		this.metricsRegistry
		        .removeMetric(numberOfReceivedPingRequestsMetricName(channel));
		this.metricsRegistry
		        .removeMetric(numberOfReceivedPingResponsesMetricName(channel));
		this.metricsRegistry
		        .removeMetric(numberOfReceivedSmsMetricName(channel));

		super.channelClosed(ctx, e);
	}

	private MetricName numberOfReceivedSmsMetricName(final Channel channel) {
		return new MetricName(Channel.class, "received-sms",
		        ChannelUtils.toString(channel));
	}

	private MetricName numberOfReceivedPingResponsesMetricName(
	        final Channel channel) {
		return new MetricName(Channel.class, "received-ping-responses",
		        ChannelUtils.toString(channel));
	}

	private MetricName numberOfReceivedPingRequestsMetricName(
	        final Channel channel) {
		return new MetricName(Channel.class, "received-ping-requests",
		        ChannelUtils.toString(channel));
	}

	private MetricName numberOfReceivedLoginResponsesMetricName(
	        final Channel channel) {
		return new MetricName(Channel.class, "received-login-responses",
		        ChannelUtils.toString(channel));
	}

	private MetricName numberOfReceivedLoginRequestsMetricName(
	        final Channel channel) {
		return new MetricName(Channel.class, "received-login-requests",
		        ChannelUtils.toString(channel));
	}
}
