/**
 * 
 */
package vnet.sms.gateway.server.framework.internal.shell;

import static org.apache.commons.lang.Validate.notNull;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;

import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CommandMarker;

/**
 * @author obergner
 * 
 */
public class ListChannelsCommand implements CommandMarker {

	private final ChannelGroup	allOpenChannels;

	public ListChannelsCommand(final ChannelGroup allOpenChannels) {
		notNull(allOpenChannels, "Argument 'allOpenChannels' must not be null");
		this.allOpenChannels = allOpenChannels;
	}

	@CliCommand(value = { "list-channels", "list" }, help = "List all open channels")
	public String list() {
		final StringBuilder list = new StringBuilder();
		for (final Channel channel : this.allOpenChannels) {
			list.append(String.format("%s%n", channel.toString()));
		}
		return list.toString();
	}
}
