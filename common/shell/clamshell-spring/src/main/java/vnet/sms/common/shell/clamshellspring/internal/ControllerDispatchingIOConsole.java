/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jline.CandidateListCompletionHandler;
import jline.ConsoleReader;
import jline.SimpleCompletor;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.InputController;
import org.clamshellcli.api.Prompt;
import org.clamshellcli.api.SplashScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author obergner
 * 
 */
public class ControllerDispatchingIOConsole implements IOConsole {

	private final Logger	      log	= LoggerFactory.getLogger(getClass());

	private Context	              context;

	private Prompt	              prompt;

	private ConsoleReader	      console;

	private List<InputController>	controllers;

	private InputStream	          input;

	private OutputStream	      output;

	private Thread	              consoleThread;

	@Override
	public InputStream getInputStream() {
		return this.input;
	}

	@Override
	public OutputStream getOutputStream() {
		return this.output;
	}

	@Override
	public void plug(final Context plug) {
		this.log.info("Initializing IOConsole [{}] ...", this);
		this.context = plug;
		this.prompt = plug.getPrompt();
		this.input = (InputStream) this.context
		        .getValue(Context.KEY_INPUT_STREAM);
		this.output = (OutputStream) this.context
		        .getValue(Context.KEY_OUTPUT_STREAM);

		this.console = createConsoleReader();
		this.log.debug("Console reader [{}] created", this.console);

		// plug in installed input controllers
		this.controllers = createControllers(plug);
		this.log.debug("Created and registered input controllers [{}]",
		        this.controllers);
		aggregateExpectedInputs();

		// show splash on the default OutputStream
		renderSplashScreens(plug);

		this.consoleThread = createConsoleThread();
		this.consoleThread.start();
		this.log.info("Console thread [{}] started", this.consoleThread);
	}

	private ConsoleReader createConsoleReader() throws RuntimeException {
		try {
			final ConsoleReader result = new ConsoleReader(this.input,
			        new OutputStreamWriter(this.output));
			result.setCompletionHandler(new CandidateListCompletionHandler());
			return result;
		} catch (final IOException ex) {
			throw new RuntimeException("Unable to initialize the console. "
			        + " Program will stop now.", ex);
		}
	}

	private List<InputController> createControllers(final Context plug)
	        throws RuntimeException {
		final List<InputController> result = plug
		        .getPluginsByType(InputController.class);
		if (result.size() == 0) {
			throw new RuntimeException(
			        "Unable to initialize Clamshell-Cli. "
			                + " No InputController instances found in plugin registry             . Exiting...");
		}
		for (final InputController ctrl : result) {
			ctrl.plug(plug);
		}

		return result;
	}

	private void renderSplashScreens(final Context plug) {
		final List<SplashScreen> screens = plug
		        .getPluginsByType(SplashScreen.class);
		if ((screens != null) && (screens.size() > 0)) {
			for (final SplashScreen sc : screens) {
				sc.plug(plug);
				sc.render(plug);
			}
		}
	}

	@Override
	public void writeOutput(final String val) {
		try {
			this.console.printString(val);
		} catch (final IOException ex) {
			throw new RuntimeException("Unable to invoke print on console: ",
			        ex);
		}
	}

	@Override
	public String readInput(final String prompt) {
		try {
			return this.console.readLine(prompt);
		} catch (final IOException ex) {
			throw new RuntimeException("Unable to read input: ", ex);
		}
	}

	private Thread createConsoleThread() {
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					// reset command line arguments from previous command
					ControllerDispatchingIOConsole.this.context.putValue(
					        Context.KEY_COMMAND_LINE_ARGS, null);

					boolean handled = false;
					final String line = readInput(ControllerDispatchingIOConsole.this.prompt
					        .getValue(ControllerDispatchingIOConsole.this.context));
					if ((line == null) || line.trim().isEmpty()) {
						continue;
					}
					ControllerDispatchingIOConsole.this.log.trace(
					        "Handling input line [{}] ...", line);

					ControllerDispatchingIOConsole.this.context.putValue(
					        Context.KEY_COMMAND_LINE_INPUT, line);
					if (!controllersExist()) {
						writeOutput(String
						        .format("Warning: no controllers(s) found.%n"));
						ControllerDispatchingIOConsole.this.log
						        .warn("No input controller(s) found");
						continue;
					}
					for (final InputController controller : ControllerDispatchingIOConsole.this.controllers) {
						ControllerDispatchingIOConsole.this.log
						        .trace("Dispatching line [{}] to input controller [{}] ...",
						                new Object[] { line, controller });
						final boolean ctrlResult = controller
						        .handle(ControllerDispatchingIOConsole.this.context);
						handled = handled || ctrlResult;
					}
					// was command line handled.
					if (!handled) {
						writeOutput(String
						        .format("%nCommand unhandled. "
						                + "%nNo controller found to respond to [%s].%n%n",
						                line));
						ControllerDispatchingIOConsole.this.log
						        .warn("Could not process line [{}] - no matching input controller found",
						                line);
					}
				}
			}
		});

		return t;
	}

	/**
	 * Are there any controllers installed?
	 * 
	 * @param controllers
	 */
	private boolean controllersExist() {
		return ((this.controllers != null) && (this.controllers.size() > 0)) ? true
		        : false;
	}

	/**
	 * Collection expected input values to build suggestion lists.
	 */
	private void aggregateExpectedInputs() {
		final List<String> inputs = new ArrayList<String>();
		for (final InputController ctrl : this.controllers) {
			final String[] expectedInputs = ctrl.getExpectedInputs();
			if (expectedInputs != null) {
				Collections.addAll(inputs, expectedInputs);
			}
		}
		this.console.addCompletor(new SimpleCompletor(inputs
		        .toArray(new String[0])));
	}

}
