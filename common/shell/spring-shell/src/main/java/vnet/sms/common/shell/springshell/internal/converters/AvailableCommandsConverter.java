package vnet.sms.common.shell.springshell.internal.converters;

import java.util.List;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;
import vnet.sms.common.shell.springshell.internal.parser.SimpleParser;

/**
 * Available commands converter.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class AvailableCommandsConverter implements Converter<String> {

	@Override
	public String convertFromText(final String text,
	        final Class<?> requiredType, final String optionContext) {
		return text;
	}

	@Override
	public boolean supports(final Class<?> requiredType,
	        final String optionContext) {
		return String.class.isAssignableFrom(requiredType)
		        && "availableCommands".equals(optionContext);
	}

	@Override
	public boolean getAllPossibleValues(final List<Completion> completions,
	        final Class<?> requiredType, final String existingData,
	        final String optionContext, final MethodTarget target) {
		if (target.getTarget() instanceof SimpleParser) {
			final SimpleParser cmd = (SimpleParser) target.getTarget();

			// Only include the first word of each command
			for (final String s : cmd.getEveryCommand()) {
				if (s.contains(" ")) {
					completions.add(new Completion(s.substring(0,
					        s.indexOf(" "))));
				} else {
					completions.add(new Completion(s));
				}
			}
		}
		return true;
	}
}
