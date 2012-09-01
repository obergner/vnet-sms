package vnet.sms.common.shell.springshell.internal;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import vnet.sms.common.shell.springshell.JLineShellComponent;
import vnet.sms.common.shell.springshell.JLineShellComponentFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("itest")
@ContextConfiguration({ "classpath*:META-INF/module/module-context.xml" })
public class JLineShellComponentFactoryContextIT {

	@Autowired
	private JLineShellComponentFactory	objectUnderTest;

	private JLineShellComponent	       shell;

	@Before
	public void createAndStartShell() {
		this.shell = this.objectUnderTest.newShell(System.in, System.out);
		this.shell.start();
	}

	@After
	public void quitAndStopShell() {
		this.shell.executeCommand("quit");
		this.shell.stop();
	}

	@Test
	public final void assertThatShellSuccessfullyExcutesHelpCommand()
	        throws InterruptedException {
		final boolean successful = this.shell.executeCommand("help");

		assertTrue("Shell failed to execute command 'help'", successful);
	}

}
