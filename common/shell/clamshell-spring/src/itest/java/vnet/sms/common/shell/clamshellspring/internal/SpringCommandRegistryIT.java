package vnet.sms.common.shell.clamshellspring.internal;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.clamshellcli.api.Command;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@ContextConfiguration({ "classpath*:META-INF/module/module-context.xml" })
public class SpringCommandRegistryIT {

	@Autowired
	private SpringCommandRegistry	objectUnderTest;

	@Test
	public final void assertThatSpringCommandRegistryRegistersAllCommands() {
		final List<Command> allRegisteredCommands = this.objectUnderTest
		        .getCommands();

		assertEquals(
		        "SpringCommandRegistry registered an unexpected number of commands on application context startup",
		        3, allRegisteredCommands.size());
	}
}
