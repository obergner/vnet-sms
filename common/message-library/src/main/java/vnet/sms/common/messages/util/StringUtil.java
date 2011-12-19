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
package vnet.sms.common.messages.util;

import java.util.Random;

/**
 * Various functions to encode and decode strings.
 * 
 * @author Markus Eriksson
 */
public final class StringUtil {

	private static final char[]	RANDOM_CHARS	= "abcdefghijklmnopqrstuvwxyz1234567890"
	                                                 .toCharArray();

	private static Random	    rnd_	     = new Random();

	/**
	 * This class isn't intended to be instantiated.
	 */
	private StringUtil() {
	}

	/**
	 * 
	 * @param stringTable
	 * @param text
	 * @return
	 */
	public static int findString(final String[] stringTable, final String text) {
		if (stringTable != null) {
			for (int i = 0; i < stringTable.length; i++) {
				if ((stringTable[i] != null) && (stringTable[i].equals(text))) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Converts a byte array to a string with hex values.
	 * 
	 * @param data
	 *            Data to convert
	 * @return the encoded string
	 */
	public static String bytesToHexString(final byte[] data) {
		final StringBuffer hexStrBuff = new StringBuffer(data.length * 2);

		for (final byte element : data) {
			final String hexByteStr = Integer.toHexString(element & 0xff)
			        .toUpperCase();
			if (hexByteStr.length() == 1) {
				hexStrBuff.append("0");
			}
			hexStrBuff.append(hexByteStr);
		}

		return hexStrBuff.toString();
	}

	/**
	 * Converts a byte to a string with hex values.
	 * 
	 * @param data
	 *            Byte to convert
	 * @return the encoded string
	 */
	public static String byteToHexString(final byte data) {
		final StringBuffer hexStrBuff = new StringBuffer(2);

		final String hexByteStr = Integer.toHexString(data & 0xff)
		        .toUpperCase();
		if (hexByteStr.length() == 1) {
			hexStrBuff.append("0");
		}
		hexStrBuff.append(hexByteStr);

		return hexStrBuff.toString();
	}

	/**
	 * Converts a string of hex characters to a byte array.
	 * 
	 * @param hexString
	 *            The hex string to read
	 * @return the resulting byte array
	 */
	public static byte[] hexStringToBytes(final String hexString) {
		final byte[] data = new byte[hexString.length() / 2];

		for (int i = 0; i < data.length; i++) {
			final String a = hexString.substring(i * 2, i * 2 + 2);
			data[i] = (byte) Integer.parseInt(a, 16);
		}

		return data;
	}

	/**
	 * Method intToString.
	 * 
	 * Converst an integer to nChars characters
	 * 
	 * @param value
	 *            Integer value
	 * @param nChars
	 *            Number of chars to represent the "value"
	 * @return String The string representing "value"
	 */
	public static String intToString(final int value, final int nChars) {
		final String strValue = Integer.toString(value);
		final StringBuffer strBuf = new StringBuffer(nChars);

		for (int i = strValue.length(); i < nChars; i++) {
			strBuf.append('0');
		}
		strBuf.append(strValue);

		return strBuf.toString();
	}

	/**
	 * Generates a random string of the given length.
	 * 
	 * "abcdefghijklmnopqrstuvwxyz1234567890"
	 * 
	 * @param length
	 * @return A random string
	 */
	public static String randString(final int length) {
		final StringBuffer sb = new StringBuffer(length);

		for (int i = 0; i < length; i++) {
			sb.append(RANDOM_CHARS[rnd_.nextInt(RANDOM_CHARS.length)]);
		}

		return sb.toString();
	}
}
