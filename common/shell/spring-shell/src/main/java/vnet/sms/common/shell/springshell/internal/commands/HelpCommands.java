package vnet.sms.common.shell.springshell.internal.commands;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Required;

import vnet.sms.common.shell.springshell.MethodTarget;
import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CliOption;
import vnet.sms.common.shell.springshell.command.CommandMarker;
import vnet.sms.common.shell.springshell.internal.NaturalOrderComparator;
import vnet.sms.common.shell.springshell.internal.logging.HandlerUtils;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;

/**
 * Provides a listing of commands known to the shell.
 * 
 * @author Ben Alex
 * @author Mark Pollack
 * @author Jarred Li
 * 
 */
public class HelpCommands implements CommandMarker {

	private static final Logger	            LOGGER	   = HandlerUtils
	                                                           .getLogger(HelpCommands.class);

	private static final Comparator<Object>	COMPARATOR	= new NaturalOrderComparator<Object>();

	private CommandsRegistry	            commandsRegistry;

	@CliCommand(value = "help", help = "list all commands usage")
	public void obtainHelp(
	        @CliOption(key = { "", "command" }, optionContext = "availableCommands", help = "Command name to provide help for") final String buffer) {
		doObtainHelp(buffer);
	}

	/**
	 * @param commandsRegistry
	 *            the commandsRegistry to set
	 */
	@Required
	public void setCommandsRegistry(final CommandsRegistry commandsRegistry) {
		this.commandsRegistry = commandsRegistry;
	}

	private void doObtainHelp(
	        @CliOption(key = { "", "command" }, optionContext = "availableCommands", help = "Command name to provide help for") String buffer) {
		if (buffer == null) {
			buffer = "";
		}

		final StringBuilder sb = new StringBuilder();

		// Figure out if there's a single command we can offer help for
		final Collection<MethodTarget> matchingTargets = this.commandsRegistry
		        .findMatchingCommands(buffer, false, false);
		if (matchingTargets.size() == 1) {
			// Single command help
			final MethodTarget methodTarget = matchingTargets.iterator().next();

			// Argument conversion time
			final Annotation[][] parameterAnnotations = methodTarget
			        .getMethod().getParameterAnnotations();
			if (parameterAnnotations.length > 0) {
				// Offer specified help
				final CliCommand cmd = methodTarget.getMethod().getAnnotation(
				        CliCommand.class);
				Assert.notNull(cmd, "CliCommand not found");

				for (final String value : cmd.value()) {
					sb.append("Keyword:                   ").append(value)
					        .append(StringUtils.LINE_SEPARATOR);
				}

				sb.append("Description:               ").append(cmd.help())
				        .append(StringUtils.LINE_SEPARATOR);

				for (final Annotation[] annotations : parameterAnnotations) {
					CliOption cliOption = null;
					for (final Annotation a : annotations) {
						if (a instanceof CliOption) {
							cliOption = (CliOption) a;

							for (String key : cliOption.key()) {
								if ("".equals(key)) {
									key = "** default **";
								}
								sb.append(" Keyword:                  ")
								        .append(key)
								        .append(StringUtils.LINE_SEPARATOR);
							}

							sb.append("   Help:                   ")
							        .append(cliOption.help())
							        .append(StringUtils.LINE_SEPARATOR);
							sb.append("   Mandatory:              ")
							        .append(cliOption.mandatory())
							        .append(StringUtils.LINE_SEPARATOR);
							sb.append("   Default if specified:   '")
							        .append(cliOption.specifiedDefaultValue())
							        .append("'")
							        .append(StringUtils.LINE_SEPARATOR);
							sb.append("   Default if unspecified: '")
							        .append(cliOption.unspecifiedDefaultValue())
							        .append("'")
							        .append(StringUtils.LINE_SEPARATOR);
							sb.append(StringUtils.LINE_SEPARATOR);
						}

					}
					Assert.notNull(
					        cliOption,
					        "CliOption not found for parameter '"
					                + Arrays.toString(annotations) + "'");
				}
			}
			// Only a single argument, so default to the normal help
			// operation
		}

		final SortedSet<String> result = new TreeSet<String>(COMPARATOR);
		for (final MethodTarget mt : matchingTargets) {
			final CliCommand cmd = mt.getMethod().getAnnotation(
			        CliCommand.class);
			if (cmd != null) {
				for (final String value : cmd.value()) {
					if ("".equals(cmd.help())) {
						result.add("* " + value);
					} else {
						result.add("* " + value + " - " + cmd.help());
					}
				}
			}
		}

		for (final String s : result) {
			sb.append(s).append(StringUtils.LINE_SEPARATOR);
		}

		LOGGER.info(sb.toString());
		LOGGER.warning("** Type 'hint' (without the quotes) and hit ENTER for step-by-step guidance **"
		        + StringUtils.LINE_SEPARATOR);
	}
}
