package vnet.sms.routingengine.blueprint.test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ObjectHelper {

	private static final String	DEFAULT_DELIMITER	= ",";

	/**
	 * Utility classes should not have a public constructor.
	 */
	private ObjectHelper() {
	}

	/**
	 * Tests whether the value is <b>not</b> <tt>null</tt> or an empty string.
	 * 
	 * @param value
	 *            the value, if its a String it will be tested for text length
	 *            as well
	 * @return true if <b>not</b> empty
	 */
	public static boolean isNotEmpty(final Object value) {
		if (value == null) {
			return false;
		} else if (value instanceof String) {
			final String text = (String) value;
			return text.trim().length() > 0;
		} else {
			return true;
		}
	}

	/**
	 * Creates an iterator over the value if the value is a collection, an
	 * Object[], a String with values separated by comma, or a primitive type
	 * array; otherwise to simplify the caller's code, we just create a
	 * singleton collection iterator over a single value
	 * <p/>
	 * Will default use comma for String separating String values.
	 * 
	 * @param value
	 *            the value
	 * @return the iterator
	 */
	public static Iterator<Object> createIterator(final Object value) {
		return createIterator(value, DEFAULT_DELIMITER);
	}

	/**
	 * Creates an iterator over the value if the value is a collection, an
	 * Object[], a String with values separated by the given delimiter, or a
	 * primitive type array; otherwise to simplify the caller's code, we just
	 * create a singleton collection iterator over a single value
	 * 
	 * @param value
	 *            the value
	 * @param delimiter
	 *            delimiter for separating String values
	 * @return the iterator
	 */
	@SuppressWarnings("unchecked")
	public static Iterator<Object> createIterator(final Object value,
	        final String delimiter) {
		if (value == null) {
			return Collections.emptyList().iterator();
		} else if (value instanceof Iterator) {
			return (Iterator<Object>) value;
		} else if (value instanceof Iterable) {
			return ((Iterable<Object>) value).iterator();
		} else if (value.getClass().isArray()) {
			// TODO we should handle primitive array types?
			final List<Object> list = Arrays.asList((Object[]) value);
			return list.iterator();
		} else if (value instanceof NodeList) {
			// lets iterate through DOM results after performing XPaths
			final NodeList nodeList = (NodeList) value;
			return CastUtils.cast(new Iterator<Node>() {
				int	idx	= -1;

				@Override
				public boolean hasNext() {
					return (this.idx + 1) < nodeList.getLength();
				}

				@Override
				public Node next() {
					this.idx++;
					return nodeList.item(this.idx);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			});
		} else if (value instanceof String) {
			final String s = (String) value;

			// this code is optimized to only use a Scanner if needed, eg there
			// is a delimiter

			if ((delimiter != null) && s.contains(delimiter)) {
				// use a scanner if it contains the delimiter
				final Scanner scanner = new Scanner((String) value);
				String realDelimiter = delimiter;
				if (DEFAULT_DELIMITER.equals(realDelimiter)) {
					// we use the default delimiter which is a comma, then cater
					// for bean expressions with OGNL
					// which may have balanced parentheses pairs as well.
					// if the value contains parentheses we need to balance
					// those, to avoid iterating
					// in the middle of parentheses pair, so use this regular
					// expression (a bit hard to read)
					// the regexp will split by comma, but honor parentheses
					// pair that may include commas
					// as well, eg if value =
					// "bean=foo?method=killer(a,b),bean=bar?method=great(a,b)"
					// then the regexp will split that into two:
					// -> bean=foo?method=killer(a,b)
					// -> bean=bar?method=great(a,b)
					// http://stackoverflow.com/questions/1516090/splitting-a-title-into-separate-parts
					realDelimiter = ",(?!(?:[^\\(,]|[^\\)],[^\\)])+\\))";
				}

				scanner.useDelimiter(realDelimiter);
				return CastUtils.cast(scanner);
			} else {
				// use a plain iterator that returns the value as is as there
				// are only a single value
				return CastUtils.cast(new Iterator<String>() {
					int	idx	= -1;

					@Override
					public boolean hasNext() {
						// empty string should not be regarded as having next
						return (this.idx + 1 == 0)
						        && ObjectHelper.isNotEmpty(s);
					}

					@Override
					public String next() {
						this.idx++;
						return s;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				});
			}
		} else {
			return Collections.singletonList(value).iterator();
		}
	}

	/**
	 * Attempts to load the given resource as a stream using the thread context
	 * class loader or the class loader used to load this class
	 * 
	 * @param name
	 *            the name of the resource to load
	 * @return the stream or null if it could not be loaded
	 */
	public static URL loadResourceAsURL(final String name) {
		URL url = null;

		final ClassLoader contextClassLoader = Thread.currentThread()
		        .getContextClassLoader();
		if (contextClassLoader != null) {
			url = contextClassLoader.getResource(name);
		}
		if (url == null) {
			url = ObjectHelper.class.getClassLoader().getResource(name);
		}

		return url;
	}

	/**
	 * Strip any leading separators
	 */
	public static String stripLeadingSeparator(final String name) {
		if (name == null) {
			return null;
		}
		String strippedName = name;
		while (strippedName.startsWith("/")
		        || strippedName.startsWith(File.separator)) {
			strippedName = name.substring(1);
		}
		return strippedName;
	}
}
