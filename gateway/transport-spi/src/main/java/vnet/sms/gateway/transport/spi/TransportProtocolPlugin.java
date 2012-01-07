/**
 * 
 */
package vnet.sms.gateway.transport.spi;

import java.io.Serializable;

import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;

/**
 * @author obergner
 * 
 */
public interface TransportProtocolPlugin<ID extends Serializable, TP> {

	Class<TP> getPduType();

	FrameDecoder getFrameDecoder();

	OneToOneDecoder getDecoder();

	OneToOneEncoder getEncoder();

	TransportProtocolAdaptingUpstreamChannelHandler<ID, TP> getPduToWindowedMessageEventConverter();

	TransportProtocolAdaptingDownstreamChannelHandler<ID, TP> getWindowedMessageEventToPduConverter();

	MessageReferenceGenerator<ID> getMessageReferenceGenerator();
}
