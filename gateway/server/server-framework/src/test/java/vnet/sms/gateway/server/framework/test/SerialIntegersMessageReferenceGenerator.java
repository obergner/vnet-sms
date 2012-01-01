package vnet.sms.gateway.server.framework.test;

import java.util.concurrent.atomic.AtomicInteger;

import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;

public class SerialIntegersMessageReferenceGenerator implements
        MessageReferenceGenerator<Integer> {

	private final AtomicInteger	nextReference	= new AtomicInteger();

	@Override
	public Integer nextMessageReference() {
		return this.nextReference.incrementAndGet();
	}
}
