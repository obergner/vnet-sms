/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notEmpty;

import java.net.SocketAddress;
import java.util.UUID;

/**
 * @author obergner
 * 
 */
public class LoginRequest extends Message {

	private static final long	serialVersionUID	= 8063236854497731334L;

	private final String	  username;

	private final String	  password;

	public LoginRequest(final UUID id, final long creationTimestamp,
	        final String username, final String password,
	        final SocketAddress sender, final SocketAddress receiver) {
		super(id, creationTimestamp, sender, receiver);
		notEmpty(username, "Argument 'username' must not be empty");
		notEmpty(password, "Argument 'password' must not be empty");
		this.username = username;
		this.password = password;
	}

	public LoginRequest(final String username, final String password,
	        final SocketAddress sender, final SocketAddress receiver) {
		super(sender, receiver);
		notEmpty(username, "Argument 'username' must not be empty");
		notEmpty(password, "Argument 'password' must not be empty");
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	@Override
	public String toString() {
		return "LoginRequest@" + this.hashCode() + "[ID: " + getId()
		        + "|creationTimestamp: " + getCreationTimestamp()
		        + "|username: " + this.username
		        + "|password: [PROTECTED]|sender: " + getSender()
		        + "|receiver: " + getReceiver() + "]";
	}
}
