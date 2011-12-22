/**
 * 
 */
package vnet.sms.gateway.server.framework.spi;

import static org.apache.commons.lang.Validate.notNull;

import java.io.Serializable;

import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;

/**
 * @author obergner
 * 
 */
public class DefaultTransportProtocolPlugin<ID extends Serializable, TP>
        implements TransportProtocolPlugin<ID, TP> {

	private final FrameDecoder	                                            frameDecoder;

	private final OneToOneDecoder	                                        decoder;

	private final OneToOneEncoder	                                        encoder;

	private final TransportProtocolAdaptingUpstreamChannelHandler<ID, TP>	pduToWindowedMessageEventConverter;

	private final TransportProtocolAdaptingDownstreamChannelHandler<ID, TP>	windowedMessageEventToPduConverter;

	/**
	 * @param frameDecoder
	 * @param decoder
	 * @param encoder
	 * @param pduToWindowedMessageEventConverter
	 * @param windowedMessageEventToPduConverter
	 */
	public DefaultTransportProtocolPlugin(
	        final FrameDecoder frameDecoder,
	        final OneToOneDecoder decoder,
	        final OneToOneEncoder encoder,
	        final TransportProtocolAdaptingUpstreamChannelHandler<ID, TP> pduToWindowedMesssageEventConverter,
	        final TransportProtocolAdaptingDownstreamChannelHandler<ID, TP> windowedMessageEventToPduConverter) {
		notNull(frameDecoder, "Argument 'frameDecoder' must not be null");
		notNull(encoder, "Argument 'encoder' must not be null");
		notNull(pduToWindowedMesssageEventConverter,
		        "Argument 'pduToWindowedMessageEventConverter' must not be null");
		notNull(windowedMessageEventToPduConverter,
		        "Argument 'windowedMessageEventToPduConverter' must not be null");
		this.frameDecoder = frameDecoder;
		this.decoder = decoder;
		this.encoder = encoder;
		this.pduToWindowedMessageEventConverter = pduToWindowedMesssageEventConverter;
		this.windowedMessageEventToPduConverter = windowedMessageEventToPduConverter;
	}

	/**
	 * @return the frameDecoder
	 */
	@Override
	public final FrameDecoder getFrameDecoder() {
		return this.frameDecoder;
	}

	/**
	 * @return the decoder
	 */
	@Override
	public final OneToOneDecoder getDecoder() {
		return this.decoder;
	}

	/**
	 * @return the encoder
	 */
	@Override
	public final OneToOneEncoder getEncoder() {
		return this.encoder;
	}

	/**
	 * @return the pduToWindowedMessageEventConverter
	 */
	@Override
	public final TransportProtocolAdaptingUpstreamChannelHandler<ID, TP> getPduToWindowedMessageEventConverter() {
		return this.pduToWindowedMessageEventConverter;
	}

	/**
	 * @return the windowedMessageEventToPduConverter
	 */
	@Override
	public final TransportProtocolAdaptingDownstreamChannelHandler<ID, TP> getWindowedMessageEventToPduConverter() {
		return this.windowedMessageEventToPduConverter;
	}

	@Override
	public String toString() {
		return "DefaultTransportProtocolPlugin@" + hashCode()
		        + " [frameDecoder: " + this.frameDecoder + "|decoder: "
		        + this.decoder + "|encoder: " + this.encoder
		        + "|pduToWindowedMessageEventConverter: "
		        + this.pduToWindowedMessageEventConverter
		        + "|windowedMessageEventToPduConverter: "
		        + this.windowedMessageEventToPduConverter + "]";
	}
}
