package vnet.sms.common.shell.springshell.internal.converters;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;
import vnet.sms.common.shell.springshell.internal.commands.HintOperations;

/**
 * {@link Converter} for {@link String} that understands the "topics" option
 * context.
 * 
 * @author Ben Alex
 * @since 1.1
 */
public class HintConverter implements Converter<String> {

	private HintOperations	hintOperations;

	/**
	 * @param hintOperations
	 *            the hintOperations to set
	 */
	@Required
	public void setHintOperations(final HintOperations hintOperations) {
		this.hintOperations = hintOperations;
	}

	@Override
	public String convertFromText(final String value,
	        final Class<?> requiredType, final String optionContext) {
		return value;
	}

	@Override
	public boolean getAllPossibleValues(final List<Completion> completions,
	        final Class<?> requiredType, final String existingData,
	        final String optionContext, final MethodTarget target) {
		for (final String currentTopic : this.hintOperations.getCurrentTopics()) {
			completions.add(new Completion(currentTopic));
		}
		return false;
	}

	@Override
	public boolean supports(final Class<?> requiredType,
	        final String optionContext) {
		return String.class.isAssignableFrom(requiredType)
		        && optionContext.contains("topics");
	}
}
