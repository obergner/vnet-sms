package vnet.sms.gateway.nettysupport.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.management.ManagementFactory;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.junit.Test;
import org.springframework.jmx.export.MBeanExporter;

import vnet.sms.common.messages.GsmPdu;
import vnet.sms.common.messages.LoginRequest;
import vnet.sms.common.messages.Msisdn;
import vnet.sms.common.messages.Sms;
import vnet.sms.common.wme.acknowledge.ReceivedSmsAckedContainer;
import vnet.sms.common.wme.acknowledge.ReceivedSmsAckedEvent;
import vnet.sms.common.wme.acknowledge.ReceivedSmsNackedContainer;
import vnet.sms.common.wme.acknowledge.ReceivedSmsNackedEvent;
import vnet.sms.common.wme.receive.SmsReceivedEvent;
import vnet.sms.gateway.nettysupport.test.ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.nettysupport.window.incoming.IncomingWindowStore;
import vnet.sms.gateway.nettytest.ChannelEventFilter;
import vnet.sms.gateway.nettytest.ChannelPipelineEmbedder;
import vnet.sms.gateway.nettytest.DefaultChannelPipelineEmbedder;

public class WindowingChannelHandlerTest {

	@Test
	public final void assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest()
	        throws Throwable {
		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        new IncomingWindowStore<Integer>(100, 1000, mbeanExporter));

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);

		embeddedPipeline
		        .receive(new LoginRequest(
		                "assertThatWindowedChannelHandlerCorrectlyPropagatesLoginRequest",
		                "secret"));
		final MessageEvent propagatedMessageEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull("WindowingChannelHandler did not propagate LoginRequest",
		        propagatedMessageEvent);
		assertEquals(
		        "WindowingChannelHandler converted LoginRequest to unexpected output",
		        LoginRequest.class, propagatedMessageEvent.getMessage()
		                .getClass());
	}

	@Test
	public final void assertThatWindowedChannelHandlerIssuesNoWindowForIncomingMessageAvailableEventIfNoWindowIsAvailable()
	        throws Throwable {
		final LoginRequest loginRequest = new LoginRequest(
		        "assertThatWindowedChannelHandlerIssuesNoWindowForIncomingMessageEventIfNoWindowIsAvailable",
		        "secret");

		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        new IncomingWindowStore<Integer>(1, 1, mbeanExporter));

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);

		embeddedPipeline.receive(loginRequest);
		embeddedPipeline.nextReceivedMessageEvent();
		embeddedPipeline.receive(loginRequest);

		final ChannelEvent propagatedMessageEvent = embeddedPipeline
		        .nextUpstreamChannelEvent(ChannelEventFilter.FILTERS
		                .ofType(NoWindowForIncomingMessageAvailableEvent.class));

		assertNotNull(
		        "WindowingChannelHandler did not propagate error event when rejecting incoming message due to no window available",
		        propagatedMessageEvent);
		assertEquals(
		        "WindowingChannelHandler propagated unexpected event when rejecting incoming message due to no window available",
		        NoWindowForIncomingMessageAvailableEvent.class,
		        propagatedMessageEvent.getClass());
	}

	@Test
	public final void assertThatWindowedChannelHandlerStoresReceivedSmsInIncomingWindowingStore()
	        throws Throwable {
		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());
		final IncomingWindowStore<Integer> incomingWindowStore = new IncomingWindowStore<Integer>(
		        100, 1000, mbeanExporter);
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        incomingWindowStore);

		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest);

		final Sms receivedSms = new Sms(new Msisdn("01686754432"), new Msisdn(
		        "01686754432"),
		        "assertThatWindowedChannelHandlerStoresReceivedSmsInIncomingWindowingStore");
		embeddedPipeline.receive(receivedSms);
		final MessageEvent propagatedMessageEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		assertNotNull("WindowingChannelHandler did not propagate Sms",
		        propagatedMessageEvent);
		assertEquals(
		        "WindowingChannelHandler converted Sms to unexpected output",
		        SmsReceivedEvent.class, propagatedMessageEvent.getClass());

		final Integer messageRef = (Integer) SmsReceivedEvent.class.cast(
		        propagatedMessageEvent).getMessageReference();
		final GsmPdu storedMessage = incomingWindowStore
		        .releaseWindow(messageRef);
		assertEquals(
		        "WindowingChannelHandler did not store received SMS in incoming windowing store but some other message",
		        receivedSms, storedMessage);
	}

	@Test
	public final void assertThatWindowedChannelHandlerReleasesSmsStoredInIncomingWindowingStoreWhenReceivingAnAck()
	        throws Throwable {
		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());
		final IncomingWindowStore<Integer> incomingWindowStore = new IncomingWindowStore<Integer>(
		        100, 1000, mbeanExporter);
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        incomingWindowStore);

		final SimpleChannelDownstreamHandler converterHandler = new SimpleChannelDownstreamHandler() {
			@Override
			public void writeRequested(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				final ReceivedSmsAckedEvent<Integer> ackedEvent = ReceivedSmsAckedEvent
				        .convert(e);
				ctx.sendDownstream(ackedEvent);
			}
		};
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest, converterHandler);

		final Sms receivedSms = new Sms(
		        new Msisdn("01686754432"),
		        new Msisdn("01686754432"),
		        "assertThatWindowedChannelHandlerReleasesSmsStoredInIncomingWindowingStoreWhenReceivingAnAck");
		embeddedPipeline.receive(receivedSms);

		assertEquals(
		        "WindowingChannelHandler should have stored received SMS in incoming windowing store",
		        1, incomingWindowStore.getCurrentMessageCount());

		final MessageEvent propagatedMessageEvent = embeddedPipeline
		        .nextReceivedMessageEvent();
		final Integer messageRef = (Integer) SmsReceivedEvent.class.cast(
		        propagatedMessageEvent).getMessageReference();
		embeddedPipeline.send(new ReceivedSmsAckedContainer<Integer>(
		        messageRef, 0, receivedSms));

		assertEquals(
		        "WindowingChannelHandler should have released received SMS when processing an Ack for that SMS",
		        0, incomingWindowStore.getCurrentMessageCount());
	}

	@Test
	public final void assertThatWindowedChannelHandlerPropagatesFailedToReleaseAcknowledgedMessageEventUpstreamIfReleasedMessagesDoNotMatch()
	        throws Throwable {
		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());
		final IncomingWindowStore<Integer> incomingWindowStore = new IncomingWindowStore<Integer>(
		        100, 1000, mbeanExporter);
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        incomingWindowStore);

		final SimpleChannelDownstreamHandler converterHandler = new SimpleChannelDownstreamHandler() {
			@Override
			public void writeRequested(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				final ReceivedSmsAckedEvent<Integer> ackedEvent = ReceivedSmsAckedEvent
				        .convert(e);
				ctx.sendDownstream(ackedEvent);
			}
		};
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest, converterHandler);

		final Sms smsToAcknowledge = new Sms(
		        new Msisdn("01686754432"),
		        new Msisdn("01686754432"),
		        "assertThatWindowedChannelHandlerPropagatesFailedToReleaseAcknowledgedMessageEventUpstreamIfReleasedMessagesDoNotMatch");
		embeddedPipeline.receive(smsToAcknowledge);

		assertEquals(
		        "WindowingChannelHandler should have stored received SMS in incoming windowing store",
		        1, incomingWindowStore.getCurrentMessageCount());

		final MessageEvent propagatedMessageEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		final Sms actuallyAcknowledgedSms = new Sms(new Msisdn("01686754432"),
		        new Msisdn("01686754432"), "actually");
		final Integer messageRef = (Integer) SmsReceivedEvent.class.cast(
		        propagatedMessageEvent).getMessageReference();
		embeddedPipeline.send(new ReceivedSmsAckedContainer<Integer>(
		        messageRef, 0, actuallyAcknowledgedSms));

		final ChannelEvent expectedFailedToReleaseEvent = embeddedPipeline
		        .nextUpstreamChannelEvent(ChannelEventFilter.FILTERS
		                .ofType(FailedToReleaseAcknowledgedMessageEvent.class));

		assertNotNull(
		        "WindowingChannelHandler should have sent a "
		                + FailedToReleaseAcknowledgedMessageEvent.class
		                        .getName()
		                + " upstream when realizing that the released SMS is not the formerly stored SMS",
		        expectedFailedToReleaseEvent);
	}

	@Test
	public final void assertThatWindowedChannelHandlerReleasesSmsStoredInIncomingWindowingStoreWhenReceivingANack()
	        throws Throwable {
		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());
		final IncomingWindowStore<Integer> incomingWindowStore = new IncomingWindowStore<Integer>(
		        100, 1000, mbeanExporter);
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        incomingWindowStore);

		final SimpleChannelDownstreamHandler converterHandler = new SimpleChannelDownstreamHandler() {
			@Override
			public void writeRequested(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				final ReceivedSmsNackedEvent<Integer> nackedEvent = ReceivedSmsNackedEvent
				        .convert(e);
				ctx.sendDownstream(nackedEvent);
			}
		};
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest, converterHandler);

		final Sms receivedSms = new Sms(
		        new Msisdn("01686754432"),
		        new Msisdn("01686754432"),
		        "assertThatWindowedChannelHandlerReleasesSmsStoredInIncomingWindowingStoreWhenReceivingANack");
		embeddedPipeline.receive(receivedSms);

		assertEquals(
		        "WindowingChannelHandler should have stored received SMS in incoming windowing store",
		        1, incomingWindowStore.getCurrentMessageCount());

		final MessageEvent propagatedMessageEvent = embeddedPipeline
		        .nextReceivedMessageEvent();
		final Integer messageRef = (Integer) SmsReceivedEvent.class.cast(
		        propagatedMessageEvent).getMessageReference();
		embeddedPipeline
		        .send(new ReceivedSmsNackedContainer<Integer>(
		                1,
		                "assertThatWindowedChannelHandlerReleasesSmsStoredInIncomingWindowingStoreWhenReceivingANack",
		                messageRef, 0, receivedSms));

		assertEquals(
		        "WindowingChannelHandler should have released received SMS when processing an Nack for that SMS",
		        0, incomingWindowStore.getCurrentMessageCount());
	}

	@Test
	public final void assertThatWindowedChannelHandlerPropagatesFailedToReleaseAcknowledgedMessageEventUpstreamIfMessageReferenceIsUnknown()
	        throws Throwable {
		final MBeanExporter mbeanExporter = new MBeanExporter();
		mbeanExporter.setServer(ManagementFactory.getPlatformMBeanServer());
		final IncomingWindowStore<Integer> incomingWindowStore = new IncomingWindowStore<Integer>(
		        100, 1000, mbeanExporter);
		final WindowingChannelHandler<Integer> objectUnderTest = new WindowingChannelHandler<Integer>(
		        incomingWindowStore);

		final SimpleChannelDownstreamHandler converterHandler = new SimpleChannelDownstreamHandler() {
			@Override
			public void writeRequested(final ChannelHandlerContext ctx,
			        final MessageEvent e) throws Exception {
				final ReceivedSmsAckedEvent<Integer> ackedEvent = ReceivedSmsAckedEvent
				        .convert(e);
				ctx.sendDownstream(ackedEvent);
			}
		};
		final ChannelPipelineEmbedder embeddedPipeline = new DefaultChannelPipelineEmbedder(
		        new ObjectSerializationTransportProtocolAdaptingUpstreamChannelHandler(),
		        objectUnderTest, converterHandler);

		final Sms smsToAcknowledge = new Sms(
		        new Msisdn("01686754432"),
		        new Msisdn("01686754432"),
		        "assertThatWindowedChannelHandlerPropagatesFailedToReleaseAcknowledgedMessageEventUpstreamIfMessageReferenceIsUnknown");
		embeddedPipeline.receive(smsToAcknowledge);

		assertEquals(
		        "WindowingChannelHandler should have stored received SMS in incoming windowing store",
		        1, incomingWindowStore.getCurrentMessageCount());

		final MessageEvent propagatedMessageEvent = embeddedPipeline
		        .nextReceivedMessageEvent();

		final Integer messageRef = (Integer) SmsReceivedEvent.class.cast(
		        propagatedMessageEvent).getMessageReference();
		embeddedPipeline.send(new ReceivedSmsAckedContainer<Integer>(
		        messageRef + 1, 0, smsToAcknowledge));

		final ChannelEvent expectedFailedToReleaseEvent = embeddedPipeline
		        .nextUpstreamChannelEvent(ChannelEventFilter.FILTERS
		                .ofType(FailedToReleaseAcknowledgedMessageEvent.class));

		assertNotNull(
		        "WindowingChannelHandler should have sent a "
		                + FailedToReleaseAcknowledgedMessageEvent.class
		                        .getName()
		                + " upstream when realizing that the supplied message reference is unknown",
		        expectedFailedToReleaseEvent);
	}
}
