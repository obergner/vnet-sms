package vnet.sms.common.shell.clamshellspring.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.Plugin;
import org.clamshellcli.api.Prompt;
import org.clamshellcli.api.Shell;
import org.clamshellcli.api.SplashScreen;
import org.junit.Test;

public class SpringPluginRegistryTest {

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatGetPluginsReturnsAnImmutableList() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Plugin pluginBean = new Plugin() {
			@Override
			public void plug(final Context arg0) {
			}
		};
		objectUnderTest.postProcessAfterInitialization(pluginBean,
		        "assertThatGetPluginsReturnsAnImmutableList");

		final List<Plugin> plugins = objectUnderTest.getPlugins();

		plugins.remove(0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatGetPluginsByTypeReturnsAnImmutableList() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Plugin pluginBean = new Plugin() {
			@Override
			public void plug(final Context arg0) {
			}
		};
		objectUnderTest.postProcessAfterInitialization(pluginBean,
		        "assertThatGetPluginsReturnsAnImmutableList");

		final List<Plugin> plugins = objectUnderTest
		        .getPluginsByType(Plugin.class);

		plugins.remove(0);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetShellRecognizesThatNoShellHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();

		objectUnderTest.getShell();
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetShellRecognizesThatMoreThaOneShellHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Shell shellOne = new Shell() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public void exec(final Context arg0) {
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(shellOne,
		                "assertThatGetShellRecognizesThatMoreThaOneShellHasBeenRegistered");
		final Shell shellTwo = new Shell() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public void exec(final Context arg0) {
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(shellTwo,
		                "assertThatGetShellRecognizesThatMoreThaOneShellHasBeenRegistered");

		objectUnderTest.getShell();
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetIOConsoleRecognizesThatNoIOConsoleHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();

		objectUnderTest.getIOConsole();
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetIOConsoleRecognizesThatMoreThaOneIOConsoleHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final IOConsole one = new IOConsole() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public InputStream getInputStream() {
				return null;
			}

			@Override
			public OutputStream getOutputStream() {
				return null;
			}

			@Override
			public String readInput(final String arg0) {
				return null;
			}

			@Override
			public void writeOutput(final String arg0) {
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(one,
		                "assertThatGetIOConsoleRecognizesThatMoreThaOneIOConsoleHasBeenRegistered");
		final IOConsole two = new IOConsole() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public InputStream getInputStream() {
				return null;
			}

			@Override
			public OutputStream getOutputStream() {
				return null;
			}

			@Override
			public String readInput(final String arg0) {
				return null;
			}

			@Override
			public void writeOutput(final String arg0) {
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(two,
		                "assertThatGetIOConsoleRecognizesThatMoreThaOneIOConsoleHasBeenRegistered");

		objectUnderTest.getIOConsole();
	}

	@Test
	public final void assertThatGetPromptReturnsDefaultPromptIfNoPromptHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();

		final Prompt defaultPrompt = objectUnderTest.getPrompt();

		assertNotNull(
		        "SpringPluginRegistry should have returned a default Prompt if none has been explicitly registered",
		        defaultPrompt);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetPromptRecognizesThatMoreThaOnePromptHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Prompt one = new Prompt() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public String getValue(final Context arg0) {
				return null;
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(one,
		                "assertThatGetPromptRecognizesThatMoreThaOnePromptHasBeenRegistered");
		final Prompt two = new Prompt() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public String getValue(final Context arg0) {
				return null;
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(two,
		                "assertThatGetPromptRecognizesThatMoreThaOnePromptHasBeenRegistered");

		objectUnderTest.getPrompt();
	}

	@Test
	public final void assertThatGetSplashScreenReturnsNullIfNoSplashScreenHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();

		final SplashScreen nullSplashScreen = objectUnderTest.getSplashScreen();

		assertNull(
		        "getSplashScreen() should return null if no splash screen has been registered",
		        nullSplashScreen);
	}

	@Test(expected = IllegalStateException.class)
	public final void assertThatGetSplashScreenRecognizesThatMoreThaOneSplashScreenHasBeenRegistered() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final SplashScreen one = new SplashScreen() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public void render(final Context arg0) {
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(
		                one,
		                "assertThatGetSplashScreenRecognizesThatMoreThaOneSplashScreenHasBeenRegistered");
		final SplashScreen two = new SplashScreen() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public void render(final Context arg0) {
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(
		                two,
		                "assertThatGetSplashScreenRecognizesThatMoreThaOneSplashScreenHasBeenRegistered");

		objectUnderTest.getSplashScreen();
	}

	@Test
	public final void assertThatPostProcessBeforeInitializationReturnsBeanPassedIn() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Object beanPassedIn = new Object();

		final Object beanReturned = objectUnderTest
		        .postProcessBeforeInitialization(beanPassedIn,
		                "assertThatPostProcessBeforeInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessBeforeInitialization(...) should have returned the bean passed in, yet it didn't",
		        beanPassedIn, beanReturned);
	}

	@Test
	public final void assertThatPostProcessAfterInitializationReturnsBeanPassedIn() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Object beanPassedIn = new Object();

		final Object beanReturned = objectUnderTest
		        .postProcessAfterInitialization(beanPassedIn,
		                "assertThatPostProcessAfterInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessAfterInitialization(...) should have returned the bean passed in, yet it didn't",
		        beanPassedIn, beanReturned);
	}

	@Test
	public final void assertThatPostProcessAfterInitializationIgnoresNonPluginBeans() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Object nonPluginBean = new Object();

		objectUnderTest
		        .postProcessAfterInitialization(nonPluginBean,
		                "assertThatPostProcessAfterInitializationIgnoresNonPluginBeans");

		assertTrue(
		        "postProcessAfterInitialization(...) should have ignored a bean that does not implement "
		                + Plugin.class.getName()
		                + ", yet it registered it as a Plugin", objectUnderTest
		                .getPlugins().isEmpty());
	}

	@Test
	public final void assertThatPostProcessAfterInitializationIgnoresCommandBeans() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Command commandBean = new Command() {
			@Override
			public void plug(final Context arg0) {
			}

			@Override
			public Object execute(final Context arg0) {
				return null;
			}

			@Override
			public Descriptor getDescriptor() {
				return null;
			}
		};

		objectUnderTest.postProcessAfterInitialization(commandBean,
		        "assertThatPostProcessAfterInitializationIgnoresCommandBeans");

		assertTrue(
		        "postProcessAfterInitialization(...) should have ignored a bean that implements "
		                + Command.class.getName()
		                + ", yet it registered it as a Plugin", objectUnderTest
		                .getPlugins().isEmpty());
	}

	@Test
	public final void assertThatPostProcessAfterInitializationRegistersAPluginThatIsNotACommand() {
		final SpringPluginRegistry objectUnderTest = new SpringPluginRegistry();
		final Plugin pluginBean = new Plugin() {
			@Override
			public void plug(final Context arg0) {
			}
		};
		objectUnderTest
		        .postProcessAfterInitialization(pluginBean,
		                "assertThatPostProcessAfterInitializationRegistersAPluginThatIsNotACommand");

		assertTrue(
		        "postProcessAfterInitialization(...) should have registered a bean that implements "
		                + Plugin.class.getName()
		                + " and is not a "
		                + Command.class.getName() + ", yet it ignored it",
		        objectUnderTest.getPlugins().size() == 1);
	}
}
