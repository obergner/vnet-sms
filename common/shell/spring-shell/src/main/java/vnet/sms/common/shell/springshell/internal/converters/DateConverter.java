package vnet.sms.common.shell.springshell.internal.converters;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;

/**
 * {@link Converter} for {@link Date}.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
public class DateConverter implements Converter<Date> {

	// Fields
	private final DateFormat	dateFormat;

	public DateConverter() {
		this.dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT,
		        Locale.getDefault());
	}

	public DateConverter(final DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Override
	public Date convertFromText(final String value,
	        final Class<?> requiredType, final String optionContext) {
		try {
			return this.dateFormat.parse(value);
		} catch (final ParseException e) {
			throw new IllegalArgumentException("Could not parse date: "
			        + e.getMessage());
		}
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
		return Date.class.isAssignableFrom(requiredType);
	}
}
