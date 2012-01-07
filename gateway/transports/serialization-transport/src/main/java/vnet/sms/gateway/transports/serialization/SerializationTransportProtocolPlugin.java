/**
 * 
 */
package vnet.sms.gateway.transports.serialization;

import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import vnet.sms.gateway.transport.spi.DefaultTransportProtocolPlugin;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

/**
 * @author obergner
 * 
 */
public class SerializationTransportProtocolPlugin extends
        DefaultTransportProtocolPlugin<Integer, ReferenceableMessageContainer> {

	public SerializationTransportProtocolPlugin() {
		super(
		        ReferenceableMessageContainer.class,
		        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		        null,
		        new ObjectEncoder(),
		        new SerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        new SerializationTransportProtocolAdaptingDownstreamChannelHandler(),
		        new MonotonicallyIncreasingMessageReferenceGenerator());
	}
}
