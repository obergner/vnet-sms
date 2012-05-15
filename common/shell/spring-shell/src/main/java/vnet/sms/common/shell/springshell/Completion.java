package vnet.sms.common.shell.springshell;

import vnet.sms.common.shell.springshell.internal.util.AnsiEscapeCode;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;

public class Completion {

	// Fields
	private final int	 order;

	private final String	formattedValue;

	private final String	heading;

	private final String	value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	public Completion(final String value) {
		this(value, value, null, 0);
	}

	/**
	 * Constructor
	 * 
	 * @param value
	 * @param formattedValue
	 * @param heading
	 * @param order
	 */
	public Completion(final String value, final String formattedValue,
	        final String heading, final int order) {
		this.formattedValue = formattedValue;
		this.order = order;
		this.value = value;
		this.heading = StringUtils.hasText(heading) ? AnsiEscapeCode.decorate(
		        heading, AnsiEscapeCode.UNDERSCORE, AnsiEscapeCode.FG_GREEN)
		        : heading;
	}

	public String getValue() {
		return this.value;
	}

	public String getFormattedValue() {
		return this.formattedValue;
	}

	public String getHeading() {
		return this.heading;
	}

	public int getOrder() {
		return this.order;
	}

	@Override
	public String toString() {
		return this.order + ". " + this.heading + " - " + this.value;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}

		final Completion that = (Completion) o;
		if (this.formattedValue != null ? !this.formattedValue
		        .equals(that.formattedValue) : that.formattedValue != null) {
			return false;
		}
		if (this.heading != null ? !this.heading.equals(that.heading)
		        : that.heading != null) {
			return false;
		}
		if (this.value != null ? !this.value.equals(that.value)
		        : that.value != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = this.value != null ? this.value.hashCode() : 0;
		result = 31
		        * result
		        + (this.formattedValue != null ? this.formattedValue.hashCode()
		                : 0);
		result = 31 * result
		        + (this.heading != null ? this.heading.hashCode() : 0);
		return result;
	}
}
