/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.net.SocketAddress;

/**
 * @author obergner
 * 
 */
public class LoginRequest extends Message {

	private static final long	serialVersionUID	= 8063236854497731334L;

	private final String	    username;

	private final String	    password;

	private final SocketAddress	sender;

	private final SocketAddress	receiver;

	public LoginRequest(final String username, final String password,
	        final SocketAddress sender, final SocketAddress receiver) {
		notEmpty(username, "Argument 'username' must not be empty");
		notEmpty(password, "Argument 'password' must not be empty");
		notNull(sender, "Argument 'sender' must not be null");
		notNull(receiver, "Argument 'receiver' must not be null");
		this.username = username;
		this.password = password;
		this.sender = sender;
		this.receiver = receiver;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public SocketAddress getSender() {
		return this.sender;
	}

	public SocketAddress getReceiver() {
		return this.receiver;
	}

	@Override
	public String toString() {
		return "LoginRequest@" + this.hashCode() + " [ID: " + getId()
		        + "|creationTimestamp: " + getCreationTimestamp()
		        + "|username: " + this.username
		        + "|password: [PROTECTED]|sender: " + this.sender
		        + "|receiver: " + this.receiver + "]";
	}
}
