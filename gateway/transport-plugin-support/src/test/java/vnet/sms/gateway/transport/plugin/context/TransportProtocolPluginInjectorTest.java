package vnet.sms.gateway.transport.plugin.context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;

import vnet.sms.gateway.transport.plugin.TransportProtocolExtensionPoint;
import vnet.sms.gateway.transport.spi.DefaultTransportProtocolPlugin;
import vnet.sms.gateway.transport.spi.TransportProtocolPlugin;
import vnet.sms.gateway.transports.serialization.MonotonicallyIncreasingMessageReferenceGenerator;
import vnet.sms.gateway.transports.serialization.ReferenceableMessageContainer;
import vnet.sms.gateway.transports.serialization.incoming.SerializationTransportProtocolAdaptingUpstreamChannelHandler;
import vnet.sms.gateway.transports.serialization.outgoing.SerializationTransportProtocolAdaptingDownstreamChannelHandler;

public class TransportProtocolPluginInjectorTest {

	@Test
	public final void assertThatPostProcessBeforeInitializationReturnsBeanPassedIn() {
		final Object beanPassedIn = new Object();

		final TransportProtocolPluginInjector objectUnderTest = new TransportProtocolPluginInjector();
		final Object returnedBean = objectUnderTest
		        .postProcessBeforeInitialization(beanPassedIn,
		                "assertThatPostProcessBeforeInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessBeforeInitialization() did not return the bean that has been passed in",
		        beanPassedIn, returnedBean);
	}

	@Test
	public final void assertThatPostProcessAfterInitializationReturnsBeanPassedIn() {
		final Object beanPassedIn = new Object();

		final TransportProtocolPluginInjector objectUnderTest = new TransportProtocolPluginInjector();
		final Object returnedBean = objectUnderTest
		        .postProcessAfterInitialization(beanPassedIn,
		                "assertThatPostProcessAfterInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessAfterInitialization() did not return the bean that has been passed in",
		        beanPassedIn, returnedBean);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatPostProcessAfterInitializationRecognizesMissingApplicationContext() {
		final AtomicReference<TransportProtocolPlugin<Integer, ReferenceableMessageContainer>> transportProtocolPluginHolder = new AtomicReference<TransportProtocolPlugin<Integer, ReferenceableMessageContainer>>();
		final TransportProtocolExtensionPoint<Integer, ReferenceableMessageContainer> extensionPoint = new TransportProtocolExtensionPoint<Integer, ReferenceableMessageContainer>() {
			@Override
			public void plugin(
			        final TransportProtocolPlugin<Integer, ReferenceableMessageContainer> plugin) {
				transportProtocolPluginHolder.set(plugin);
			}
		};

		final TransportProtocolPluginInjector objectUnderTest = new TransportProtocolPluginInjector();

		objectUnderTest
		        .postProcessAfterInitialization(
		                extensionPoint,
		                "assertThatPostProcessAfterInitializationInjectsTransportProtocolPluginIntoTransportProtocolExtensionPoint");
	}

	@Test
	public final void assertThatPostProcessAfterInitializationInjectsTransportProtocolPluginIntoTransportProtocolExtensionPoint() {
		final AtomicReference<TransportProtocolPlugin<Integer, ReferenceableMessageContainer>> transportProtocolPluginHolder = new AtomicReference<TransportProtocolPlugin<Integer, ReferenceableMessageContainer>>();
		final TransportProtocolExtensionPoint<Integer, ReferenceableMessageContainer> extensionPoint = new TransportProtocolExtensionPoint<Integer, ReferenceableMessageContainer>() {
			@Override
			public void plugin(
			        final TransportProtocolPlugin<Integer, ReferenceableMessageContainer> plugin) {
				transportProtocolPluginHolder.set(plugin);
			}
		};

		final StaticApplicationContext appContext = new StaticApplicationContext();
		appContext
		        .registerSingleton(
		                "assertThatPostProcessAfterInitializationInjectsTransportProtocolPluginIntoTransportProtocolExtensionPoint",
		                TestTransportProtocolPlugin.class);

		final TransportProtocolPluginInjector objectUnderTest = new TransportProtocolPluginInjector();
		objectUnderTest.setApplicationContext(appContext);

		final TransportProtocolExtensionPoint<Integer, ReferenceableMessageContainer> returnedBean = (TransportProtocolExtensionPoint<Integer, ReferenceableMessageContainer>) objectUnderTest
		        .postProcessAfterInitialization(
		                extensionPoint,
		                "assertThatPostProcessAfterInitializationInjectsTransportProtocolPluginIntoTransportProtocolExtensionPoint");

		assertNotNull(
		        "postProcessAfterInitialization() did NOT inject TransportProtocolPlugin into TransportProtocolExtensionPoint",
		        transportProtocolPluginHolder.get());
	}

	private static class TestTransportProtocolPlugin
	        extends
	        DefaultTransportProtocolPlugin<Integer, ReferenceableMessageContainer> {

		TestTransportProtocolPlugin() {
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

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetApplicationContextRejectsNullApplicationContext() {
		final TransportProtocolPluginInjector objectUnderTest = new TransportProtocolPluginInjector();
		objectUnderTest.setApplicationContext(null);
	}
}
