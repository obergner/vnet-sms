/**
 * 
 */
package vnet.sms.gateway.server.framework.internal.shell;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collections;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;

/**
 * @author obergner
 * 
 */
public class ListChannelsCommand implements Command {

	private final ChannelGroup	allOpenChannels;

	public ListChannelsCommand(final ChannelGroup allOpenChannels) {
		notNull(allOpenChannels, "Argument 'allOpenChannels' must not be null");
		this.allOpenChannels = allOpenChannels;
	}

	/**
	 * @see org.clamshellcli.api.Plugin#plug(org.clamshellcli.api.Context)
	 */
	@Override
	public void plug(final Context context) {
		// Noop
	}

	/**
	 * @see org.clamshellcli.api.Command#execute(org.clamshellcli.api.Context)
	 */
	@Override
	public Object execute(final Context context) {
		final IOConsole console = context.getIoConsole();
		for (final Channel channel : this.allOpenChannels) {
			console.writeOutput(String.format("%s%n", channel.toString()));
		}
		return null;
	}

	/**
	 * @see org.clamshellcli.api.Command#getDescriptor()
	 */
	@Override
	public Command.Descriptor getDescriptor() {
		return CmdDescriptor.INSTANCE;
	}

	private static final class CmdDescriptor implements Command.Descriptor {

		static final CmdDescriptor	INSTANCE	= new CmdDescriptor();

		@Override
		public Map<String, String> getArguments() {
			return Collections.emptyMap();
		}

		@Override
		public String getDescription() {
			return "List all connected channels";
		}

		@Override
		public String getName() {
			return "list-channels";
		}

		@Override
		public String getNamespace() {
			return "gatewaysrv";
		}

		@Override
		public String getUsage() {
			return "list-channels";
		}
	}
}
