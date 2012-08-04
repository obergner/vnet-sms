/**
 * 
 */
package vnet.sms.gateway.nettysupport;

import static org.apache.commons.lang.Validate.notNull;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;

import vnet.sms.common.messages.GsmPdu;

/**
 * @author obergner
 * 
 */
public final class MessageProcessingContext {

	static final String	                         CURRENT_USER_MDC_KEY	= "currentUser";

	static final String	                         MESSAGE_UUID_MDC_KEY	= "messageUuid";

	public static final MessageProcessingContext	INSTANCE	      = new MessageProcessingContext();

	private MessageProcessingContext() {
	}

	public void onUserEnter(final Authentication user) {
		notNull(user, "Argument 'user' must not be null");
		if (MDC.get(CURRENT_USER_MDC_KEY) != null) {
			throw new IllegalStateException(
			        "Illegal attempt to associate a user with the current context more than once");
		}
		MDC.put(CURRENT_USER_MDC_KEY, user.getName());
	}

	public String currentUserName() {
		return MDC.get(CURRENT_USER_MDC_KEY);
	}

	public void onUserExit(final Authentication user) {
		notNull(user, "Argument 'user' must not be null");
		if (MDC.get(CURRENT_USER_MDC_KEY) == null) {
			throw new IllegalStateException(
			        "Illegal attempt to disassociate a user from the current context when no user has been associated before");
		}
		if (!MDC.get(CURRENT_USER_MDC_KEY).equals(user.getName())) {
			throw new IllegalArgumentException("Supplied user ["
			        + user.getName() + "] is NOT the user ["
			        + MDC.get(CURRENT_USER_MDC_KEY)
			        + "] that has been associated with this context before");
		}
		MDC.remove(CURRENT_USER_MDC_KEY);
	}

	public void onMessageEnter(final GsmPdu message) {
		notNull(message, "Argument 'message' must not be null");
		if (MDC.get(MESSAGE_UUID_MDC_KEY) != null) {
			throw new IllegalStateException(
			        "Illegal attempt to associate a message with the current context more than once");
		}
		MDC.put(MESSAGE_UUID_MDC_KEY, message.getId().toString());
	}

	public UUID currentMessageUuid() {
		return MDC.get(MESSAGE_UUID_MDC_KEY) != null ? UUID.fromString(MDC
		        .get(MESSAGE_UUID_MDC_KEY)) : null;
	}

	public void onMessageExit(final GsmPdu message) {
		notNull(message, "Argument 'message' must not be null");
		if (MDC.get(MESSAGE_UUID_MDC_KEY) == null) {
			throw new IllegalStateException(
			        "Illegal attempt to disassociate a message from the current context when no message has been associated before");
		}
		if (!MDC.get(MESSAGE_UUID_MDC_KEY).equals(message.getId().toString())) {
			throw new IllegalArgumentException("Supplied message ["
			        + message.getId() + "] is NOT the message ["
			        + MDC.get(MESSAGE_UUID_MDC_KEY)
			        + "] that has been associated with this context before");
		}
		MDC.remove(MESSAGE_UUID_MDC_KEY);
	}
}
