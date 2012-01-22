/**
 * 
 */
package vnet.sms.gateway.server.framework.description;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;

import vnet.sms.gateway.server.framework.GatewayServerDescriptionAware;
import vnet.sms.gateway.server.framework.spi.GatewayServerDescription;

/**
 * @author obergner
 * 
 */
public class GatewayServerDescriptionInjectorTest {

	@Test
	public final void assertThatPostProcessBeforeInitializationReturnsBeanPassedIn() {
		final Object beanPassedIn = new Object();

		final GatewayServerDescriptionInjector objectUnderTest = new GatewayServerDescriptionInjector();
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

		final GatewayServerDescriptionInjector objectUnderTest = new GatewayServerDescriptionInjector();
		final Object returnedBean = objectUnderTest
		        .postProcessAfterInitialization(beanPassedIn,
		                "assertThatPostProcessAfterInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessAfterInitialization() did not return the bean that has been passed in",
		        beanPassedIn, returnedBean);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatPostProcessBeforeInitializationRecognizesMissingApplicationContext() {
		final AtomicReference<GatewayServerDescription> gatewayServerDescriptionHolder = new AtomicReference<GatewayServerDescription>();
		final GatewayServerDescriptionAware extensionPoint = new GatewayServerDescriptionAware() {
			@Override
			public void setGatewayServerDescription(
			        final GatewayServerDescription description) {
				gatewayServerDescriptionHolder.set(description);
			}
		};

		final GatewayServerDescriptionInjector objectUnderTest = new GatewayServerDescriptionInjector();

		objectUnderTest
		        .postProcessBeforeInitialization(extensionPoint,
		                "assertThatPostProcessBeforeInitializationRecognizesMissingApplicationContext");
	}

	@Test
	public final void assertThatPostProcessBeforeInitializationInjectsGatewayServerDescriptionIntoGatewayServerDescriptionAwareBean() {
		final AtomicReference<GatewayServerDescription> descriptionHolder = new AtomicReference<GatewayServerDescription>();
		final GatewayServerDescriptionAware extensionPoint = new GatewayServerDescriptionAware() {
			@Override
			public void setGatewayServerDescription(
			        final GatewayServerDescription description) {
				descriptionHolder.set(description);
			}
		};

		final StaticApplicationContext appContext = new StaticApplicationContext();
		appContext
		        .registerSingleton(
		                "assertThatPostProcessBeforeInitializationInjectsGatewayServerDescriptionIntoGatewayServerDescriptionAwareBean",
		                TestGatewayServerDescription.class);

		final GatewayServerDescriptionInjector objectUnderTest = new GatewayServerDescriptionInjector();
		objectUnderTest.setApplicationContext(appContext);

		objectUnderTest
		        .postProcessBeforeInitialization(
		                extensionPoint,
		                "assertThatPostProcessBeforeInitializationInjectsGatewayServerDescriptionIntoGatewayServerDescriptionAwareBean");

		assertNotNull(
		        "postProcessBeforeInitialization() did NOT inject GatewayServerDescription into GatewayServerDescriptionAware bean",
		        descriptionHolder.get());
	}

	@SuppressWarnings("serial")
	private static final class TestGatewayServerDescription extends
	        GatewayServerDescription {

		public TestGatewayServerDescription() {
			super("Test", 1, 0, 0, "BETA", 15);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetApplicationContextRejectsNullApplicationContext() {
		final GatewayServerDescriptionInjector objectUnderTest = new GatewayServerDescriptionInjector();
		objectUnderTest.setApplicationContext(null);
	}
}
