package vnet.sms.common.shell.springshell;

import java.lang.reflect.Method;

import vnet.sms.common.shell.springshell.internal.style.ToStringCreator;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.ObjectUtils;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;

/**
 * A method that can be executed via a shell command.
 * <p>
 * Immutable since 1.2.0.
 * 
 * @author Ben Alex
 */
public class MethodTarget {

	// Fields
	private final Method	method;

	private final Object	target;

	private final String	remainingBuffer;

	private final String	key;

	/**
	 * Constructor for a <code>null remainingBuffer</code> and <code>key</code>
	 * 
	 * @param method
	 *            the method to invoke (required)
	 * @param target
	 *            the object on which the method is to be invoked (required)
	 * @since 1.2.0
	 */
	public MethodTarget(final Method method, final Object target) {
		this(method, target, null, null);
	}

	/**
	 * Constructor that allows all fields to be set
	 * 
	 * @param method
	 *            the method to invoke (required)
	 * @param target
	 *            the object on which the method is to be invoked (required)
	 * @param remainingBuffer
	 *            can be blank
	 * @param key
	 *            can be blank
	 * @since 1.2.0
	 */
	public MethodTarget(final Method method, final Object target,
	        final String remainingBuffer, final String key) {
		Assert.notNull(method, "Method is required");
		Assert.notNull(target, "Target is required");
		this.key = StringUtils.trimToEmpty(key);
		this.method = method;
		this.remainingBuffer = StringUtils.trimToEmpty(remainingBuffer);
		this.target = target;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof MethodTarget)) {
			return false;
		}
		final MethodTarget otherMethodTarget = (MethodTarget) other;
		return this.method.equals(otherMethodTarget.getMethod())
		        && this.target.equals(otherMethodTarget.getTarget());
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.method, this.target);
	}

	@Override
	public final String toString() {
		final ToStringCreator tsc = new ToStringCreator(this);
		tsc.append("target", this.target);
		tsc.append("method", this.method);
		tsc.append("remainingBuffer", this.remainingBuffer);
		tsc.append("key", this.key);
		return tsc.toString();
	}

	/**
	 * @since 1.2.0
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * @return a non-<code>null</code> method
	 * @since 1.2.0
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * @since 1.2.0
	 */
	public String getRemainingBuffer() {
		return this.remainingBuffer;
	}

	/**
	 * @return a non-<code>null</code> Object
	 * @since 1.2.0
	 */
	public Object getTarget() {
		return this.target;
	}
}
