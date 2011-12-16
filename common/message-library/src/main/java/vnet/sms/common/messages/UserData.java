/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is "SMS Library for the Java platform".
 *
 * The Initial Developer of the Original Code is Markus Eriksson.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * @author Markus Eriksson
 * @version $Id: UserData.java 410 2006-03-13 19:48:31Z c95men $
 */
public class UserData implements Serializable {

	private static final long	   serialVersionUID	= -3893690943294008224L;

	/** The actual user data. */
	private final byte[]	       data_;

	/** Length of data, in octets or septets depending on the dcs. */
	private final int	           length_;

	/** Data Coding Scheme for this user data. */
	private final DataCodingScheme	dcs_;

	public UserData(final byte[] userData, final int userDataLength,
	        final DataCodingScheme dataCodingScheme) {
		notNull(userData, "Argument 'userData' must not be null");
		notNull(dataCodingScheme,
		        "Argument 'dataCodingScheme' must not be null");
		this.data_ = userData;
		this.length_ = userDataLength;
		this.dcs_ = dataCodingScheme;
	}

	public UserData(final byte[] userData) {
		notNull(userData, "Argument 'userData' must not be null");
		this.data_ = userData;
		this.length_ = userData.length;
		this.dcs_ = DataCodingScheme.getGeneralDataCodingDcs(
		        DataCodingScheme.ALPHABET_8BIT,
		        DataCodingScheme.MSG_CLASS_UNKNOWN);
	}

	public byte[] getData() {
		return this.data_;
	}

	/**
	 * Returns the length of the user data field.
	 * 
	 * This can be in characters or byte depending on the message (DCS). If
	 * message is 7 bit coded the length is given in septets. If 8bit or UCS2
	 * the length is in octets.
	 * 
	 * @return The length
	 */
	public int getLength() {
		return this.length_;
	}

	/**
	 * Returns the data coding scheme.
	 * 
	 * @return The dcs
	 */
	public DataCodingScheme getDcs() {
		return this.dcs_;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.data_);
		result = prime * result
		        + ((this.dcs_ == null) ? 0 : this.dcs_.hashCode());
		result = prime * result + this.length_;
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
		final UserData other = (UserData) obj;
		if (!Arrays.equals(this.data_, other.data_)) {
			return false;
		}
		if (this.dcs_ == null) {
			if (other.dcs_ != null) {
				return false;
			}
		} else if (!this.dcs_.equals(other.dcs_)) {
			return false;
		}
		if (this.length_ != other.length_) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserData@" + this.hashCode() + " [data_: "
		        + Arrays.toString(this.data_) + "|length_: " + this.length_
		        + "|dcs_: " + this.dcs_ + "]";
	}
}
