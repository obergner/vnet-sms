/**
 * 
 */
package vnet.routing.netty.server.support.window.map;

/**
 * @author obergner
 * 
 */
public class MapCapacityExhaustedException extends IllegalStateException {

	private static final long serialVersionUID = 8622869743640807744L;

	private final int maximumMapCapacity;

	/**
	 * 
	 */
	public MapCapacityExhaustedException(final int maximumMapCapacity) {
		this.maximumMapCapacity = maximumMapCapacity;
	}

	/**
	 * @param s
	 */
	public MapCapacityExhaustedException(final String s,
			final int maximumMapCapacity) {
		super(s);
		this.maximumMapCapacity = maximumMapCapacity;
	}

	/**
	 * @param cause
	 */
	public MapCapacityExhaustedException(final Throwable cause,
			final int maximumMapCapacity) {
		super(cause);
		this.maximumMapCapacity = maximumMapCapacity;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MapCapacityExhaustedException(final String message,
			final Throwable cause, final int maximumMapCapacity) {
		super(message, cause);
		this.maximumMapCapacity = maximumMapCapacity;
	}

	public final int getMaximumMapCapacity() {
		return this.maximumMapCapacity;
	}
}
