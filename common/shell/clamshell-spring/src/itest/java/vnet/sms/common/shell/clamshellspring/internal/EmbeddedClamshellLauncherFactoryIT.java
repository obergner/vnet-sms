package vnet.sms.common.shell.clamshellspring.internal;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.shell.clamshellspring.ClamshellLauncher;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@ContextConfiguration({ "classpath*:META-INF/module/module-context.xml" })
public class EmbeddedClamshellLauncherFactoryIT {

	@Autowired
	private EmbeddedClamshellLauncherFactory	objectUnderTest;

	@Test
	public final void assertThatNewLauncherReturnsNonNullClamshellLauncher() {
		final ClamshellLauncher newLauncher = this.objectUnderTest
		        .newLauncher();

		assertNotNull(
		        "newLauncher() returned a null launcher - this should NEVER happen",
		        newLauncher);
	}
}
