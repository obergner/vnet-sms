/**
 * 
 */
package vnet.sms.common.wme.send;

import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.MessageType;

/**
 * @author obergner
 * 
 */
public final class SendSmsContainer extends AbstractMessageSendContainer<Sms> {

	/**
     * 
     */
	private static final long	serialVersionUID	= -6177476897774774517L;

	public SendSmsContainer(final Sms message) {
		super(MessageType.SEND_SMS, message);
	}

	@Override
	public String toString() {
		return "SendSmsContainer@" + this.hashCode() + "[messageType: "
		        + this.getMessageType() + "|message: " + this.getMessage()
		        + "]";
	}
}
