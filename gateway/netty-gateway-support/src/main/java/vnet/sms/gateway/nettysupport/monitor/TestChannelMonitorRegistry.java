package vnet.sms.gateway.nettysupport.monitor;

import org.jboss.netty.channel.Channel;

public class TestChannelMonitorRegistry extends ChannelMonitorRegistry {

	private final ChannelMonitor.Callback	metricsListener;

	public TestChannelMonitorRegistry() {
		this(ChannelMonitor.Callback.NULL);
	}

	public TestChannelMonitorRegistry(
	        final ChannelMonitor.Callback metricsListener) {
		this.metricsListener = metricsListener;
	}

	@Override
	public ChannelMonitor.Callback registerChannel(final Channel channel) {
		return this.metricsListener;
	}
}
