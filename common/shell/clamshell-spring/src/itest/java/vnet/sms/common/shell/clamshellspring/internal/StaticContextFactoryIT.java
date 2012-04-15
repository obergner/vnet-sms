package vnet.sms.common.shell.clamshellspring.internal;

import static org.junit.Assert.assertNotNull;

import org.clamshellcli.api.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@ContextConfiguration({ "classpath*:META-INF/module/module-context.xml" })
public class StaticContextFactoryIT {

	@Autowired
	private StaticContextFactory	objectUnderTest;

	@Test
	public final void asserThatNewContextProducesNonNullContext() {
		final Context newContext = this.objectUnderTest.newContext();

		assertNotNull(
		        "newContext() returned a null context - this should NEVER happen",
		        newContext);
	}
}
