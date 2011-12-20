package vnet.sms.gateway.nettysupport.window.incoming;

/**
 * @author obergner
 * 
 */
public interface IncomingWindowStoreMBean {

	int getMaximumCapacity();

	int getCurrentMessageCount();
}
