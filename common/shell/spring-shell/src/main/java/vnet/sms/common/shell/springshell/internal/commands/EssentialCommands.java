package vnet.sms.common.shell.springshell.internal.commands;

import vnet.sms.common.shell.springshell.ExitShellRequest;
import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CommandMarker;

public class EssentialCommands implements CommandMarker {

	@CliCommand(value = { "exit", "quit" }, help = "Exits the shell")
	public ExitShellRequest quit() {
		return ExitShellRequest.NORMAL_EXIT;
	}

}
