package vnet.sms.gateway.transports.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.gateway.nettysupport.transport.incoming.TransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.transport.outgoing.TransportProtocolAdaptingDownstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.spi.MessageReferenceGenerator;
import vnet.sms.gateway.transport.spi.TransportProtocolPlugin;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:META-INF/module/module-context.xml")
public class SerializationTransportPluginContextIT {

	@Autowired
	private TransportProtocolPlugin<Integer, ReferenceableMessageContainer>	objectUnderTest;

	@Test
	public final void assertThatExportedTransportProtocolPluginExportsReferenceablMessageContainerAsPduType() {
		final Class<?> exportedPduType = this.objectUnderTest.getPduType();

		assertEquals(
		        "Exported transport plugin should export ReferenceableMessageContainer as PduType",
		        ReferenceableMessageContainer.class, exportedPduType);
	}

	@Test
	public final void assertThatExportedTransportProtocolPluginExportsObjectDecoderAsFrameDecoder() {
		final FrameDecoder exportedFrameDecoder = this.objectUnderTest
		        .getFrameDecoder();

		assertEquals(
		        "Exported transport plugin should export ObjectDecoder as FrameDecoder",
		        ObjectDecoder.class, exportedFrameDecoder.getClass());
	}

	@Test
	public final void assertThatExportedTransportProtocolPluginExportsNoDecoder() {
		final OneToOneDecoder exportedDecoder = this.objectUnderTest
		        .getDecoder();

		assertNull("Exported transport plugin should export NO decoder",
		        exportedDecoder);
	}

	@Test
	public final void assertThatExportedTransportProtocolPluginExportsObjectEncoderAsEncoder() {
		final OneToOneEncoder exportedEncoder = this.objectUnderTest
		        .getEncoder();

		assertEquals(
		        "Exported transport plugin should export ObjectEncoder as encoder",
		        ObjectEncoder.class, exportedEncoder.getClass());
	}

	@Test
	public final void assertThatExportedTransportProtocolPluginExportsSerializationTransportProtocolAdaptingUpstreamChannelHandler() {
		final TransportProtocolAdaptingUpstreamChannelHandler<Integer, ReferenceableMessageContainer> exportedUpstreamConverter = this.objectUnderTest
		        .getPduToWindowedMessageEventConverter();

		assertEquals(
		        "Exported transport plugin should export SerializationTransportProtocolAdaptingUpstreamChannelHandler",
		        SerializationTransportProtocolAdaptingUpstreamChannelHandler.class,
		        exportedUpstreamConverter.getClass());
	}

	@Test
	public final void assertThatExportedTransportProtocolPluginExportsSerializationTransportProtocolAdaptingDownstreamChannelHandler() {
		final TransportProtocolAdaptingDownstreamChannelHandler<Integer, ReferenceableMessageContainer> exportedDownstreamConverter = this.objectUnderTest
		        .getWindowedMessageEventToPduConverter();

		assertEquals(
		        "Exported transport plugin should export SerializationTransportProtocolAdaptingUpstreamChannelHandler",
		        SerializationTransportProtocolAdaptingDownstreamChannelHandler.class,
		        exportedDownstreamConverter.getClass());
	}

	@Test
	public final void assertThatExportedTransportProtocolPluginExportsMonotonicallyIncreasingMessageReferenceGenerator() {
		final MessageReferenceGenerator<Integer> exportedMessageRefGen = this.objectUnderTest
		        .getMessageReferenceGenerator();

		assertEquals(
		        "Exported transport plugin should export MonotonicallyIncreasingMessageReferenceGenerator",
		        MonotonicallyIncreasingMessageReferenceGenerator.class,
		        exportedMessageRefGen.getClass());
	}
}
