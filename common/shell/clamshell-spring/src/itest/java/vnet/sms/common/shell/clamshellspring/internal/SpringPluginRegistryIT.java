package vnet.sms.common.shell.clamshellspring.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.InputController;
import org.clamshellcli.api.Plugin;
import org.clamshellcli.api.Shell;
import org.clamshellcli.impl.CliConsole;
import org.clamshellcli.impl.CliShell;
import org.clamshellcli.impl.CmdController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@ContextConfiguration({ "classpath*:META-INF/module/module-context.xml" })
public class SpringPluginRegistryIT {

	@Autowired
	private SpringPluginRegistry	objectUnderTest;

	@Test
	public final void assertThatSpringPluginRegistryRegistersAllPlugins() {
		final List<Plugin> allRegisteredPlugins = this.objectUnderTest
		        .getPlugins();

		assertEquals(
		        "SpringPluginRegistry registered an unexpected number of plugins on application context startup",
		        3, allRegisteredPlugins.size());
	}

	@Test
	public final void assertThatSpringPluginRegistryRegistersCliShell() {
		final Shell shell = this.objectUnderTest.getShell();

		assertEquals(
		        "SpringPluginRegistry registered unexpected type of shell",
		        CliShell.class, shell.getClass());
	}

	@Test
	public final void assertThatSpringPluginRegistryRegistersCliConsole() {
		final IOConsole console = this.objectUnderTest.getIOConsole();

		assertEquals(
		        "SpringPluginRegistry registered unexpected type of console",
		        CliConsole.class, console.getClass());
	}

	@Test
	public final void assertThatSpringPluginRegistryRegistersCmdController() {
		final List<InputController> inputControllers = this.objectUnderTest
		        .getPluginsByType(InputController.class);
		CmdController cmdController = null;
		for (final InputController candidate : inputControllers) {
			if (CmdController.class.isInstance(candidate)) {
				cmdController = CmdController.class.cast(candidate);
				break;
			}
		}

		assertNotNull(
		        "SpringPluginRegistry did NOT register CmdController a plugin",
		        cmdController);
	}
}
