package vnet.sms.common.shell.springshell.internal.converters;

import java.util.List;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;

/**
 * {@link Converter} for {@link String}.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class StringConverter implements Converter<String> {

	@Override
	public String convertFromText(final String value,
	        final Class<?> requiredType, final String optionContext) {
		return value;
	}

	@Override
	public boolean getAllPossibleValues(final List<Completion> completions,
	        final Class<?> requiredType, final String existingData,
	        final String optionContext, final MethodTarget target) {
		return false;
	}

	@Override
	public boolean supports(final Class<?> requiredType,
	        final String optionContext) {
		return String.class.isAssignableFrom(requiredType)
		        && ((optionContext == null) || !optionContext
		                .contains("disable-string-converter"));
	}
}
