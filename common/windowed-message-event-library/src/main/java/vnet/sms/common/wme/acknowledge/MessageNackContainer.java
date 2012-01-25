/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import vnet.sms.common.messages.Message;

/**
 * @author obergner
 * 
 */
public interface MessageNackContainer<ID extends Serializable, M extends Message>
        extends MessageAcknowledgementContainer<ID, M> {

	int getErrorKey();

	String getErrorDescription();
}
