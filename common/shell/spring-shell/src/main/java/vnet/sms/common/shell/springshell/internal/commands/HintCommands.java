package vnet.sms.common.shell.springshell.internal.commands;

import org.springframework.beans.factory.annotation.Required;

import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CliOption;
import vnet.sms.common.shell.springshell.command.CommandMarker;

public class HintCommands implements CommandMarker {

	private HintOperations	hintOperations;

	/**
	 * @param hintOperations
	 *            the hintOperations to set
	 */
	@Required
	public void setHintOperations(final HintOperations hintOperations) {
		this.hintOperations = hintOperations;
	}

	@CliCommand(value = "hint", help = "provide context-sensitive hints to the user about what to do next")
	public String hint(
	        @CliOption(key = { "topic", "" }, mandatory = false, unspecifiedDefaultValue = "", optionContext = "disable-string-converter,topics", help = "The topic for which advice should be provided") final String topic) {
		return this.hintOperations.hint(topic);
	}
}
