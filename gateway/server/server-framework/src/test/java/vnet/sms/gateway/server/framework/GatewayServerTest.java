package vnet.sms.gateway.server.framework;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;

import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import vnet.sms.gateway.server.framework.internal.channel.GatewayServerChannelPipelineFactory;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;
import vnet.sms.gateway.server.framework.test.AcceptAllAuthenticationManager;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;

public class GatewayServerTest extends AbstractGatewayServerTest {

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullDescription() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(null,
		        "assertThatConstructorRejectsNullDescription",
		        new LocalAddress("assertThatConstructorRejectsNullInstanceId"),
		        new DefaultLocalServerChannelFactory(),
		        createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@SuppressWarnings("serial")
	private static final class TestGatewayServerDescription extends
	        GatewayServerDescription {

		public TestGatewayServerDescription() {
			super("Test", 1, 0, 0, "BETA", 15);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullInstanceId() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(), null, new LocalAddress(
		                "assertThatConstructorRejectsNullInstanceId"),
		        new DefaultLocalServerChannelFactory(),
		        createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsEmptyInstanceId() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(), "", new LocalAddress(
		                "assertThatConstructorRejectsEmptyInstanceId"),
		        new DefaultLocalServerChannelFactory(),
		        createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullSocketAddress() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatConstructorRejectsNullSocketAddress", null,
		        new DefaultLocalServerChannelFactory(),
		        createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullServerChannelFactory() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatConstructorRejectsNullServerChannelFactory",
		        new LocalAddress(
		                "assertThatConstructorRejectsNullServerChannelFactory"),
		        null, createNiceMock(GatewayServerChannelPipelineFactory.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatConstructorRejectsNullServerChannelPipelineFactory() {
		new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatConstructorRejectsNullServerChannelFactory",
		        new LocalAddress(
		                "assertThatConstructorRejectsNullServerChannelFactory"),
		        new DefaultLocalServerChannelFactory(), null);
	}

	@Test
	public final void assertThatStartPromotesGatewayServerToStateRunning()
	        throws Exception {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatStartPromotesGatewayServerToStateRunning", 10, 2000,
		        2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatStartPromotesGatewayServerToStateRunning",
		        new LocalAddress(
		                "assertThatStartPromotesGatewayServerToStateRunning"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		objectUnderTest.start();

		assertEquals(
		        "start() did not promote GatewayServer into state RUNNING",
		        ServerStatus.RUNNING, objectUnderTest.getCurrentStatus());

		objectUnderTest.stop();
	}

	@Test
	public final void assertThatStopPromotesGatewayServerToStateStopped()
	        throws Exception {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatStopPromotesGatewayServerToStateStopped", 10, 2000,
		        2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatStopPromotesGatewayServerToStateStopped",
		        new LocalAddress(
		                "assertThatStopPromotesGatewayServerToStateStopped"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		objectUnderTest.start();
		objectUnderTest.stop();

		assertEquals("stop() did not promote GatewayServer into state STOPPED",
		        ServerStatus.STOPPED, objectUnderTest.getCurrentStatus());
	}

	@Test
	public final void assertThatGetCurrentStateInitiallyReturnsStateSTOPPED() {
		final JmsTemplate jmsTemplate = newJmsTemplate();
		final GatewayServerChannelPipelineFactory<Integer, ReferenceableMessageContainer> pipelineFactory = newGatewayServerChannelPipelineFactory(
		        "assertThatGetCurrentStateInitiallyReturnsStateSTOPPED", 10,
		        2000, 2000, 5, 30000, new AcceptAllAuthenticationManager(),
		        jmsTemplate);
		final GatewayServer<Integer, ReferenceableMessageContainer> objectUnderTest = new GatewayServer<Integer, ReferenceableMessageContainer>(
		        new TestGatewayServerDescription(),
		        "assertThatGetCurrentStateInitiallyReturnsStateSTOPPED",
		        new LocalAddress(
		                "assertThatGetCurrentStateInitiallyReturnsStateSTOPPED"),
		        new DefaultLocalServerChannelFactory(), pipelineFactory);

		assertEquals("getCurrentState() did not return STOPPED after creation",
		        ServerStatus.STOPPED, objectUnderTest.getCurrentStatus());
	}
}
