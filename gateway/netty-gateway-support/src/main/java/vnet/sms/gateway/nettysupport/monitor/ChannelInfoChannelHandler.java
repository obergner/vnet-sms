/**
 * 
 */
package vnet.sms.gateway.nettysupport.monitor;

import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;
import java.util.Date;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import vnet.sms.gateway.nettysupport.ChannelUtils;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * @author obergner
 * 
 */
public final class ChannelInfoChannelHandler extends
        SimpleChannelUpstreamHandler {

	public static final String	  NAME	= "vnet.sms.gateway:channel-info-handler";

	private final MetricsRegistry	metricsRegistry;

	private Gauge<Integer>	      id;

	private Gauge<Date>	          connectedSince;

	private Gauge<SocketAddress>	localAddress;

	private Gauge<SocketAddress>	remoteAddress;

	private Gauge<Integer>	      connectTimeoutMillis;

	public ChannelInfoChannelHandler(final MetricsRegistry metricsRegistry) {
		notNull(metricsRegistry, "Argument 'metricsRegistry' must not be null");
		this.metricsRegistry = metricsRegistry;
	}

	// ------------------------------------------------------------------------
	// Publish metrics
	// ------------------------------------------------------------------------

	/**
	 * @return the connectedSince
	 */
	public final Gauge<Integer> getId() {
		return this.id;
	}

	/**
	 * @return the connectedSince
	 */
	public final Gauge<Date> getConnectedSince() {
		return this.connectedSince;
	}

	/**
	 * @return the localAddress
	 */
	public final Gauge<SocketAddress> getLocalAddress() {
		return this.localAddress;
	}

	/**
	 * @return the remoteAddress
	 */
	public final Gauge<SocketAddress> getRemoteAddress() {
		return this.remoteAddress;
	}

	/**
	 * @return the remoteAddress
	 */
	public final Gauge<Integer> getConnectTimeoutMillis() {
		return this.connectTimeoutMillis;
	}

	// ------------------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------------------

	@Override
	public void channelConnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.id = this.metricsRegistry.newGauge(idMetricName(e.getChannel()),
		        new Gauge<Integer>() {
			        @Override
			        public Integer value() {
				        return e.getChannel().getId();
			        }
		        });

		final Date connectedSince = new Date();
		this.connectedSince = this.metricsRegistry.newGauge(
		        connectedSinceMetricName(e.getChannel()), new Gauge<Date>() {
			        @Override
			        public Date value() {
				        return connectedSince;
			        }
		        });
		this.localAddress = this.metricsRegistry.newGauge(
		        localAddressMetricName(e.getChannel()),
		        new Gauge<SocketAddress>() {
			        @Override
			        public SocketAddress value() {
				        return e.getChannel().getLocalAddress();
			        }
		        });
		this.remoteAddress = this.metricsRegistry.newGauge(
		        remoteAddressMetricName(e.getChannel()),
		        new Gauge<SocketAddress>() {
			        @Override
			        public SocketAddress value() {
				        return e.getChannel().getRemoteAddress();
			        }
		        });
		this.connectTimeoutMillis = this.metricsRegistry.newGauge(
		        connectTimeoutMillisMetricName(e.getChannel()),
		        new Gauge<Integer>() {
			        @Override
			        public Integer value() {
				        return e.getChannel().getConfig()
				                .getConnectTimeoutMillis();
			        }
		        });

		super.channelConnected(ctx, e);
	}

	private MetricName idMetricName(final Channel channel) {
		return new MetricName(Channel.class, "channel-id",
		        ChannelUtils.toString(channel));
	}

	private MetricName connectedSinceMetricName(final Channel channel) {
		return new MetricName(Channel.class, "connected-since",
		        ChannelUtils.toString(channel));
	}

	private MetricName localAddressMetricName(final Channel channel) {
		return new MetricName(Channel.class, "local-address",
		        ChannelUtils.toString(channel));
	}

	private MetricName remoteAddressMetricName(final Channel channel) {
		return new MetricName(Channel.class, "remote-address",
		        ChannelUtils.toString(channel));
	}

	private MetricName connectTimeoutMillisMetricName(final Channel channel) {
		return new MetricName(Channel.class, "connect-timeout-millis",
		        ChannelUtils.toString(channel));
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,
	        final ChannelStateEvent e) throws Exception {
		this.metricsRegistry.removeMetric(idMetricName(e.getChannel()));
		this.metricsRegistry.removeMetric(remoteAddressMetricName(e
		        .getChannel()));
		this.metricsRegistry
		        .removeMetric(localAddressMetricName(e.getChannel()));
		this.metricsRegistry.removeMetric(connectedSinceMetricName(e
		        .getChannel()));
		this.metricsRegistry.removeMetric(connectTimeoutMillisMetricName(e
		        .getChannel()));

		super.channelDisconnected(ctx, e);
	}
}
