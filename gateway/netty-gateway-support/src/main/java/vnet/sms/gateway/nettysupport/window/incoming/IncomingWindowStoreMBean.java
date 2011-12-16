package vnet.sms.gateway.nettysupport.window.incoming;

/**
 * @author obergner
 * 
 */
public interface IncomingWindowStoreMBean {

	String getOwnerUid();

	int getMaximumCapacity();

	int getCurrentMessageCount();
}
