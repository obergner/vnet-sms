/**
 * 
 */
package vnet.sms.gateway.server.framework.spi;

import java.io.Serializable;

/**
 * @author obergner
 * 
 */
public final class Version implements Serializable, Comparable<Version> {

	/**
     * 
     */
	private static final long	serialVersionUID	= 5022321534158546222L;

	private final int	      majorVersion;

	private final int	      minorVersion;

	private final int	      incrementalVersion;

	private final String	  qualifier;

	private final int	      buildNumber;

	/**
	 * @param majorVersion
	 * @param minorVersion
	 * @param incrementalVersion
	 * @param qualifier
	 * @param buildNumber
	 */
	public Version(final int majorVersion, final int minorVersion,
	        final int incrementalVersion, final String qualifier,
	        final int buildNumber) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.incrementalVersion = incrementalVersion;
		this.qualifier = qualifier;
		this.buildNumber = buildNumber;
	}

	/**
	 * @return the majorVersion
	 */
	public final int getMajorVersion() {
		return this.majorVersion;
	}

	/**
	 * @return the minorVersion
	 */
	public final int getMinorVersion() {
		return this.minorVersion;
	}

	/**
	 * @return the incrementalVersion
	 */
	public final int getIncrementalVersion() {
		return this.incrementalVersion;
	}

	/**
	 * @return the qualifier
	 */
	public final String getQualifier() {
		return this.qualifier;
	}

	/**
	 * @return the buildNumber
	 */
	public final int getBuildNumber() {
		return this.buildNumber;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Version other) {
		if (this.majorVersion < other.majorVersion) {
			return -1;
		}
		if (this.majorVersion > other.majorVersion) {
			return 1;
		}
		if (this.minorVersion < other.minorVersion) {
			return -1;
		}
		if (this.minorVersion > other.minorVersion) {
			return 1;
		}
		if (this.incrementalVersion < other.incrementalVersion) {
			return -1;
		}
		if (this.incrementalVersion > other.incrementalVersion) {
			return 1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.buildNumber;
		result = prime * result + this.incrementalVersion;
		result = prime * result + this.majorVersion;
		result = prime * result + this.minorVersion;
		result = prime * result
		        + ((this.qualifier == null) ? 0 : this.qualifier.hashCode());
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
		final Version other = (Version) obj;
		if (this.buildNumber != other.buildNumber) {
			return false;
		}
		if (this.incrementalVersion != other.incrementalVersion) {
			return false;
		}
		if (this.majorVersion != other.majorVersion) {
			return false;
		}
		if (this.minorVersion != other.minorVersion) {
			return false;
		}
		if (this.qualifier == null) {
			if (other.qualifier != null) {
				return false;
			}
		} else if (!this.qualifier.equals(other.qualifier)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.majorVersion + "." + this.minorVersion + "."
		        + this.incrementalVersion
		        + (this.qualifier != null ? "-" + this.qualifier : "")
		        + " (build " + this.buildNumber + ")";
	}
}
