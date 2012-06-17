/**
 * 
 */
package vnet.sms.gateway.nettysupport.window;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.wme.acknowledge.ReceivedMessageAcknowledgedEvent;
import vnet.sms.gateway.nettysupport.AbstractMessageProcessingEvent;

/**
 * @author obergner
 * 
 */
public class FailedToReleaseAcknowledgedMessageEvent<ID extends Serializable, M extends GsmPdu>
        extends AbstractMessageProcessingEvent<M> {

	public static final <I extends Serializable, N extends GsmPdu> FailedToReleaseAcknowledgedMessageEvent<I, N> fail(
	        final ReceivedMessageAcknowledgedEvent<I, N> e,
	        final Exception error) {
		notNull(e, "Argument 'e' must not be null");
		notNull(error, "Argument 'error' must not be null");
		return new FailedToReleaseAcknowledgedMessageEvent<I, N>(e, error);
	}

	private final Exception	error;

	private FailedToReleaseAcknowledgedMessageEvent(
	        final ReceivedMessageAcknowledgedEvent<ID, M> e,
	        final Exception error) {
		super(e.getChannel(), e.getMessage(), e.getRemoteAddress());
		this.error = error;
	}

	/**
	 * @return the error
	 */
	public final Exception getError() {
		return this.error;
	}

	@Override
	public String toString() {
		return "FailedToReleaseAcknowledgedMessageEvent@" + this.hashCode()
		        + "[id: " + this.getId() + "|channel: " + this.getChannel()
		        + "|creationTime: " + this.getCreationTime() + "|message: "
		        + this.getMessage() + "|remoteAddress: "
		        + this.getRemoteAddress() + "|error: "
		        + this.error.getMessage() + "]";
	}
}
