/**
 * 
 */
package vnet.sms.gateway.transports.serialization;

import java.util.concurrent.atomic.AtomicInteger;

import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;

/**
 * @author obergner
 * 
 */
public class MonotonicallyIncreasingMessageReferenceGenerator implements
        MessageReferenceGenerator<Integer> {

	private final AtomicInteger	nextId	= new AtomicInteger(0);

	/**
	 * @see vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator#nextMessageReference()
	 */
	@Override
	public Integer nextMessageReference() {
		return this.nextId.incrementAndGet();
	}

	@Override
	public String toString() {
		return "MonotonicallyIncreasingMessageReferenceGenerator@"
		        + this.hashCode() + "[nextId: " + this.nextId + "]";
	}
}
