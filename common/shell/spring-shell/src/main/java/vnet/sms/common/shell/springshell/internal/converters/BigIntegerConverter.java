package vnet.sms.common.shell.springshell.internal.converters;

import java.math.BigInteger;
import java.util.List;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;

/**
 * {@link Converter} for {@link BigInteger}.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
public class BigIntegerConverter implements Converter<BigInteger> {

	@Override
	public BigInteger convertFromText(final String value,
	        final Class<?> requiredType, final String optionContext) {
		return new BigInteger(value);
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
		return BigInteger.class.isAssignableFrom(requiredType);
	}
}
