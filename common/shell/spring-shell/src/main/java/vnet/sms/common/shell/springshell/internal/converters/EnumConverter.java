package vnet.sms.common.shell.springshell.internal.converters;

import java.util.List;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;

/**
 * {@link Converter} for {@link Enum}.
 * 
 * @author Ben Alex
 * @author Alan Stewart
 * @since 1.0
 */
public class EnumConverter implements Converter<Enum> {

	@Override
	public Enum convertFromText(final String value,
	        final Class<?> requiredType, final String optionContext) {
		final Class<Enum> enumClass = (Class<Enum>) requiredType;
		return Enum.valueOf(enumClass, value);
	}

	@Override
	public boolean getAllPossibleValues(final List<Completion> completions,
	        final Class<?> requiredType, final String existingData,
	        final String optionContext, final MethodTarget target) {
		final Class<Enum> enumClass = (Class<Enum>) requiredType;
		for (final Enum enumValue : enumClass.getEnumConstants()) {
			final String candidate = enumValue.name();
			if ("".equals(existingData)
			        || candidate.startsWith(existingData)
			        || existingData.startsWith(candidate)
			        || candidate.toUpperCase().startsWith(
			                existingData.toUpperCase())
			        || existingData.toUpperCase().startsWith(
			                candidate.toUpperCase())) {
				completions.add(new Completion(candidate));
			}
		}
		return true;
	}

	@Override
	public boolean supports(final Class<?> requiredType,
	        final String optionContext) {
		return Enum.class.isAssignableFrom(requiredType);
	}
}
