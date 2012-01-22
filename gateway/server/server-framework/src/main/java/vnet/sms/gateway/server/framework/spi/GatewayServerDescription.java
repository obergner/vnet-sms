/**
 * 
 */
package vnet.sms.gateway.server.framework.spi;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public class GatewayServerDescription implements Serializable {

	private static final long	serialVersionUID	= 5010903612854056605L;

	private final String	  name;

	private final Version	  version;

	/**
	 * @param name
	 * @param majorVersion
	 * @param minorVersion
	 * @param incrementalVersion
	 * @param qualifier
	 * @param buildNumber
	 */
	public GatewayServerDescription(final String name, final int majorVersion,
	        final int minorVersion, final int incrementalVersion,
	        final String qualifier, final int buildNumber) {
		this(name, new Version(majorVersion, minorVersion, incrementalVersion,
		        qualifier, buildNumber));
	}

	/**
	 * @param name
	 * @param version
	 */
	public GatewayServerDescription(final String name, final Version version) {
		notEmpty(name, "Argument 'name' must neither be null nor empty. Got: "
		        + name);
		notNull(version, "Argument 'version' must not be null.");
		this.name = name;
		this.version = version;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * @return the version
	 */
	public final Version getVersion() {
		return this.version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result
		        + ((this.version == null) ? 0 : this.version.hashCode());
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
		final GatewayServerDescription other = (GatewayServerDescription) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!this.version.equals(other.version)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.name + " v. " + this.version.toString();
	}
}
