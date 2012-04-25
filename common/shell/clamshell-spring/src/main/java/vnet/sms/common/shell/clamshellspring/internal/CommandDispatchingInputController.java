/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.core.AnInputController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class CommandDispatchingInputController extends AnInputController {

	private final Logger	           log	     = LoggerFactory
	                                                     .getLogger(getClass());

	private final Map<String, Command>	commands	= new HashMap<String, Command>();

	/**
	 * Handles incoming command-line input. CmdController first splits the input
	 * and uses token[0] as the action name mapped to the Command.
	 * 
	 * @param ctx
	 *            the shell context.
	 */
	@Override
	public boolean handle(final Context ctx) {
		final String cmdLine = (String) ctx
		        .getValue(Context.KEY_COMMAND_LINE_INPUT);
		this.log.debug("Processing command line [{}] ...", cmdLine);

		if ((cmdLine == null) || cmdLine.trim().isEmpty()
		        || this.commands.isEmpty()) {
			this.log.warn("No command line given or no commands registered - will abort processing");
			return false;
		}
		// handle command line entry. NOTE: value can be null
		final String[] tokens = cmdLine.trim().split("\\s+");
		final Command cmd = this.commands.get(tokens[0]);
		if (cmd == null) {
			this.log.debug("Unknown command [{}] - abort processing", tokens[0]);
			ctx.getIoConsole()
			        .writeOutput(
			                String.format(
			                        "%nCommand [%s] is unknown. "
			                                + "Type help for a list of installed commands.%n%n",
			                        tokens[0]));

			return false;
		}
		if (tokens.length > 1) {
			final String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
			ctx.putValue(Context.KEY_COMMAND_LINE_ARGS, args);
		}
		cmd.execute(ctx);
		this.log.debug(
		        "Finished processing command line [{}] - using command [{}]",
		        cmdLine, cmd);

		return true;
	}

	/**
	 * Entry point for the plugin.
	 * 
	 * @param plug
	 */
	@Override
	public void plug(final Context plug) {
		super.plug(plug);
		final List<Command> allCommands = plug.getCommands();
		if (allCommands.size() > 0) {
			this.commands.putAll(plug.mapCommands(allCommands));
			this.log.info("Registered known commands: [{}]", this.commands);
			final Set<String> cmdHints = new TreeSet<String>();
			// plug each Command instance and collect input hints
			for (final Command cmd : allCommands) {
				cmd.plug(plug);
				cmdHints.addAll(collectInputHints(cmd));
			}
			// save expected command input hints
			setExpectedInputs(cmdHints.toArray(new String[0]));
		} else {
			plug.getIoConsole().writeOutput(
			        String.format(
			                "%nNo commands were found for input controller"
			                        + " [%s].%nn", this.getClass().getName()));
		}
	}
}
