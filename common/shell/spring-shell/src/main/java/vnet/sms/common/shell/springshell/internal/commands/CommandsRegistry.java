/**
 * 
 */
package vnet.sms.common.shell.springshell.internal.commands;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import vnet.sms.common.shell.springshell.MethodTarget;
import vnet.sms.common.shell.springshell.command.CliAvailabilityIndicator;
import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CommandMarker;
import vnet.sms.common.shell.springshell.internal.NaturalOrderComparator;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;

/**
 * @author obergner
 * 
 */
public class CommandsRegistry implements BeanPostProcessor {

	private static final Comparator<Object>	COMPARATOR	           = new NaturalOrderComparator<Object>();

	private final Logger	                log	                   = LoggerFactory
	                                                                       .getLogger(getClass());

	private final Set<CommandMarker>	    commands	           = new HashSet<CommandMarker>();

	private final Map<String, MethodTarget>	availabilityIndicators	= new HashMap<String, MethodTarget>();

	// ------------------------------------------------------------------------
	// API
	// ------------------------------------------------------------------------

	public Set<CommandMarker> getAllCommands() {
		return Collections.unmodifiableSet(this.commands);
	}

	public SortedSet<String> getAllCommandNames() {
		final SortedSet<String> result = new TreeSet<String>(COMPARATOR);
		for (final Object o : this.commands) {
			final Method[] methods = o.getClass().getMethods();
			for (final Method m : methods) {
				final CliCommand cmd = m.getAnnotation(CliCommand.class);
				if (cmd != null) {
					result.addAll(Arrays.asList(cmd.value()));
				}
			}
		}
		return result;
	}

	public Set<MethodTarget> findMatchingCommands(final String buffer,
	        final boolean strictMatching,
	        final boolean checkAvailabilityIndicators) {
		Assert.notNull(buffer, "Buffer required");
		final Set<MethodTarget> result = new HashSet<MethodTarget>();

		// The reflection could certainly be optimised, but it's good enough for
		// now (and cached reflection
		// is unlikely to be noticeable to a human being using the CLI)
		for (final CommandMarker command : this.commands) {
			for (final Method method : command.getClass().getMethods()) {
				final CliCommand cmd = method.getAnnotation(CliCommand.class);
				if (cmd != null) {
					// We have a @CliCommand.
					if (checkAvailabilityIndicators) {
						// Decide if this @CliCommand is available at this
						// moment
						Boolean available = null;
						for (final String value : cmd.value()) {
							final MethodTarget mt = getAvailabilityIndicator(value);
							if (mt != null) {
								Assert.isNull(available,
								        "More than one availability indicator is defined for '"
								                + method.toGenericString()
								                + "'");
								try {
									available = (Boolean) mt.getMethod()
									        .invoke(mt.getTarget());
									// We should "break" here, but we loop over
									// all to ensure no conflicting availability
									// indicators are defined
								} catch (final Exception e) {
									available = false;
								}
							}
						}
						// Skip this @CliCommand if it's not available
						if ((available != null) && !available) {
							continue;
						}
					}

					for (final String value : cmd.value()) {
						final String remainingBuffer = isMatch(buffer, value,
						        strictMatching);
						if (remainingBuffer != null) {
							result.add(new MethodTarget(method, command,
							        remainingBuffer, value));
						}
					}
				}
			}
		}
		return result;
	}

	public MethodTarget getAvailabilityIndicator(final String command) {
		return this.availabilityIndicators.get(command);
	}

	// ------------------------------------------------------------------------
	// BeanPostProcessor
	// ------------------------------------------------------------------------

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean,
	        final String beanName) throws BeansException {
		this.log.trace(
		        "Testing if bean [name = {} | bean = {}] implements [{}] ...",
		        new Object[] { beanName, bean, CommandMarker.class.getName() });
		if (bean instanceof CommandMarker) {
			this.log.info(
			        "Bean [name = {} | bean = {}] implements [{}] - will inject be added to set of known commands",
			        new Object[] { beanName, bean,
			                CommandMarker.class.getName() });
			add(CommandMarker.class.cast(bean));
		} else {
			this.log.trace(
			        "Bean [name = {} | bean = {}] does NOT implement [{}] - SKIPPING",
			        new Object[] { beanName, bean,
			                CommandMarker.class.getName() });
		}
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean,
	        final String beanName) throws BeansException {
		return bean;
	}

	// ------------------------------------------------------------------------
	// Internal
	// ------------------------------------------------------------------------

	private void add(final CommandMarker command) {
		this.commands.add(command);
		for (final Method method : command.getClass().getMethods()) {
			final CliAvailabilityIndicator availability = method
			        .getAnnotation(CliAvailabilityIndicator.class);
			if (availability != null) {
				Assert.isTrue(method.getParameterTypes().length == 0,
				        "CliAvailabilityIndicator is only legal for 0 parameter methods ("
				                + method.toGenericString() + ")");
				Assert.isTrue(method.getReturnType().equals(Boolean.TYPE),
				        "CliAvailabilityIndicator is only legal for primitive boolean return types ("
				                + method.toGenericString() + ")");
				for (final String cmd : availability.value()) {
					Assert.isTrue(
					        !this.availabilityIndicators.containsKey(cmd),
					        "Cannot specify an availability indicator for '"
					                + cmd + "' more than once");
					this.availabilityIndicators.put(cmd, new MethodTarget(
					        method, command));
				}
			}
		}
	}

	private String isMatch(final String buffer, final String command,
	        final boolean strictMatching) {
		if ("".equals(buffer.trim())) {
			return "";
		}
		final String[] commandWords = StringUtils.delimitedListToStringArray(
		        command, " ");
		int lastCommandWordUsed = 0;
		Assert.notEmpty(commandWords, "Command required");

		String bufferToReturn = null;
		String lastWord = null;

		next_buffer_loop: for (int bufferIndex = 0; bufferIndex < buffer
		        .length(); bufferIndex++) {
			final String bufferSoFarIncludingThis = buffer.substring(0,
			        bufferIndex + 1);
			final String bufferRemaining = buffer.substring(bufferIndex + 1);

			final int bufferLastIndexOfWord = bufferSoFarIncludingThis
			        .lastIndexOf(" ");
			String wordSoFarIncludingThis = bufferSoFarIncludingThis;
			if (bufferLastIndexOfWord != -1) {
				wordSoFarIncludingThis = bufferSoFarIncludingThis
				        .substring(bufferLastIndexOfWord);
			}

			if (wordSoFarIncludingThis.equals(" ")
			        || (bufferIndex == buffer.length() - 1)) {
				if ((bufferIndex == buffer.length() - 1)
				        && !"".equals(wordSoFarIncludingThis.trim())) {
					lastWord = wordSoFarIncludingThis.trim();
				}

				// At end of word or buffer. Let's see if a word matched or not
				for (int candidate = lastCommandWordUsed; candidate < commandWords.length; candidate++) {
					if ((lastWord != null) && (lastWord.length() > 0)
					        && commandWords[candidate].startsWith(lastWord)) {
						if (bufferToReturn == null) {
							// This is the first match, so ensure the intended
							// match really represents the start of a command
							// and not a later word within it
							if ((lastCommandWordUsed == 0) && (candidate > 0)) {
								// This is not a valid match
								break next_buffer_loop;
							}
						}

						if (bufferToReturn != null) {
							// We already matched something earlier, so ensure
							// we didn't skip any word
							if (candidate != lastCommandWordUsed + 1) {
								// User has skipped a word
								bufferToReturn = null;
								break next_buffer_loop;
							}
						}

						bufferToReturn = bufferRemaining;
						lastCommandWordUsed = candidate;
						if (candidate + 1 == commandWords.length) {
							// This was a match for the final word in the
							// command, so abort
							break next_buffer_loop;
						}
						// There are more words left to potentially match, so
						// continue
						continue next_buffer_loop;
					}
				}

				// This word is unrecognised as part of a command, so abort
				bufferToReturn = null;
				break next_buffer_loop;
			}

			lastWord = wordSoFarIncludingThis.trim();
		}

		// We only consider it a match if ALL words were actually used
		if (bufferToReturn != null) {
			if (!strictMatching
			        || (lastCommandWordUsed + 1 == commandWords.length)) {
				return bufferToReturn;
			}
		}

		return null; // Not a match
	}
}
