package vnet.sms.common.shell.springshell.internal;

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

	@Test
	public final void testNewShell() throws InterruptedException {
		final JLineShellComponent shell = this.objectUnderTest.newShell(
		        System.in, System.out);
		shell.start();
		Thread.sleep(20000L);
		shell.stop();
	}

}
