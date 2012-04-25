package vnet.sms.common.shell.clamshellspring.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import vnet.sms.common.shell.clamshellspring.ClamshellLauncher;
import vnet.sms.common.shell.clamshellspring.ClamshellLauncher.Factory;
import vnet.sms.common.shell.clamshellspring.ClamshellLauncherFactoryAware;

public class ClamshellLauncherFactoryInjectorTest {

	@Test
	public final void assertThatPostProcessBeforeInitializationReturnsBeanPassedIn() {
		final ClamshellLauncherFactoryInjector objectUnderTest = new ClamshellLauncherFactoryInjector();
		final Object beanPassedIn = new Object();

		final Object beanReturned = objectUnderTest
		        .postProcessBeforeInitialization(beanPassedIn,
		                "assertThatPostProcessBeforeInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessBeforeInitialization(...) should have returned the bean passed in, yet it didn't",
		        beanPassedIn, beanReturned);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatPostProcessBeforeInitializationRecognizesThatNoClamshellLauncherFactoryHasBeenSet() {
		final ClamshellLauncherFactoryInjector objectUnderTest = new ClamshellLauncherFactoryInjector();
		final Object beanPassedIn = new SampleClamshellLauncherFactoryAware();

		objectUnderTest
		        .postProcessBeforeInitialization(
		                beanPassedIn,
		                "assertThatPostProcessBeforeInitializationRecognizesThatNoClamshellLauncherFactoryHasBeenSet");
	}

	private static class SampleClamshellLauncherFactoryAware implements
	        ClamshellLauncherFactoryAware {

		ClamshellLauncher.Factory	clamshellLauncherFactory;

		@Override
		public void setClamshellLauncherFactory(
		        final Factory clamshellLauncherFactory) {
			this.clamshellLauncherFactory = clamshellLauncherFactory;
		}
	}

	@Test
	public final void assertThatPostProcessBefpreInitializationSetsClamshellLauncherFactoryOnBeanImplementingClamshellLauncherFactoryAware() {
		final ClamshellLauncherFactoryInjector objectUnderTest = new ClamshellLauncherFactoryInjector();
		objectUnderTest
		        .setClamshellLauncherFactory(new EmbeddedClamshellLauncherFactory());
		final SampleClamshellLauncherFactoryAware beanPassedIn = new SampleClamshellLauncherFactoryAware();

		objectUnderTest
		        .postProcessBeforeInitialization(
		                beanPassedIn,
		                "assertThatPostProcessBefpreInitializationSetsClamshellLauncherFactoryOnBeanImplementingClamshellLauncherFactoryAware");

		assertNotNull(
		        "postProcessAfterInitialization(...) should have set ClamshellLauncher.Factory on bean passed in",
		        beanPassedIn.clamshellLauncherFactory);
	}

	@Test
	public final void assertThatPostProcessAfterInitializationReturnsBeanPassedIn() {
		final ClamshellLauncherFactoryInjector objectUnderTest = new ClamshellLauncherFactoryInjector();
		final Object beanPassedIn = new Object();

		final Object beanReturned = objectUnderTest
		        .postProcessAfterInitialization(beanPassedIn,
		                "assertThatPostProcessAfterInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessAfterInitialization(...) should have returned the bean passed in, yet it didn't",
		        beanPassedIn, beanReturned);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetClamshellLauncherFactoryRejectsNullClamshellLauncherFactory() {
		final ClamshellLauncherFactoryInjector objectUnderTest = new ClamshellLauncherFactoryInjector();

		objectUnderTest.setClamshellLauncherFactory(null);
	}
}
