/**
 * 
 */
package vnet.sms.common.messages;

/**
 * @author obergner
 * 
 */
public final class Headers {

	public static final String	EVENT_TYPE	            = "urn:message:event:type";

	public static final String	MESSAGE_REFERENCE	    = "urn:message:message-reference";

	public static final String	RECEIVING_CHANNEL_ID	= "urn:message:receiving-channel:id";

	public static final String	RECEIVE_TIMESTAMP	    = "urn:message:receive-timestamp";

	public static final String	SENDER_SOCKET_ADDRESS	= "urn:message:sender:socket-address";

	public static final String	RECEIVER_SOCKET_ADDRESS	= "urn:message:receiver:socket-address";

	public static final String	ERROR_KEY	            = "urn:message:error:key";

	public static final String	ERROR_DESCRIPTION	    = "urn:message:error:description";

	private Headers() {
		// Noop
	}
}
