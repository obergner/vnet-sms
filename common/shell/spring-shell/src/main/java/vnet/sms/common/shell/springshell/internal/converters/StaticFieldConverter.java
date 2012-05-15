package vnet.sms.common.shell.springshell.internal.converters;

import vnet.sms.common.shell.springshell.Converter;

/**
 * Interface for adding and removing classes that provide static fields which
 * should be made available via a {@link Converter}.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public interface StaticFieldConverter extends Converter<Object> {

	void add(Class<?> clazz);

	void remove(Class<?> clazz);
}
