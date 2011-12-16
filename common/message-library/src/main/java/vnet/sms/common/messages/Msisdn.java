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

import java.io.Serializable;

/**
 * Represents an phonenumber in SMSj.
 * <p>
 * The address can be a phonenumber (+463482422) or alphanumeric ('SmsService').
 * Not all networks and transports supports alphanumeric sending id.
 * <p>
 * Max address length is <br>
 * - 20 digits (excluding any initial '+') or<br>
 * - 11 alphanumeric chars (if TON == TON_ALPHANUMERIC).
 * <p>
 * Look in SmsConstants for definitions of TON and NPI.
 * 
 * @author Markus Eriksson
 * @version $Id: Msisdn.java 410 2006-03-13 19:48:31Z c95men $
 */
public class Msisdn implements Serializable {

	private static final long	serialVersionUID	= -2876815864192558926L;

	private static final String	ALLOWED_DIGITS	 = "+0123456789*#ab";

	private int	                ton_	         = SmsConstants.TON_INTERNATIONAL;

	private int	                npi_	         = SmsConstants.NPI_ISDN_TELEPHONE;

	private String	            address_;

	/**
	 * Creates an Msisdn object.
	 * <p>
	 * This constructor tries to be intelligent by choosing the correct NPI and
	 * TON from the given address.
	 * 
	 * @param address
	 *            The address
	 * @throws IllegalArgumentException
	 *             Thrown if the address is invalid
	 */
	public Msisdn(final String address) throws IllegalArgumentException {
		int npi = SmsConstants.NPI_ISDN_TELEPHONE;
		int ton = SmsConstants.TON_INTERNATIONAL;

		for (int i = 0; i < address.length(); i++) {
			final char ch = address.charAt(i);
			if (ALLOWED_DIGITS.indexOf(ch) == -1) {
				ton = SmsConstants.TON_ALPHANUMERIC;
				npi = SmsConstants.NPI_UNKNOWN;
				break;
			}
		}

		init(address, ton, npi);
	}

	/**
	 * Creates an Msisdn object.
	 * <p>
	 * If you choose TON_ALPHANUMERIC then the NPI will be set to NPI_UNKNOWN.
	 * 
	 * @param address
	 *            The address
	 * @param ton
	 *            The type of number
	 * @param npi
	 *            The number plan indication
	 * @throws IllegalArgumentException
	 *             Thrown if the address is invalid
	 */
	public Msisdn(final String address, final int ton, final int npi)
	        throws IllegalArgumentException {
		init(address, ton, npi);
	}

	private void init(final String address, final int ton, final int npi)
	        throws IllegalArgumentException {
		int msisdnLength;

		if (address == null) {
			throw new IllegalArgumentException("Empty msisdn.");
		}

		this.ton_ = ton;
		this.address_ = address.trim();
		msisdnLength = this.address_.length();

		if (msisdnLength == 0) {
			throw new IllegalArgumentException("Empty address.");
		}

		if (ton == SmsConstants.TON_ALPHANUMERIC) {
			this.npi_ = SmsConstants.NPI_UNKNOWN;

			if (address.length() > 11) {
				throw new IllegalArgumentException(
				        "Alphanumeric address can be at most 11 chars.");
			}
		} else {
			this.npi_ = npi;

			// Trim '+' from address
			if (this.address_.charAt(0) == '+') {
				this.address_ = this.address_.substring(1);
				msisdnLength -= 1;
			}

			if (msisdnLength > 20) {
				throw new IllegalArgumentException(
				        "Too long address, Max allowed is 20 digits (excluding any inital '+').");
			}

			for (int i = 0; i < address.length(); i++) {
				final char ch = address.charAt(i);
				if (ALLOWED_DIGITS.indexOf(ch) == -1) {
					throw new IllegalArgumentException(
					        "Invalid digit in address. '" + ch + "'.");
				}
			}
		}
	}

	/**
	 * Returns the msisdn.
	 * 
	 * @return The address
	 */
	public String getAddress() {
		return this.address_;
	}

	/**
	 * Returns the TON field
	 * <p>
	 * See SmsConstants for definitions of different TON:s
	 * 
	 * @return The TON
	 */
	public int getTypeOfNumber() {
		return this.ton_;
	}

	/**
	 * Returns the NPI field
	 * <p>
	 * See SmsConstants for definitions of different TON:s
	 * 
	 * @return The NPI
	 */
	public int getNumberingPlanIdentification() {
		return this.npi_;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((this.address_ == null) ? 0 : this.address_.hashCode());
		result = prime * result + this.npi_;
		result = prime * result + this.ton_;
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
		final Msisdn other = (Msisdn) obj;
		if (this.address_ == null) {
			if (other.address_ != null) {
				return false;
			}
		} else if (!this.address_.equals(other.address_)) {
			return false;
		}
		if (this.npi_ != other.npi_) {
			return false;
		}
		if (this.ton_ != other.ton_) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Msisdn@" + this.hashCode() + " [ton_: " + this.ton_ + "|npi_: "
		        + this.npi_ + "|address_: " + this.address_ + "]";
	}
}
