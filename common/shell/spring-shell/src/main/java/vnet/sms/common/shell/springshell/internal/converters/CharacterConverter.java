package vnet.sms.common.shell.springshell.internal.converters;

import java.util.List;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;

/**
 * {@link Converter} for {@link Character}.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
public class CharacterConverter implements Converter<Character> {

	@Override
	public Character convertFromText(final String value,
	        final Class<?> requiredType, final String optionContext) {
		return value.charAt(0);
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
		return Character.class.isAssignableFrom(requiredType)
		        || char.class.isAssignableFrom(requiredType);
	}
}
