package vnet.sms.common.shell.springshell.internal.converters;

import java.util.List;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;

/**
 * {@link Converter} for {@link Float}.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
public class FloatConverter implements Converter<Float> {

	@Override
	public Float convertFromText(final String value,
	        final Class<?> requiredType, final String optionContext) {
		return new Float(value);
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
		return Float.class.isAssignableFrom(requiredType)
		        || float.class.isAssignableFrom(requiredType);
	}
}
