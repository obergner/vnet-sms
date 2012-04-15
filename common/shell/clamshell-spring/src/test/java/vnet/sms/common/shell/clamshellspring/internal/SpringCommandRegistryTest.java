package vnet.sms.common.shell.clamshellspring.internal;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.junit.Test;

public class SpringCommandRegistryTest {

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatGetCommandsReturnsImmutableList() {
		final SpringCommandRegistry objectUnderTest = new SpringCommandRegistry();
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
		        "assertThatGetCommandsReturnsImmutableList");

		final List<Command> commands = objectUnderTest.getCommands();

		commands.remove(0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatGetCommandsByNamespaceReturnsAnImmutableList() {
		final SpringCommandRegistry objectUnderTest = new SpringCommandRegistry();
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
		        "assertThatGetCommandsByNamespaceReturnsAnImmutableList");

		final List<Command> commands = objectUnderTest
		        .getCommandsByNamespace("namespace");

		commands.remove(0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void assertThatMapCommandsReturnsAnImmutableMap() {
		final SpringCommandRegistry objectUnderTest = new SpringCommandRegistry();

		final Map<String, Command> commands = objectUnderTest
		        .mapCommands(Collections.<Command> emptyList());

		commands.remove(0);
	}

	@Test
	public final void assertThatPostProcessBeforeInitializationReturnsBeanPassedIn() {
		final SpringCommandRegistry objectUnderTest = new SpringCommandRegistry();
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
		final SpringCommandRegistry objectUnderTest = new SpringCommandRegistry();
		final Object beanPassedIn = new Object();

		final Object beanReturned = objectUnderTest
		        .postProcessAfterInitialization(beanPassedIn,
		                "assertThatPostProcessAfterInitializationReturnsBeanPassedIn");

		assertSame(
		        "postProcessAfterInitialization(...) should have returned the bean passed in, yet it didn't",
		        beanPassedIn, beanReturned);
	}

	@Test
	public final void assertThatPostProcessAfterInitializationIgnoresNonCommandBeans() {
		final SpringCommandRegistry objectUnderTest = new SpringCommandRegistry();
		final Object nonPluginBean = new Object();

		objectUnderTest
		        .postProcessAfterInitialization(nonPluginBean,
		                "assertThatPostProcessAfterInitializationIgnoresNonPluginBeans");

		assertTrue(
		        "postProcessAfterInitialization(...) should have ignored a bean that does not implement "
		                + Command.class.getName()
		                + ", yet it registered it as a Plugin", objectUnderTest
		                .getCommands().isEmpty());
	}

	@Test
	public final void assertThatPostProcessAfterInitializationRegistersACommand() {
		final SpringCommandRegistry objectUnderTest = new SpringCommandRegistry();
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
		        "assertThatPostProcessAfterInitializationRegistersACommand");

		assertTrue(
		        "postProcessAfterInitialization(...) should have registered a bean that implements "
		                + Command.class.getName() + ", yet it ignored it",
		        objectUnderTest.getCommands().size() == 1);
	}
}
