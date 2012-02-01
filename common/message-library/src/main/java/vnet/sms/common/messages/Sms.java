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

import java.io.UnsupportedEncodingException;
import java.util.Random;

import vnet.sms.common.messages.util.SmsPduUtil;
import vnet.sms.common.messages.util.SmsUdhUtil;

/**
 * Represents a text message.
 * <p>
 * The text can be sent in unicode (max 70 chars/SMS), 8-bit (max 140 chars/SMS)
 * or GSM encoding (max 160 chars/SMS).
 * 
 * @author Markus Eriksson
 * @version $Id: Sms.java 410 2006-03-13 19:48:31Z c95men $
 */
public class Sms extends Message {

	private static final long	serialVersionUID	= 7793476003910915706L;

	private static Random	  rnd	             = new Random();

	private String	          text;

	private DataCodingScheme	dcs;

	/**
	 * Creates an Sms with the given dcs.
	 * 
	 * @param msg
	 *            The message
	 * @param dcs
	 *            The data coding scheme
	 */
	public Sms(final String msg, final DataCodingScheme dcs) {
		super();
		notNull(msg, "Argument 'msg' must not be null");
		notNull(dcs, "Argument 'dcs' must not be null");
		setText(msg, dcs);
	}

	/**
	 * Creates an Sms with the given alphabet and message class.
	 * <p>
	 * alphabet can be any of:<br>
	 * - SmsConstants.ALPHABET_GSM<br>
	 * - SmsConstants.ALPHABET_8BIT<br>
	 * - SmsConstants.ALPHABET_UCS2<br>
	 * <p>
	 * messageClass can be any of:<br>
	 * - SmsConstants.MSG_CLASS_0 (Often called a FLASH message)<br>
	 * - SmsConstants.MSG_CLASS_1<br>
	 * - SmsConstants.MSG_CLASS_2<br>
	 * - SmsConstants.MSG_CLASS_3<br>
	 * 
	 * @param msg
	 *            The message
	 * @param alphabet
	 *            The alphabet
	 * @param messageClass
	 *            The messageclass
	 */
	public Sms(final String msg, final int alphabet, final int messageClass) {
		this(msg, DataCodingScheme.getGeneralDataCodingDcs(alphabet,
		        messageClass));
	}

	/**
	 * Creates an Sms with default 7Bit GSM Alphabet
	 * 
	 * @param msg
	 *            The message
	 */
	public Sms(final String msg) {
		this(msg, DataCodingScheme.ALPHABET_GSM,
		        DataCodingScheme.MSG_CLASS_UNKNOWN);
	}

	/**
	 * Returns the text message.
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 */
	public void setText(final String text) {
		if (text == null) {
			throw new IllegalArgumentException(
			        "Text cannot be null, use an empty string instead.");
		}

		this.text = text;
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 */
	public void setText(final String text, final DataCodingScheme dcs) {
		// Check input for null
		if (text == null) {
			throw new IllegalArgumentException(
			        "text cannot be null, use an empty string instead.");
		}

		if (dcs == null) {
			throw new IllegalArgumentException("dcs cannot be null.");
		}

		this.text = text;
		this.dcs = dcs;
	}

	/**
	 * Returns the dcs.
	 */
	public DataCodingScheme getDcs() {
		return this.dcs;
	}

	/**
	 * Returns the user data.
	 * 
	 * @return user data
	 */
	public UserData getUserData() {
		try {
			final UserData ud;
			switch (this.dcs.getAlphabet()) {
			case DataCodingScheme.ALPHABET_GSM:
				ud = new UserData(SmsPduUtil.getSeptets(this.text),
				        this.text.length(), this.dcs);
				break;

			case DataCodingScheme.ALPHABET_8BIT:
				ud = new UserData(this.text.getBytes("ISO-8859-1"),
				        this.text.length(), this.dcs);
				break;

			case DataCodingScheme.ALPHABET_UCS2:
				ud = new UserData(this.text.getBytes("UTF-16BE"),
				        this.text.length() * 2, this.dcs);
				break;

			default:
				ud = null;
				break;
			}

			return ud;
		} catch (final UnsupportedEncodingException ex) {
			// Shouldn't happen. According to the javadoc documentation
			// for JDK 1.3.1 the "UTF-16BE" and "ISO-8859-1" encoding
			// are standard...
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns null.
	 */
	public UdhElement[] getUdhElements() {
		return null;
	}

	/**
	 * Converts this message into SmsPdu:s
	 * <p>
	 * If the message is too long to fit in one SmsPdu the message is divided
	 * into many SmsPdu:s with a 8-bit concat pdu UDH element.
	 * 
	 * @return Returns the message as SmsPdu:s
	 */
	public SmsPdu[] getPdus() {
		final UserData ud = getUserData();
		final UdhElement[] udhElements = getUdhElements();
		final int udhLength = SmsUdhUtil.getTotalSize(udhElements);
		final int nBytesLeft = 140 - udhLength;

		final SmsPdu[] smsPdus;
		switch (ud.getDcs().getAlphabet()) {
		case DataCodingScheme.ALPHABET_GSM:
			smsPdus = createSeptetPdus(udhElements, ud, nBytesLeft);
			break;
		case DataCodingScheme.ALPHABET_UCS2:
			smsPdus = createUnicodePdus(udhElements, ud, nBytesLeft);
			break;
		case DataCodingScheme.ALPHABET_8BIT:
		default:
			smsPdus = createOctalPdus(udhElements, ud, nBytesLeft);
			break;
		}

		return smsPdus;
	}

	private SmsPdu[] createOctalPdus(final UdhElement[] udhElements,
	        final UserData ud, final int maxBytes) {
		// 8-bit concat header is 6 bytes...
		final int nMaxConcatChars = maxBytes - 6;
		final int nMaxChars = maxBytes;

		final SmsPdu[] smsPdus;
		if (ud.getLength() <= nMaxChars) {
			smsPdus = new SmsPdu[] { new SmsPdu(udhElements, ud) };
		} else {
			final int refno = rnd.nextInt(256);

			// Calculate number of SMS needed
			int nSms = ud.getLength() / nMaxConcatChars;
			if ((ud.getLength() % nMaxConcatChars) > 0) {
				nSms += 1;
			}
			smsPdus = new SmsPdu[nSms];

			// Calculate number of UDHI
			UdhElement[] pduUdhElements = null;
			if (udhElements == null) {
				pduUdhElements = new UdhElement[1];
			} else {
				pduUdhElements = new UdhElement[udhElements.length + 1];

				// Copy the UDH headers
				for (int j = 0; j < udhElements.length; j++) {
					// Leave position pduUdhElements[0] for the concat UDHI
					pduUdhElements[j + 1] = udhElements[j];
				}
			}

			// Create pdus
			for (int i = 0; i < nSms; i++) {
				// Create concat header
				pduUdhElements[0] = SmsUdhUtil.get8BitConcatUdh(refno, nSms,
				        i + 1);

				// Create
				// Must concatenate messages
				// Calc pdu length
				final int udOffset = nMaxConcatChars * i;
				int udBytes = ud.getLength() - udOffset;
				if (udBytes > nMaxConcatChars) {
					udBytes = nMaxConcatChars;
				}
				final int udLength = udBytes;

				final byte[] pduUd = new byte[udBytes];
				SmsPduUtil.arrayCopy(ud.getData(), udOffset, pduUd, 0, udBytes);
				smsPdus[i] = new SmsPdu(pduUdhElements, pduUd, udLength,
				        ud.getDcs());
			}
		}
		return smsPdus;
	}

	private SmsPdu[] createUnicodePdus(final UdhElement[] udhElements,
	        final UserData ud, final int maxBytes) {
		// 8-bit concat header is 6 bytes...
		final int nMaxConcatChars = (maxBytes - 6) / 2;

		final SmsPdu[] smsPdus;
		if (ud.getLength() <= maxBytes) {
			smsPdus = new SmsPdu[] { new SmsPdu(udhElements, ud) };
		} else {
			final int refno = rnd.nextInt(256);

			// Calculate number of SMS needed
			int nSms = (ud.getLength() / 2) / nMaxConcatChars;
			if (((ud.getLength() / 2) % nMaxConcatChars) > 0) {
				nSms += 1;
			}
			smsPdus = new SmsPdu[nSms];

			// Calculate number of UDHI
			UdhElement[] pduUdhElements = null;
			if (udhElements == null) {
				pduUdhElements = new UdhElement[1];
			} else {
				pduUdhElements = new UdhElement[udhElements.length + 1];
				// Copy the UDH headers
				for (int j = 0; j < udhElements.length; j++) {
					// Leave position pduUdhElements[0] for the concat UDHI
					pduUdhElements[j + 1] = udhElements[j];
				}
			}

			// Create pdus
			for (int i = 0; i < nSms; i++) {
				// Create concat header
				pduUdhElements[0] = SmsUdhUtil.get8BitConcatUdh(refno, nSms,
				        i + 1);
				// Create
				// Must concatenate messages
				// Calc pdu length
				final int udOffset = nMaxConcatChars * i;
				int udLength = (ud.getLength() / 2) - udOffset;
				if (udLength > nMaxConcatChars) {
					udLength = nMaxConcatChars;
				}
				final int udBytes = udLength * 2;

				final byte[] pduUd = new byte[udBytes];
				SmsPduUtil.arrayCopy(ud.getData(), udOffset * 2, pduUd, 0,
				        udBytes);
				smsPdus[i] = new SmsPdu(pduUdhElements, pduUd, udBytes,
				        ud.getDcs());
			}
		}
		return smsPdus;
	}

	private SmsPdu[] createSeptetPdus(final UdhElement[] udhElements,
	        final UserData ud, final int maxBytes) {
		// 8-bit concat header is 6 bytes...
		final int nMaxConcatChars = ((maxBytes - 6) * 8) / 7;
		final int nMaxChars = (maxBytes * 8) / 7;

		final SmsPdu[] smsPdus;
		if (ud.getLength() <= nMaxChars) {
			smsPdus = new SmsPdu[] { new SmsPdu(udhElements, ud) };
		} else {
			final int refno = rnd.nextInt(256);

			// Calculate number of SMS needed
			int nSms = ud.getLength() / nMaxConcatChars;
			if ((ud.getLength() % nMaxConcatChars) > 0) {
				nSms += 1;
			}
			smsPdus = new SmsPdu[nSms];

			// Calculate number of UDHI
			UdhElement[] pduUdhElements = null;
			if (udhElements == null) {
				pduUdhElements = new UdhElement[1];
			} else {
				pduUdhElements = new UdhElement[udhElements.length + 1];

				// Copy the UDH headers
				for (int j = 0; j < udhElements.length; j++) {
					// Leave position pduUdhElements[0] for the concat UDHI
					pduUdhElements[j + 1] = udhElements[j];
				}
			}

			// Convert septets into a string...
			final String msg = SmsPduUtil.readSeptets(ud.getData(),
			        ud.getLength());

			// Create pdus
			for (int i = 0; i < nSms; i++) {
				// Create concat header
				pduUdhElements[0] = SmsUdhUtil.get8BitConcatUdh(refno, nSms,
				        i + 1);
				// Create
				// Must concatenate messages
				// Calc pdu length
				final int udOffset = nMaxConcatChars * i;
				int udLength = ud.getLength() - udOffset;
				if (udLength > nMaxConcatChars) {
					udLength = nMaxConcatChars;
				}

				final byte[] pduUd = SmsPduUtil.getSeptets(msg.substring(
				        udOffset, udOffset + udLength));
				smsPdus[i] = new SmsPdu(pduUdhElements, pduUd, udLength,
				        ud.getDcs());
			}
		}
		return smsPdus;
	}

	@Override
	public String toString() {
		return "Sms@" + this.hashCode() + "[ID: " + this.getId()
		        + "|creationTimestamp: " + this.getCreationTimestamp()
		        + "|text: " + this.text + "|dcs: " + this.dcs + "]";
	}

}
