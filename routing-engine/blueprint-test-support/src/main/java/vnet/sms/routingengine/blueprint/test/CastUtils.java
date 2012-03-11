package vnet.sms.routingengine.blueprint.test;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

import javax.naming.NamingEnumeration;

public final class CastUtils {

	private CastUtils() {
		// utility class, never constructed
	}

	public static <T, U> Map<T, U> cast(final Map<?, ?> p) {
		return (Map<T, U>) p;
	}

	public static <T, U> Map<T, U> cast(final Map<?, ?> p, final Class<T> t,
	        final Class<U> u) {
		return (Map<T, U>) p;
	}

	public static <T> Collection<T> cast(final Collection<?> p) {
		return (Collection<T>) p;
	}

	public static <T> Collection<T> cast(final Collection<?> p,
	        final Class<T> cls) {
		return (Collection<T>) p;
	}

	public static <T> List<T> cast(final List<?> p) {
		return (List<T>) p;
	}

	public static <T> List<T> cast(final List<?> p, final Class<T> cls) {
		return (List<T>) p;
	}

	public static <T> Iterator<T> cast(final Iterator<?> p) {
		return (Iterator<T>) p;
	}

	public static <T> Iterator<T> cast(final Iterator<?> p, final Class<T> cls) {
		return (Iterator<T>) p;
	}

	public static <T> Set<T> cast(final Set<?> p) {
		return (Set<T>) p;
	}

	public static <T> Set<T> cast(final Set<?> p, final Class<T> cls) {
		return (Set<T>) p;
	}

	public static <T> Queue<T> cast(final Queue<?> p) {
		return (Queue<T>) p;
	}

	public static <T> Queue<T> cast(final Queue<?> p, final Class<T> cls) {
		return (Queue<T>) p;
	}

	public static <T, U> Hashtable<T, U> cast(final Hashtable<?, ?> p) {
		return (Hashtable<T, U>) p;
	}

	public static <T, U> Hashtable<T, U> cast(final Hashtable<?, ?> p,
	        final Class<T> pc, final Class<U> uc) {
		return (Hashtable<T, U>) p;
	}

	public static <T, U> Map.Entry<T, U> cast(final Map.Entry<?, ?> p) {
		return (Map.Entry<T, U>) p;
	}

	public static <T, U> Map.Entry<T, U> cast(final Map.Entry<?, ?> p,
	        final Class<T> pc, final Class<U> uc) {
		return (Map.Entry<T, U>) p;
	}

	public static <T> Enumeration<T> cast(final Enumeration<?> p) {
		return (Enumeration<T>) p;
	}

	public static <T> NamingEnumeration<T> cast(final NamingEnumeration<?> p) {
		return (NamingEnumeration<T>) p;
	}

	public static <T> Class<T> cast(final Class<?> p) {
		return (Class<T>) p;
	}

	public static <T> Class<T> cast(final Class<?> p, final Class<T> cls) {
		return (Class<T>) p;
	}

	public static <T> Future<T> cast(final Future<?> p) {
		return (Future<T>) p;
	}
}
