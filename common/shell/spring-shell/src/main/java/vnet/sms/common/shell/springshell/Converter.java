package vnet.sms.common.shell.springshell;

import java.util.List;

import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CliOption;

/**
 * Converts between Strings (as displayed by and entered via the shell) and Java
 * objects
 * 
 * @author Ben Alex
 * @param <T>
 *            the type being converted to/from
 */
public interface Converter<T> {

	/**
	 * Indicates whether this converter supports the given type in the given
	 * option context
	 * 
	 * @param type
	 *            the type being checked
	 * @param optionContext
	 *            a non-<code>null</code> string that customises the behaviour
	 *            of this converter for a given {@link CliOption} of a given
	 *            {@link CliCommand}; the contents will have special meaning to
	 *            this converter (e.g. be a comma-separated list of keywords
	 *            known to this converter)
	 * @return see above
	 */
	boolean supports(Class<?> type, String optionContext);

	/**
	 * Converts from the given String value to type T
	 * 
	 * @param value
	 *            the value to convert
	 * @param targetType
	 *            the type being converted to; can't be <code>null</code>
	 * @param optionContext
	 *            a non-<code>null</code> string that customises the behaviour
	 *            of this converter for a given {@link CliOption} of a given
	 *            {@link CliCommand}; the contents will have special meaning to
	 *            this converter (e.g. be a comma-separated list of keywords
	 *            known to this converter)
	 * @return see above
	 * @throws RuntimeException
	 *             if the given value could not be converted
	 */
	T convertFromText(String value, Class<?> targetType, String optionContext);

	/**
	 * Populates the given list with the possible completions
	 * 
	 * @param completions
	 *            the list to populate; can't be <code>null</code>
	 * @param targetType
	 *            the type of parameter for which a string is being entered
	 * @param existingData
	 *            what the user has typed so far
	 * @param optionContext
	 *            a non-<code>null</code> string that customises the behaviour
	 *            of this converter for a given {@link CliOption} of a given
	 *            {@link CliCommand}; the contents will have special meaning to
	 *            this converter (e.g. be a comma-separated list of keywords
	 *            known to this converter)
	 * @param target
	 * @return <code>true</code> if all the added completions are complete
	 *         values, or <code>false</code> if the user can press TAB to add
	 *         further information to some or all of them
	 */
	boolean getAllPossibleValues(List<Completion> completions,
	        Class<?> targetType, String existingData, String optionContext,
	        MethodTarget target);
}
