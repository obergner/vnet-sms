package vnet.sms.common.spring.jmx.support;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.jmx.export.MBeanExportOperations;
import org.springframework.jmx.export.MBeanExporter;

import vnet.sms.common.spring.jmx.MBeanExportOperationsAware;

public class MBeanExportOperationsInjectorTest {

	@Test
	public final void assertThatPostProcessBeforeInitializationReturnsBeanPassedIn() {
		final Object beanPassedIn = new Object();

		final MBeanExportOperationsInjector objectUnderTest = new MBeanExportOperationsInjector();
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

		final MBeanExportOperationsInjector objectUnderTest = new MBeanExportOperationsInjector();
		final Object returnedBean = objectUnderTest
		        .postProcessAfterInitialization(beanPassedIn,
		                "assertThatPostProcessAfterInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessAfterInitialization() did not return the bean that has been passed in",
		        beanPassedIn, returnedBean);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatPostProcessBeforeInitializationRecognizesMissingApplicationContext() {
		final MBeanExportOperationsAware extensionPoint = new MBeanExportOperationsAware() {
			@Override
			public void setMBeanExportOperations(
			        final MBeanExportOperations mbeanExportOperations) {
			}
		};

		final MBeanExportOperationsInjector objectUnderTest = new MBeanExportOperationsInjector();

		objectUnderTest
		        .postProcessBeforeInitialization(extensionPoint,
		                "assertThatPostProcessBeforeInitializationRecognizesMissingApplicationContext");
	}

	@Test
	public final void assertThatPostProcessBeforeInitializationInjectsMBeanExportOperationsIntoMBeanExportOperationsAwareBean() {
		final AtomicReference<MBeanExportOperations> mbeanExportOperationsHolder = new AtomicReference<MBeanExportOperations>();
		final MBeanExportOperationsAware extensionPoint = new MBeanExportOperationsAware() {
			@Override
			public void setMBeanExportOperations(
			        final MBeanExportOperations mbeanExportOperations) {
				mbeanExportOperationsHolder.set(mbeanExportOperations);
			}
		};

		final StaticApplicationContext appContext = new StaticApplicationContext();
		appContext
		        .registerSingleton(
		                "assertThatPostProcessBeforeInitializationInjectsMBeanExportOperationsIntoMBeanExportOperationsAwareBean",
		                MBeanExporter.class);

		final MBeanExportOperationsInjector objectUnderTest = new MBeanExportOperationsInjector();
		objectUnderTest.setApplicationContext(appContext);

		objectUnderTest
		        .postProcessBeforeInitialization(
		                extensionPoint,
		                "assertThatPostProcessBeforeInitializationInjectsTransportProtocolPluginIntoTransportProtocolExtensionPoint");

		assertNotNull(
		        "postProcessBeforeInitialization() did NOT inject TransportProtocolPlugin into TransportProtocolExtensionPoint",
		        mbeanExportOperationsHolder.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetApplicationContextRejectsNullApplicationContext() {
		final MBeanExportOperationsInjector objectUnderTest = new MBeanExportOperationsInjector();
		objectUnderTest.setApplicationContext(null);
	}
}
