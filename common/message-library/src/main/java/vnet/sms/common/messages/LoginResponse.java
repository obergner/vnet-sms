/**
 * 
 */
package vnet.sms.common.messages;

import static org.apache.commons.lang.Validate.notNull;

/**
 * @author obergner
 * 
 */
public class LoginResponse extends Message {

	private static final long	serialVersionUID	= -3756368928116010958L;

	public static final LoginResponse accept(final LoginRequest loginRequest) {
		return new LoginResponse(Acknowledgement.ack(), loginRequest);
	}

	public static final LoginResponse reject(final LoginRequest loginRequest) {
		return new LoginResponse(Acknowledgement.nack(), loginRequest);
	}

	private final Acknowledgement	ack;

	private final LoginRequest	  loginRequest;

	private LoginResponse(final Acknowledgement ack,
	        final LoginRequest loginRequest) {
		super(loginRequest.getReceiver(), loginRequest.getSender());
		notNull(ack, "Argument 'ack' must not be null");
		notNull(loginRequest, "Argument 'loginRequest' must not be null");
		this.ack = ack;
		this.loginRequest = loginRequest;
	}

	public boolean loginSucceeded() {
		return this.ack.is(Acknowledgement.Status.ACK);
	}

	public LoginRequest getRequest() {
		return this.loginRequest;
	}

	@Override
	public String toString() {
		return "LoginResponse@" + this.hashCode() + " [ID: " + this.getId()
		        + "|creationTimestamp: " + this.getCreationTimestamp()
		        + "|ack: " + this.ack + "|loginRequest: " + this.loginRequest
		        + "]";
	}
}
