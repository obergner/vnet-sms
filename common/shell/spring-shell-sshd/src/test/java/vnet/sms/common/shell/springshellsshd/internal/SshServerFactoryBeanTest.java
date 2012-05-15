package vnet.sms.common.shell.springshellsshd.internal;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.sshd.SshServer;
import org.junit.Test;

import vnet.sms.common.shell.springshell.JLineShellComponentFactory;

public class SshServerFactoryBeanTest {

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetObjectRecognizesThatAfterPropertiesSetHasNotBeenCalled()
	        throws Exception {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		objectUnderTest.getObject();
	}

	@Test
	public final void assertThatGetObjectReturnsNonNullSshServerIfAfterPropertiesSetHasBeenCalled()
	        throws Exception {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();
		objectUnderTest
		        .setJLineShellComponentFactory(createNiceMock(JLineShellComponentFactory.class));
		objectUnderTest.setAutostart(true);
		objectUnderTest.afterPropertiesSet();

		final SshServer sshServer = objectUnderTest.getObject();

		assertNotNull(
		        "getObject() returned null although afterPropertiesSet() has been called",
		        sshServer);
	}

	@Test
	public final void assertThatGetObjectTypeReturnsSubclassOfSshServer() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		final Class<?> objectType = objectUnderTest.getObjectType();

		assertTrue("getObjectType() should have returned a subtype of "
		        + SshServer.class.getName(),
		        SshServer.class.isAssignableFrom(objectType));
	}

	@Test
	public final void assertThatIsSingletonReturnsTrue() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		assertTrue("isSingleton() should always return true",
		        objectUnderTest.isSingleton());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetHostRejectsNullHost() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		objectUnderTest.setHost(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetHostRejectsEmptyHost() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		objectUnderTest.setHost("");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetHostKeyPathRejectsNullHostKeyPath() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		objectUnderTest.setHostKeyPath(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetHostKeyPathRejectsEmptyHostKeyPath() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		objectUnderTest.setHostKeyPath("");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetClamshellLauncherFactoryRejectsNullFactory() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		objectUnderTest.setJLineShellComponentFactory(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void assertThatSetScheduledExecutorServiceRejectsNullScheduledExecutorService() {
		final SshServerFactoryBean objectUnderTest = new SshServerFactoryBean();

		objectUnderTest.setScheduledExecutorService(null);
	}
}
