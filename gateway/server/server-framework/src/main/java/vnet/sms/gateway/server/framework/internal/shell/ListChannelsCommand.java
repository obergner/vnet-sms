/**
 * 
 */
package vnet.sms.gateway.server.framework.internal.shell;

import static org.apache.commons.lang.Validate.notNull;
import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CommandMarker;
import vnet.sms.gateway.nettysupport.ChannelStatistics;
import vnet.sms.gateway.nettysupport.ChannelStatisticsGroup;

/**
 * @author obergner
 * 
 */
public class ListChannelsCommand implements CommandMarker {

	private final ChannelStatisticsGroup	allOpenChannels;

	public ListChannelsCommand(final ChannelStatisticsGroup allOpenChannels) {
		notNull(allOpenChannels, "Argument 'allOpenChannels' must not be null");
		this.allOpenChannels = allOpenChannels;
	}

	@CliCommand(value = { "list-channels", "list" }, help = "List all open channels")
	public String list() {
		final StringBuilder list = new StringBuilder();
		for (final ChannelStatistics channel : this.allOpenChannels) {
			list.append(String.format("%d %s %s %d %d%n", channel.getId()
			        .value(), channel.toString(), channel.getConnectedSince()
			        .value(), channel.getConnectTimeoutMillis().value(),
			        channel.getTotalNumberOfReceivedBytes().count()));
		}
		return list.toString();
	}
}
