package vnet.sms.gateway.nettysupport.transport.incoming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.handler.codec.embedder.CodecEmbedderException;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.After;
import org.junit.Test;

import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.LoginResponse;
import vnet.sms.common.messages.Message;
import vnet.sms.common.messages.PingRequest;
import vnet.sms.common.messages.PingResponse;
import vnet.sms.common.messages.Sms;
import vnet.sms.gateway.nettysupport.monitor.DefaultChannelMonitorCallback;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;

public class TransportProtocolAdaptingUpstreamChannelHandlerTest {

	private static class SimpleChannelMonitorCallback extends
	        DefaultChannelMonitorCallback {

		final AtomicLong	numberOfReceivedLoginRequests	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedLoginResponses	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedPingRequests	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedPingResponses	= new AtomicLong(0);

		final AtomicLong	numberOfReceivedSms		       = new AtomicLong(0);

		void reset() {
			this.numberOfReceivedSms.set(0);
			this.numberOfReceivedLoginRequests.set(0);
			this.numberOfReceivedLoginResponses.set(0);
			this.numberOfReceivedPingRequests.set(0);
			this.numberOfReceivedPingResponses.set(0);
		}

		@Override
		public void loginRequestReceived() {
			this.numberOfReceivedLoginRequests.incrementAndGet();
		}

		@Override
		public void loginResponseReceived() {
			this.numberOfReceivedLoginResponses.incrementAndGet();
		}

		@Override
		public void pingRequestReceived() {
			this.numberOfReceivedPingRequests.incrementAndGet();
		}

		@Override
		public void pingResponseReceived() {
			this.numberOfReceivedPingResponses.incrementAndGet();
		}

		@Override
		public void smsReceived() {
			this.numberOfReceivedSms.incrementAndGet();
		}
	}

	private final SimpleChannelMonitorCallback	                                    monitorCallback	= new SimpleChannelMonitorCallback();

	private final TransportProtocolAdaptingUpstreamChannelHandler<Integer, Message>	objectUnderTest	= new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(
	                                                                                                        this.monitorCallback);

	@After
	public void resetMonitor() {
		this.monitorCallback.reset();
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(new LoginRequest(
		                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                "secret", new InetSocketAddress(1),
		                new InetSocketAddress(1)));
		final Message convertedPdu = embeddedPipeline.poll();
		embeddedPipeline.finish();

		assertNotNull(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted LoginRequest to null output",
		        convertedPdu);
		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted LoginRequest to unexpected output",
		        LoginRequest.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginResponse() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(LoginResponse
		                .accept(new LoginRequest(
		                        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                        "secret", new InetSocketAddress(1),
		                        new InetSocketAddress(1))));
		final Message convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted LoginResponse to null output",
		        convertedPdu);
		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted LoginResponse to unexpected output",
		        LoginResponse.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingRequest() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline.offer(new PingRequest(new InetSocketAddress(1),
		        new InetSocketAddress(1)));
		final Message convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted PingRequest to null output",
		        convertedPdu);
		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted PingRequest to unexpected output",
		        PingRequest.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToPingResponse() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline.offer(PingResponse.accept(new PingRequest(
		        new InetSocketAddress(1), new InetSocketAddress(1))));
		final Message convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted PingResponse to null output",
		        convertedPdu);
		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted PingResponse to unexpected output",
		        PingResponse.class, convertedPdu.getClass());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline.offer(new Sms(
		        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms",
		        new InetSocketAddress(0), new InetSocketAddress(1)));
		final Message convertedPdu = embeddedPipeline.poll();

		assertNotNull(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted Sms to null output",
		        convertedPdu);
		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler converted Sms to unexpected output",
		        Sms.class, convertedPdu.getClass());
	}

	@Test(expected = CodecEmbedderException.class)
	public final void assertThatTransportProtocolAdapterRejectsUnknownPduType() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);
		final Object unknownPdu = new Object();

		embeddedPipeline.offer(unknownPdu);
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyUpdatesNumberOfReceivedLoginRequests() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(new LoginRequest(
		                "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                "secret", new InetSocketAddress(1),
		                new InetSocketAddress(1)));

		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler did not correctly count number of received login requests",
		        1L, this.monitorCallback.numberOfReceivedLoginRequests.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedLoginResponses() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline
		        .offer(LoginResponse
		                .accept(new LoginRequest(
		                        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToLoginRequest",
		                        "secret", new InetSocketAddress(1),
		                        new InetSocketAddress(1))));

		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler did not correctly count number of received login responses",
		        1L, this.monitorCallback.numberOfReceivedLoginResponses.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedPingRequests() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline.offer(new PingRequest(new InetSocketAddress(1),
		        new InetSocketAddress(1)));

		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler did not correctly count number of received ping requests",
		        1L, this.monitorCallback.numberOfReceivedPingRequests.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedPingResponses() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline.offer(PingResponse.accept(new PingRequest(
		        new InetSocketAddress(1), new InetSocketAddress(1))));

		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler did not correctly count number of received ping responses",
		        1L, this.monitorCallback.numberOfReceivedPingResponses.get());
	}

	@Test
	public final void assertThatTransportProtocolAdapterCorrectlyCountsNumberOfReceivedSms() {
		final DecoderEmbedder<Message> embeddedPipeline = new DecoderEmbedder<Message>(
		        this.objectUnderTest);

		embeddedPipeline.offer(new Sms(
		        "assertThatTransportProtocolAdapterCorrectlyConvertsPduToSms",
		        new InetSocketAddress(0), new InetSocketAddress(1)));

		assertEquals(
		        "TransportProtocolAdaptingUpstreamChannelHandler did not correctly count number of received sms",
		        1L, this.monitorCallback.numberOfReceivedSms.get());
	}
}
