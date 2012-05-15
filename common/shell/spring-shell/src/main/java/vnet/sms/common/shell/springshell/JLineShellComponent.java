package vnet.sms.common.shell.springshell;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

import vnet.sms.common.shell.springshell.internal.SimpleExecutionStrategy;
import vnet.sms.common.shell.springshell.plugin.PluginProvidersRegistry;

/**
 * Launcher for {@link JLineShell}.
 * 
 * @author Ben Alex
 * @since 1.1
 */
public class JLineShellComponent extends JLineShell {

	private volatile boolean	    running	          = false;

	private Thread	                shellThread;

	private final ExecutionStrategy	executionStrategy	= new SimpleExecutionStrategy();

	private final Parser	        parser;

	public JLineShellComponent(
	        final PluginProvidersRegistry pluginProvidersRegistry,
	        final Parser parser) {
		this(pluginProvidersRegistry, null, null, parser);

	}

	public JLineShellComponent(
	        final PluginProvidersRegistry pluginProvidersRegistry,
	        final InputStream input, final OutputStream output,
	        final Parser parser) {
		super(pluginProvidersRegistry, input, output);
		this.parser = parser;
	}

	public void start() {
		// customizePlug must run before start thread to take plugin's
		// configuration into effect
		super.costomizePlugin();
		this.shellThread = new Thread(this, "Spring Shell");
		this.shellThread.start();
		this.running = true;
	}

	public void stop() {
		closeShell();
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	/**
	 * wait the shell command to complete by typing "quit" or "exit"
	 * 
	 */
	public void waitForComplete() {
		try {
			this.shellThread.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Collection<URL> findResources(final String path) {
		// For an OSGi bundle search, we add the root prefix to the given path
		throw new UnsupportedOperationException(
		        "TODO: need to use standard classpath search");
		// return OSGiUtils.findEntriesByPath(context.getBundleContext(),
		// OSGiUtils.ROOT_PATH + path);
	}

	@Override
	protected ExecutionStrategy getExecutionStrategy() {
		return this.executionStrategy;
	}

	@Override
	protected Parser getParser() {
		return this.parser;
	}

	@Override
	public String getStartupNotifications() {
		return null;
	}

}
