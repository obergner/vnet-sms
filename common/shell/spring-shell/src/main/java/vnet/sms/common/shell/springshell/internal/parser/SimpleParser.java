package vnet.sms.common.shell.springshell.internal.parser;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Required;

import vnet.sms.common.shell.springshell.AbstractShell;
import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Converter;
import vnet.sms.common.shell.springshell.MethodTarget;
import vnet.sms.common.shell.springshell.ParseResult;
import vnet.sms.common.shell.springshell.Parser;
import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CliOption;
import vnet.sms.common.shell.springshell.command.CliOptionContext;
import vnet.sms.common.shell.springshell.command.CliSimpleParserContext;
import vnet.sms.common.shell.springshell.internal.NaturalOrderComparator;
import vnet.sms.common.shell.springshell.internal.commands.CommandsRegistry;
import vnet.sms.common.shell.springshell.internal.converters.ConvertersRegistry;
import vnet.sms.common.shell.springshell.internal.logging.HandlerUtils;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.CollectionUtils;
import vnet.sms.common.shell.springshell.internal.util.ExceptionUtils;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;

/**
 * Default implementation of {@link Parser}.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class SimpleParser implements Parser {

	// Constants
	private static final Logger	            LOGGER	   = HandlerUtils
	                                                           .getLogger(SimpleParser.class);

	private static final Comparator<Object>	COMPARATOR	= new NaturalOrderComparator<Object>();

	// Fields
	private final Object	                mutex	   = new Object();

	private ConvertersRegistry	            convertersRegistry;

	private CommandsRegistry	            commandsRegistry;

	@Required
	public void setConvertersRegistry(final ConvertersRegistry converters) {
		this.convertersRegistry = converters;
	}

	@Required
	public void setCommandRegistry(final CommandsRegistry commands) {
		this.commandsRegistry = commands;
	}

	/**
	 * get all options key.
	 * 
	 * @param cliOptions
	 * @param includeOptionalOptions
	 * @return options keys
	 */
	private List<List<String>> getOptionsKeys(
	        final Collection<CliOption> cliOptions,
	        final boolean includeOptionalOptions) {
		final List<List<String>> optionsKeys = new ArrayList<List<String>>();
		for (final CliOption option : cliOptions) {
			if (includeOptionalOptions) {
				final List<String> keys = new ArrayList<String>();
				keys.addAll(Arrays.asList(option.key()));
				optionsKeys.add(keys);
			} else if (option.mandatory()) {
				final List<String> keys = new ArrayList<String>();
				keys.addAll(Arrays.asList(option.key()));
				optionsKeys.add(keys);
			}
		}
		return optionsKeys;
	}

	@Override
	public ParseResult parse(final String rawInput) {
		synchronized (this.mutex) {
			Assert.notNull(rawInput, "Raw input required");
			final String input = normalise(rawInput);

			// Locate the applicable targets which match this buffer
			final Collection<MethodTarget> matchingTargets = this.commandsRegistry
			        .findMatchingCommands(input, true, true);
			if (matchingTargets.isEmpty()) {
				// Before we just give up, let's see if we can offer a more
				// informative message to the user
				// by seeing the command is simply unavailable at this point in
				// time
				CollectionUtils.populate(matchingTargets, this.commandsRegistry
				        .findMatchingCommands(input, true, false));
				if (matchingTargets.isEmpty()) {
					commandNotFound(LOGGER, input);
				} else {
					LOGGER.warning("Command '"
					        + input
					        + "' was found but is not currently available (type 'help' then ENTER to learn about this command)");
				}
				return null;
			}
			if (matchingTargets.size() > 1) {
				LOGGER.warning("Ambigious command '" + input
				        + "' (for assistance press "
				        + AbstractShell.completionKeys
				        + " or type \"hint\" then hit ENTER)");
				return null;
			}
			final MethodTarget methodTarget = matchingTargets.iterator().next();

			// Argument conversion time
			final Annotation[][] parameterAnnotations = methodTarget
			        .getMethod().getParameterAnnotations();
			if (parameterAnnotations.length == 0) {
				// No args
				return new ParseResult(methodTarget.getMethod(),
				        methodTarget.getTarget(), null);
			}

			// Oh well, we need to convert some arguments
			final List<Object> arguments = new ArrayList<Object>(methodTarget
			        .getMethod().getParameterTypes().length);

			// Attempt to parse
			Map<String, String> options = null;
			try {
				options = ParserUtils.tokenize(methodTarget
				        .getRemainingBuffer());
			} catch (final IllegalArgumentException e) {
				LOGGER.warning(ExceptionUtils.extractRootCause(e).getMessage());
				return null;
			}

			final Set<CliOption> cliOptions = getCliOptions(parameterAnnotations);
			for (final CliOption cliOption : cliOptions) {
				final Class<?> requiredType = methodTarget.getMethod()
				        .getParameterTypes()[arguments.size()];

				if (cliOption.systemProvided()) {
					Object result;
					if (SimpleParser.class.isAssignableFrom(requiredType)) {
						result = this;
					} else {
						LOGGER.warning("Parameter type '" + requiredType
						        + "' is not system provided");
						return null;
					}
					arguments.add(result);
					continue;
				}

				// Obtain the value the user specified, taking care to ensure
				// they only specified it via a single alias
				String value = null;
				String sourcedFrom = null;
				for (final String possibleKey : cliOption.key()) {
					if (options.containsKey(possibleKey)) {
						if (sourcedFrom != null) {
							LOGGER.warning("You cannot specify option '"
							        + possibleKey
							        + "' when you have also specified '"
							        + sourcedFrom + "' in the same command");
							return null;
						}
						sourcedFrom = possibleKey;
						value = options.get(possibleKey);
					}
				}

				// Ensure the user specified a value if the value is mandatory
				// or
				// key and value must appear in pair
				final boolean mandatory = StringUtils.isBlank(value)
				        && cliOption.mandatory();
				final boolean specifiedKey = StringUtils.isBlank(value)
				        && options.containsKey(sourcedFrom);
				boolean specifiedKeyWithoutValue = false;
				if (specifiedKey) {
					value = cliOption.specifiedDefaultValue();
					if ("__NULL__".equals(value)) {
						specifiedKeyWithoutValue = true;
					}
				}
				if (mandatory || specifiedKeyWithoutValue) {
					if ("".equals(cliOption.key()[0])) {
						final StringBuilder message = new StringBuilder(
						        "You should specify a default option ");
						if (cliOption.key().length > 1) {
							message.append("(otherwise known as option '")
							        .append(cliOption.key()[1]).append("') ");
						}
						message.append("for this command");
						LOGGER.warning(message.toString());
					} else {
						printHintMessage(cliOptions, options);
					}
					return null;
				}

				// Accept a default if the user specified the option, but didn't
				// provide a value
				if ("".equals(value)) {
					value = cliOption.specifiedDefaultValue();
				}

				// Accept a default if the user didn't specify the option at all
				if (value == null) {
					value = cliOption.unspecifiedDefaultValue();
				}

				// Special token that denotes a null value is sought (useful for
				// default values)
				if ("__NULL__".equals(value)) {
					if (requiredType.isPrimitive()) {
						LOGGER.warning("Nulls cannot be presented to primitive type "
						        + requiredType.getSimpleName()
						        + " for option '"
						        + StringUtils
						                .arrayToCommaDelimitedString(cliOption
						                        .key()) + "'");
						return null;
					}
					arguments.add(null);
					continue;
				}

				// Now we're ready to perform a conversion
				try {
					CliOptionContext
					        .setOptionContext(cliOption.optionContext());
					CliSimpleParserContext.setSimpleParserContext(this);
					Object result;
					Converter<?> c = null;
					for (final Converter<?> candidate : this.convertersRegistry) {
						if (candidate.supports(requiredType,
						        cliOption.optionContext())) {
							// Found a usable converter
							c = candidate;
							break;
						}
					}
					if (c == null) {
						throw new IllegalStateException(
						        "TODO: Add basic type conversion");
						// TODO Fall back to a normal SimpleTypeConverter and
						// attempt conversion
						// SimpleTypeConverter simpleTypeConverter = new
						// SimpleTypeConverter();
						// result =
						// simpleTypeConverter.convertIfNecessary(value,
						// requiredType, mp);
					}

					// Use the converter
					result = c.convertFromText(value, requiredType,
					        cliOption.optionContext());

					// If the option has been specified to be mandatory then the
					// result should never be null
					if ((result == null) && cliOption.mandatory()) {
						throw new IllegalStateException();
					}
					arguments.add(result);
				} catch (final RuntimeException e) {
					LOGGER.warning(e.getClass().getName()
					        + ": Failed to convert '"
					        + value
					        + "' to type "
					        + requiredType.getSimpleName()
					        + " for option '"
					        + StringUtils.arrayToCommaDelimitedString(cliOption
					                .key()) + "'");
					if (StringUtils.hasText(e.getMessage())) {
						LOGGER.warning(e.getMessage());
					}
					return null;
				} finally {
					CliOptionContext.resetOptionContext();
					CliSimpleParserContext.resetSimpleParserContext();
				}
			}

			// Check for options specified by the user but are unavailable for
			// the command
			final Set<String> unavailableOptions = getSpecifiedUnavailableOptions(
			        cliOptions, options);
			if (!unavailableOptions.isEmpty()) {
				final StringBuilder message = new StringBuilder();
				if (unavailableOptions.size() == 1) {
					message.append("Option '")
					        .append(unavailableOptions.iterator().next())
					        .append("' is not available for this command. ");
				} else {
					message.append("Options ")
					        .append(StringUtils.collectionToDelimitedString(
					                unavailableOptions, ", ", "'", "'"))
					        .append(" are not available for this command. ");
				}
				message.append("Use tab assist or the \"help\" command to see the legal options");
				LOGGER.warning(message.toString());
				return null;
			}

			return new ParseResult(methodTarget.getMethod(),
			        methodTarget.getTarget(), arguments.toArray());
		}
	}

	/**
	 * @param cliOptions
	 * @param options
	 */
	private void printHintMessage(final Set<CliOption> cliOptions,
	        final Map<String, String> options) {
		boolean hintForOptions = true;

		final StringBuilder optionBuilder = new StringBuilder();
		optionBuilder.append("You should specify option (");

		final StringBuilder valueBuilder = new StringBuilder();
		valueBuilder.append("You should specify value for option '");

		final List<List<String>> optionsKeys = getOptionsKeys(cliOptions, true);
		for (final List<String> keys : optionsKeys) {
			boolean found = false;
			for (final String key : keys) {
				if (options.containsKey(key)) {
					if (StringUtils.isBlank(options.get(key))) {
						valueBuilder.append(key);
						valueBuilder.append("' for this command");
						hintForOptions = false;
					}
					found = true;
					break;
				}
			}
			if (!found) {
				optionBuilder.append("--");
				optionBuilder.append(keys.get(0));
				optionBuilder.append(", ");
			}
		}
		// remove the ", " in the end.
		String hintForOption = optionBuilder.toString();
		hintForOption = hintForOption.substring(0, hintForOption.length() - 2);
		if (hintForOptions) {
			LOGGER.warning(hintForOption + ") for this command");
		} else {
			LOGGER.warning(valueBuilder.toString());
		}

	}

	/**
	 * Normalises the given raw user input string ready for parsing
	 * 
	 * @param rawInput
	 *            the string to normalise; can't be <code>null</code>
	 * @return a non-<code>null</code> string
	 */
	String normalise(final String rawInput) {
		// Replace all multiple spaces with a single space and then trim
		return rawInput.replaceAll(" +", " ").trim();
	}

	private Set<String> getSpecifiedUnavailableOptions(
	        final Set<CliOption> cliOptions, final Map<String, String> options) {
		final Set<String> cliOptionKeySet = new LinkedHashSet<String>();
		for (final CliOption cliOption : cliOptions) {
			for (final String key : cliOption.key()) {
				cliOptionKeySet.add(key.toLowerCase());
			}
		}
		final Set<String> unavailableOptions = new LinkedHashSet<String>();
		for (final String suppliedOption : options.keySet()) {
			if (!cliOptionKeySet.contains(suppliedOption.toLowerCase())) {
				unavailableOptions.add(suppliedOption);
			}
		}
		return unavailableOptions;
	}

	private Set<CliOption> getCliOptions(
	        final Annotation[][] parameterAnnotations) {
		final Set<CliOption> cliOptions = new LinkedHashSet<CliOption>();
		for (final Annotation[] annotations : parameterAnnotations) {
			for (final Annotation annotation : annotations) {
				if (annotation instanceof CliOption) {
					final CliOption cliOption = (CliOption) annotation;
					cliOptions.add(cliOption);
				}
			}
		}
		return cliOptions;
	}

	private void commandNotFound(final Logger logger, final String buffer) {
		logger.warning("Command '" + buffer
		        + "' not found (for assistance press "
		        + AbstractShell.completionKeys + ")");
	}

	@Override
	public int complete(final String buffer, final int cursor,
	        final List<String> candidates) {
		final List<Completion> completions = new ArrayList<Completion>();
		final int result = completeAdvanced(buffer, cursor, completions);
		for (final Completion completion : completions) {
			candidates.add(completion.getValue());
		}
		return result;
	}

	@Override
	public int completeAdvanced(String buffer, int cursor,
	        final List<Completion> candidates) {
		synchronized (this.mutex) {
			Assert.notNull(buffer, "Buffer required");
			Assert.notNull(candidates, "Candidates list required");

			// Remove all spaces from beginning of command
			while (buffer.startsWith(" ")) {
				buffer = buffer.replaceFirst("^ ", "");
				cursor--;
			}

			// Replace all multiple spaces with a single space
			while (buffer.contains("  ")) {
				buffer = StringUtils.replaceFirst(buffer, "  ", " ");
				cursor--;
			}

			// Begin by only including the portion of the buffer represented to
			// the present cursor position
			final String translated = buffer.substring(0, cursor);

			// Start by locating a method that matches
			final Collection<MethodTarget> targets = this.commandsRegistry
			        .findMatchingCommands(translated, false, true);
			final SortedSet<Completion> results = new TreeSet<Completion>(
			        COMPARATOR);

			if (targets.isEmpty()) {
				// Nothing matches the buffer they've presented
				return cursor;
			}
			if (targets.size() > 1) {
				// Assist them locate a particular target
				for (final MethodTarget target : targets) {
					// Calculate the correct starting position
					final int startAt = translated.length();

					// Only add the first word of each target
					int stopAt = target.getKey().indexOf(" ", startAt);
					if (stopAt == -1) {
						stopAt = target.getKey().length();
					}

					results.add(new Completion(target.getKey().substring(0,
					        stopAt)
					        + " "));
				}
				candidates.addAll(results);
				return 0;
			}

			// There is a single target of this method, so provide completion
			// services for it
			final MethodTarget methodTarget = targets.iterator().next();

			// Identify the command we're working with
			final CliCommand cmd = methodTarget.getMethod().getAnnotation(
			        CliCommand.class);
			Assert.notNull(cmd, "CliCommand unavailable for '"
			        + methodTarget.getMethod().toGenericString() + "'");

			// Make a reasonable attempt at parsing the remainingBuffer
			Map<String, String> options;
			try {
				options = ParserUtils.tokenize(methodTarget
				        .getRemainingBuffer());
			} catch (final IllegalArgumentException ex) {
				// Assume any IllegalArgumentException is due to a quotation
				// mark mismatch
				candidates.add(new Completion(translated + "\""));
				return 0;
			}

			// Lookup arguments for this target
			final Annotation[][] parameterAnnotations = methodTarget
			        .getMethod().getParameterAnnotations();

			// If there aren't any parameters for the method, at least ensure
			// they have typed the command properly
			if (parameterAnnotations.length == 0) {
				for (final String value : cmd.value()) {
					if (buffer.startsWith(value) || value.startsWith(buffer)) {
						results.add(new Completion(value)); // no space at the
						                                    // end, as there's
						                                    // no need to
						                                    // continue the
						                                    // command further
					}
				}
				candidates.addAll(results);
				return 0;
			}

			// If they haven't specified any parameters yet, at least verify the
			// command name is fully completed
			if (options.isEmpty()) {
				for (final String value : cmd.value()) {
					if (value.startsWith(buffer)) {
						// They are potentially trying to type this command
						// We only need provide completion, though, if they
						// failed to specify it fully
						if (!buffer.startsWith(value)) {
							// They failed to specify the command fully
							results.add(new Completion(value + " "));
						}
					}
				}

				// Only quit right now if they have to finish specifying the
				// command name
				if (results.size() > 0) {
					candidates.addAll(results);
					return 0;
				}
			}

			// To get this far, we know there are arguments required for this
			// CliCommand, and they specified a valid command name

			// Record all the CliOptions applicable to this command
			final List<CliOption> cliOptions = new ArrayList<CliOption>();
			for (final Annotation[] annotations : parameterAnnotations) {
				CliOption cliOption = null;
				for (final Annotation a : annotations) {
					if (a instanceof CliOption) {
						cliOption = (CliOption) a;
					}
				}
				Assert.notNull(cliOption, "CliOption not found for parameter '"
				        + Arrays.toString(annotations) + "'");
				cliOptions.add(cliOption);
			}

			// Make a list of all CliOptions they've already included or are
			// system-provided
			final List<CliOption> alreadySpecified = new ArrayList<CliOption>();
			for (final CliOption option : cliOptions) {
				for (final String value : option.key()) {
					if (options.containsKey(value)) {
						alreadySpecified.add(option);
						break;
					}
				}
				if (option.systemProvided()) {
					alreadySpecified.add(option);
				}
			}

			// Make a list of all CliOptions they have not provided
			final List<CliOption> unspecified = new ArrayList<CliOption>(
			        cliOptions);
			unspecified.removeAll(alreadySpecified);

			// Determine whether they're presently editing an option key or an
			// option value
			// (and if possible, the full or partial name of the said option key
			// being edited)
			String lastOptionKey = null;
			String lastOptionValue = null;

			// The last item in the options map is *always* the option key
			// they're editing (will never be null)
			if (options.size() > 0) {
				lastOptionKey = new ArrayList<String>(options.keySet())
				        .get(options.keySet().size() - 1);
				lastOptionValue = options.get(lastOptionKey);
			}

			// Handle if they are trying to find out the available option keys;
			// always present option keys in order
			// of their declaration on the method signature, thus we can stop
			// when mandatory options are filled in
			if (methodTarget.getRemainingBuffer().endsWith("--")) {
				boolean showAllRemaining = true;
				for (final CliOption include : unspecified) {
					if (include.mandatory()) {
						showAllRemaining = false;
						break;
					}
				}

				for (final CliOption include : unspecified) {
					for (final String value : include.key()) {
						if (!"".equals(value)) {
							results.add(new Completion(translated + value + " "));
						}
					}
					if (!showAllRemaining) {
						break;
					}
				}
				candidates.addAll(results);
				return 0;
			}

			// Handle suggesting an option key if they haven't got one presently
			// specified (or they've completed a full option key/value pair)
			if ((lastOptionKey == null)
			        || (!"".equals(lastOptionKey)
			                && !"".equals(lastOptionValue) && translated
			                .endsWith(" "))) {
				// We have either NEVER specified an option key/value pair
				// OR we have specified a full option key/value pair

				// Let's list some other options the user might want to try
				// (naturally skip the "" option, as that's the default)
				for (final CliOption include : unspecified) {
					for (final String value : include.key()) {
						// Manually determine if this non-mandatory but
						// unspecifiedDefaultValue=* requiring option is able to
						// be bound
						if (!include.mandatory()
						        && "*".equals(include.unspecifiedDefaultValue())
						        && !"".equals(value)) {
							try {
								for (final Converter<?> candidate : this.convertersRegistry) {
									// Find the target parameter
									Class<?> paramType = null;
									int index = -1;
									for (final Annotation[] a : methodTarget
									        .getMethod()
									        .getParameterAnnotations()) {
										index++;
										for (final Annotation an : a) {
											if (an instanceof CliOption) {
												if (an.equals(include)) {
													// Found the parameter, so
													// store it
													paramType = methodTarget
													        .getMethod()
													        .getParameterTypes()[index];
													break;
												}
											}
										}
									}
									if ((paramType != null)
									        && candidate.supports(paramType,
									                include.optionContext())) {
										// Try to invoke this usable converter
										candidate.convertFromText("*",
										        paramType,
										        include.optionContext());
										// If we got this far, the converter is
										// happy with "*" so we need not bother
										// the user with entering the data in
										// themselves
										break;
									}
								}
							} catch (final RuntimeException notYetReady) {
								if (translated.endsWith(" ")) {
									results.add(new Completion(translated
									        + "--" + value + " "));
								} else {
									results.add(new Completion(translated
									        + " --" + value + " "));
								}
								continue;
							}
						}

						// Handle normal mandatory options
						if (!"".equals(value) && include.mandatory()) {
							handleMandatoryCompletion(translated, unspecified,
							        value, results);
						}
					}
				}

				// Only abort at this point if we have some suggestions;
				// otherwise we might want to try to complete the "" option
				if (results.size() > 0) {
					candidates.addAll(results);
					return 0;
				}
			}

			// Handle completing the option key they're presently typing
			if (((lastOptionValue == null) || "".equals(lastOptionValue))
			        && !translated.endsWith(" ")) {
				// Given we haven't got an option value of any form, and there's
				// no space at the buffer end, we must still be typing an option
				// key
				// System.out.println("completing an option");
				for (final CliOption option : cliOptions) {
					for (final String value : option.key()) {
						if ((value != null)
						        && (lastOptionKey != null)
						        && value.regionMatches(true, 0, lastOptionKey,
						                0, lastOptionKey.length())) {
							final String completionValue = translated
							        .substring(
							                0,
							                (translated.length() - lastOptionKey
							                        .length()))
							        + value + " ";
							results.add(new Completion(completionValue));
						}
					}
				}
				candidates.addAll(results);
				return 0;
			}

			// To be here, we are NOT typing an option key (or we might be, and
			// there are no further option keys left)
			if ((lastOptionKey != null) && !"".equals(lastOptionKey)) {
				// Lookup the relevant CliOption that applies to this
				// lastOptionKey
				// We do this via the parameter type
				final Class<?>[] parameterTypes = methodTarget.getMethod()
				        .getParameterTypes();
				for (int i = 0; i < parameterTypes.length; i++) {
					final CliOption option = cliOptions.get(i);
					final Class<?> parameterType = parameterTypes[i];

					for (final String key : option.key()) {
						if (key.equals(lastOptionKey)) {
							final List<Completion> allValues = new ArrayList<Completion>();
							String suffix = " ";

							// Let's use a Converter if one is available
							for (final Converter<?> candidate : this.convertersRegistry) {
								if (candidate.supports(parameterType,
								        option.optionContext())) {
									// Found a usable converter
									final boolean addSpace = candidate
									        .getAllPossibleValues(allValues,
									                parameterType,
									                lastOptionValue,
									                option.optionContext(),
									                methodTarget);
									if (!addSpace) {
										suffix = "";
									}
									break;
								}
							}

							if (allValues.isEmpty()) {
								// Doesn't appear to be a custom Converter, so
								// let's go and provide defaults for simple
								// types

								// Provide some simple options for common types
								if (Boolean.class
								        .isAssignableFrom(parameterType)
								        || Boolean.TYPE
								                .isAssignableFrom(parameterType)) {
									allValues.add(new Completion("true"));
									allValues.add(new Completion("false"));
								}

								if (Number.class
								        .isAssignableFrom(parameterType)) {
									allValues.add(new Completion("0"));
									allValues.add(new Completion("1"));
									allValues.add(new Completion("2"));
									allValues.add(new Completion("3"));
									allValues.add(new Completion("4"));
									allValues.add(new Completion("5"));
									allValues.add(new Completion("6"));
									allValues.add(new Completion("7"));
									allValues.add(new Completion("8"));
									allValues.add(new Completion("9"));
								}
							}

							String prefix = "";
							if (!translated.endsWith(" ")) {
								prefix = " ";
							}

							// Only include in the candidates those results
							// which are compatible with the present buffer
							for (final Completion currentValue : allValues) {
								// We only provide a suggestion if the
								// lastOptionValue == ""
								if (StringUtils.isBlank(lastOptionValue)) {
									// We should add the result, as they haven't
									// typed anything yet
									results.add(new Completion(prefix
									        + currentValue.getValue() + suffix,
									        currentValue.getFormattedValue(),
									        currentValue.getHeading(),
									        currentValue.getOrder()));
								} else {
									// Only add the result **if** what they've
									// typed is compatible *AND* they haven't
									// already typed it in full
									if (currentValue
									        .getValue()
									        .toLowerCase()
									        .startsWith(
									                lastOptionValue
									                        .toLowerCase())
									        && !lastOptionValue
									                .equalsIgnoreCase(currentValue
									                        .getValue())
									        && (lastOptionValue.length() < currentValue
									                .getValue().length())) {
										results.add(new Completion(prefix
										        + currentValue.getValue()
										        + suffix, currentValue
										        .getFormattedValue(),
										        currentValue.getHeading(),
										        currentValue.getOrder()));
									}
								}
							}

							// ROO-389: give inline options given there's
							// multiple choices available and we want to help
							// the user
							final StringBuilder help = new StringBuilder();
							help.append(StringUtils.LINE_SEPARATOR);
							help.append(option.mandatory() ? "required --"
							        : "optional --");
							if ("".equals(option.help())) {
								help.append(lastOptionKey).append(": ")
								        .append("No help available");
							} else {
								help.append(lastOptionKey).append(": ")
								        .append(option.help());
							}
							if (option.specifiedDefaultValue().equals(
							        option.unspecifiedDefaultValue())) {
								if (option.specifiedDefaultValue().equals(
								        "__NULL__")) {
									help.append("; no default value");
								} else {
									help.append("; default: '")
									        .append(option
									                .specifiedDefaultValue())
									        .append("'");
								}
							} else {
								if (!"".equals(option.specifiedDefaultValue())
								        && !"__NULL__".equals(option
								                .specifiedDefaultValue())) {
									help.append(
									        "; default if option present: '")
									        .append(option
									                .specifiedDefaultValue())
									        .append("'");
								}
								if (!"".equals(option.unspecifiedDefaultValue())
								        && !"__NULL__".equals(option
								                .unspecifiedDefaultValue())) {
									help.append(
									        "; default if option not present: '")
									        .append(option
									                .unspecifiedDefaultValue())
									        .append("'");
								}
							}
							LOGGER.info(help.toString());

							if (results.size() == 1) {
								final String suggestion = results.iterator()
								        .next().getValue().trim();
								if (suggestion.equals(lastOptionValue)) {
									// They have pressed TAB in the default
									// value, and the default value has already
									// been provided as an explicit option
									return 0;
								}
							}

							if (results.size() > 0) {
								candidates.addAll(results);
								// Values presented from the last space onwards
								if (translated.endsWith(" ")) {
									return translated.lastIndexOf(" ") + 1;
								}
								return translated.trim().lastIndexOf(" ");
							}
							return 0;
						}
					}
				}
			}

			return 0;
		}
	}

	/**
	 * populate completion for mandatory options
	 * 
	 * @param translated
	 *            user's input
	 * @param unspecified
	 *            unspecified options
	 * @param value
	 *            the option key
	 * @param results
	 *            completion list
	 */
	private void handleMandatoryCompletion(final String translated,
	        final List<CliOption> unspecified, final String value,
	        final SortedSet<Completion> results) {
		final StringBuilder strBuilder = new StringBuilder(translated);
		if (!translated.endsWith(" ")) {
			strBuilder.append(" ");
		}
		strBuilder.append("--");
		strBuilder.append(value);
		strBuilder.append(" ");
		results.add(new Completion(strBuilder.toString()));
	}

	public Set<String> getEveryCommand() {
		return this.commandsRegistry.getAllCommandNames();
	}
}
