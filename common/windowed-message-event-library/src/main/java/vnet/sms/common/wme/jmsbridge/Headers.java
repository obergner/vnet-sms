/**
 * 
 */
package vnet.sms.common.wme.jmsbridge;

/**
 * @author obergner
 * 
 */
public final class Headers {

	public static final String	EVENT_TYPE	            = "urn:message:event:type";

	public static final String	RECEIVING_CHANNEL_ID	= "urn:message:receiving-channel:id";

	public static final String	RECEIVE_TIMESTAMP	    = "urn:message:receive-timestamp";

	public static final String	SENDER_SOCKET_ADDRESS	= "urn:message:sender:socket-address";

	public static final String	RECEIVER_SOCKET_ADDRESS	= "urn:message:receiver:socket-address";

	private Headers() {
		// Noop
	}
}