package vnet.sms.common.shell.springshell;

import java.lang.reflect.Method;
import java.util.Arrays;

import vnet.sms.common.shell.springshell.internal.style.ToStringCreator;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;

/**
 * Immutable representation of the outcome of parsing a given shell line.
 * 
 * <p>
 * Note that contained objects (the instance and the arguments) may be mutable,
 * as the shell infrastructure has no way of restricting which methods can be
 * the target of CLI commands and nor the arguments they will accept via the
 * {@link Converter} infrastructure.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class ParseResult {

	// Fields
	private final Method	method;

	private final Object	instance;

	private final Object[]	arguments; // May be null if no arguments needed

	public ParseResult(final Method method, final Object instance,
	        final Object[] arguments) {
		Assert.notNull(method, "Method required");
		Assert.notNull(instance, "Instance required");
		final int length = arguments == null ? 0 : arguments.length;
		Assert.isTrue(method.getParameterTypes().length == length, "Required "
		        + method.getParameterTypes().length
		        + " arguments, but received " + length);
		this.method = method;
		this.instance = instance;
		this.arguments = arguments;
	}

	public Method getMethod() {
		return this.method;
	}

	public Object getInstance() {
		return this.instance;
	}

	public Object[] getArguments() {
		return this.arguments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.arguments);
		result = prime * result
		        + ((this.instance == null) ? 0 : this.instance.hashCode());
		result = prime * result
		        + ((this.method == null) ? 0 : this.method.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ParseResult other = (ParseResult) obj;
		if (!Arrays.equals(this.arguments, other.arguments)) {
			return false;
		}
		if (this.instance == null) {
			if (other.instance != null) {
				return false;
			}
		} else if (!this.instance.equals(other.instance)) {
			return false;
		}
		if (this.method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!this.method.equals(other.method)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final ToStringCreator tsc = new ToStringCreator(this);
		tsc.append("method", this.method);
		tsc.append("instance", this.instance);
		tsc.append("arguments",
		        StringUtils.arrayToCommaDelimitedString(this.arguments));
		return tsc.toString();
	}
}
