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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import vnet.sms.common.messages.util.SmsUdhUtil;

/**
 * Represents an SMS pdu
 * <p>
 * A SMS pdu consists of a user data header (UDH) and the actual content often
 * called user data (UD).
 * 
 * @author Markus Eriksson
 * @version $Id: SmsPdu.java 410 2006-03-13 19:48:31Z c95men $
 */
public class SmsPdu implements Serializable {

	private static final long	serialVersionUID	= 6316944705767684892L;

	private UdhElement[]	  udhElements_;

	private UserData	      ud_;

	/**
	 * Creates an empty SMS pdu object
	 */
	public SmsPdu() {
		// Empty
	}

	/**
	 * Creates an SMS pdu object.
	 * 
	 * @param udhElements
	 *            The UDH elements
	 * @param ud
	 *            The content
	 * @param udLength
	 *            The length of the content. Can be in octets or septets
	 *            depending on the DCS
	 */
	public SmsPdu(final UdhElement[] udhElements, final byte[] ud,
	        final int udLength, final DataCodingScheme dcs) {
		setUserDataHeaders(udhElements);
		setUserData(ud, udLength, dcs);
	}

	/**
	 * Creates an SMS pdu object.
	 * 
	 * @param udhElements
	 *            The UDH elements
	 * @param ud
	 *            The content
	 */
	public SmsPdu(final UdhElement[] udhElements, final UserData ud) {
		setUserDataHeaders(udhElements);
		setUserData(ud);
	}

	/**
	 * Sets the UDH field
	 * 
	 * @param udhElements
	 *            The UDH elements
	 */
	public void setUserDataHeaders(final UdhElement[] udhElements) {
		if (udhElements != null) {
			this.udhElements_ = new UdhElement[udhElements.length];

			System.arraycopy(udhElements, 0, this.udhElements_, 0,
			        udhElements.length);
		} else {
			this.udhElements_ = null;
		}
	}

	/**
	 * Returns the user data headers
	 * 
	 * @return A byte array representing the UDH fields or null if there aren't
	 *         any UDH
	 */
	public byte[] getUserDataHeaders() {
		try {
			if (this.udhElements_ == null) {
				return null;
			}

			final ByteArrayOutputStream baos = new ByteArrayOutputStream(100);

			baos.write((byte) SmsUdhUtil.getTotalSize(this.udhElements_));
			for (final UdhElement element : this.udhElements_) {
				element.writeTo(baos);
			}

			return baos.toByteArray();
		} catch (final IOException ioe) {
			// Shouldn't happen.
			throw new RuntimeException(
			        "Failed to write to ByteArrayOutputStream");
		}
	}

	/**
	 * Sets the user data field of the message.
	 * 
	 * @param ud
	 *            The content
	 * @param udLength
	 *            The length, can be in septets or octets depending on the DCS
	 * @param dcs
	 *            The data coding scheme
	 */
	public void setUserData(final byte[] ud, final int udLength,
	        final DataCodingScheme dcs) {
		this.ud_ = new UserData(ud, udLength, dcs);
	}

	/**
	 * Sets the user data field of the message.
	 * 
	 * @param ud
	 *            The content
	 */
	public void setUserData(final UserData ud) {
		this.ud_ = ud;
	}

	/**
	 * Returns the user data part of the message.
	 * 
	 * @return UD field
	 */
	public UserData getUserData() {
		return this.ud_;
	}

	/**
	 * Returns the dcs.
	 * 
	 * @return dcs
	 */
	public DataCodingScheme getDcs() {
		return this.ud_.getDcs();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.ud_ == null) ? 0 : this.ud_.hashCode());
		result = prime * result + Arrays.hashCode(this.udhElements_);
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
		final SmsPdu other = (SmsPdu) obj;
		if (this.ud_ == null) {
			if (other.ud_ != null) {
				return false;
			}
		} else if (!this.ud_.equals(other.ud_)) {
			return false;
		}
		if (!Arrays.equals(this.udhElements_, other.udhElements_)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SmsPdu@" + this.hashCode() + "[udhElements: "
		        + Arrays.toString(this.udhElements_) + "|ud: " + this.ud_ + "]";
	}
}
