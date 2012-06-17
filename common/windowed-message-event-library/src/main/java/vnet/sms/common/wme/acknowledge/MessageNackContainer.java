/**
 * 
 */
package vnet.sms.common.wme.acknowledge;

import java.io.Serializable;

import vnet.sms.common.messages.GsmPdu;

/**
 * @author obergner
 * 
 */
public interface MessageNackContainer<ID extends Serializable, M extends GsmPdu>
        extends MessageAcknowledgementContainer<ID, M> {

	int getErrorKey();

	String getErrorDescription();
}
