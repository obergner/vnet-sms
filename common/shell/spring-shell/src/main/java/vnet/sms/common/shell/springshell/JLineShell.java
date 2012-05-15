package vnet.sms.common.shell.springshell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jline.ANSIBuffer;
import jline.ANSIBuffer.ANSICodes;
import jline.ConsoleReader;
import jline.WindowsTerminal;

import org.apache.commons.io.input.ReversedLinesFileReader;

import vnet.sms.common.shell.springshell.command.CommandMarker;
import vnet.sms.common.shell.springshell.event.ShellStatus;
import vnet.sms.common.shell.springshell.event.ShellStatus.Status;
import vnet.sms.common.shell.springshell.event.ShellStatusListener;
import vnet.sms.common.shell.springshell.internal.JLineCompletorAdapter;
import vnet.sms.common.shell.springshell.internal.logging.JLineLogHandler;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.ClassUtils;
import vnet.sms.common.shell.springshell.internal.util.IOUtils;
import vnet.sms.common.shell.springshell.internal.util.OsUtils;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;
import vnet.sms.common.shell.springshell.plugin.BannerProvider;
import vnet.sms.common.shell.springshell.plugin.HistoryFileNameProvider;
import vnet.sms.common.shell.springshell.plugin.PluginProvidersRegistry;
import vnet.sms.common.shell.springshell.plugin.PromptProvider;

/**
 * Uses the feature-rich <a
 * href="http://sourceforge.net/projects/jline/">JLine</a> library to provide an
 * interactive shell.
 * 
 * <p>
 * Due to Windows' lack of color ANSI services out-of-the-box, this
 * implementation automatically detects the classpath presence of <a
 * href="http://jansi.fusesource.org/">Jansi</a> and uses it if present. This
 * library is not necessary for *nix machines, which support colour ANSI without
 * any special effort. This implementation has been written to use reflection in
 * order to avoid hard dependencies on Jansi.
 * 
 * @author Ben Alex
 * @author Jarred Li
 * @since 1.0
 */
public abstract class JLineShell extends AbstractShell implements
        CommandMarker, Shell, Runnable {

	// Constants
	private static final String	          ANSI_CONSOLE_CLASSNAME	= "org.fusesource.jansi.AnsiConsole";

	private static final boolean	      JANSI_AVAILABLE	     = ClassUtils
	                                                                     .isPresent(
	                                                                             ANSI_CONSOLE_CLASSNAME,
	                                                                             JLineShell.class
	                                                                                     .getClassLoader());

	private static final boolean	      APPLE_TERMINAL	     = Boolean
	                                                                     .getBoolean("is.apple.terminal");

	private static final char	          ESCAPE	             = 27;

	private static final String	          BEL	                 = "\007";

	// Fields
	private final PluginProvidersRegistry	pluginProvidersRegistry;

	private final InputStream	          input;

	private final OutputStream	          output;

	protected ConsoleReader	              reader;

	private final boolean	              developmentMode	     = false;

	private FileWriter	                  fileLog;

	private final DateFormat	          df	                 = new SimpleDateFormat(
	                                                                     "yyyy-MM-dd HH:mm:ss");

	protected ShellStatusListener	      statusListener;	                                                 // ROO-836

	/** key: slot name, value: flashInfo instance */
	private final Map<String, FlashInfo>	flashInfoMap	     = new HashMap<String, FlashInfo>();

	/** key: row number, value: eraseLineFromPosition */
	private final Map<Integer, Integer>	  rowErasureMap	         = new HashMap<Integer, Integer>();

	private boolean	                      shutdownHookFired	     = false;	                                 // ROO-1599

	private boolean	                      printBanner	         = true;

	private String	                      historyFileName;

	private String	                      promptText;

	private String	                      version;

	private String	                      welcomeMessage;

	private int	                          historySize;

	/**
	 * @param input
	 * @param output
	 */
	protected JLineShell(final PluginProvidersRegistry pluginProvidersRegistry,
	        final InputStream input, final OutputStream output) {
		this.pluginProvidersRegistry = pluginProvidersRegistry;
		this.input = input != null ? input : System.in;
		this.output = output != null ? output : System.out;
	}

	@Override
	public void run() {
		this.reader = createConsoleReader();

		setPromptPath(null);

		final JLineLogHandler handler = new JLineLogHandler(this.reader, this);
		JLineLogHandler.prohibitRedraw(); // Affects this thread only
		final Logger mainLogger = Logger.getLogger("");
		removeHandlers(mainLogger);
		mainLogger.addHandler(handler);

		this.reader.addCompletor(new JLineCompletorAdapter(getParser()));

		this.reader.setBellEnabled(true);
		if (Boolean.getBoolean("jline.nobell")) {
			this.reader.setBellEnabled(false);
		}

		// reader.setDebug(new PrintWriter(new FileWriter("writer.debug",
		// true)));

		openFileLogIfPossible();
		this.reader.getHistory().setMaxSize(this.historySize);
		// Try to build previous command history from the project's log
		final String[] filteredLogEntries = filterLogEntry();
		for (final String logEntry : filteredLogEntries) {
			this.reader.getHistory().addToHistory(logEntry);
		}

		flashMessageRenderer();
		flash(Level.FINE, "Spring Shell " + versionInfo(),
		        Shell.WINDOW_TITLE_SLOT);
		printBannerAndWelcome();

		final String startupNotifications = getStartupNotifications();
		if (StringUtils.hasText(startupNotifications)) {
			this.logger.info(startupNotifications);
		}

		setShellStatus(Status.STARTED);

		// Monitor CTRL+C initiated shutdowns (ROO-1599)
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				JLineShell.this.shutdownHookFired = true;
				// We don't need to closeShell(), as the shutdown hook in
				// o.s.r.bootstrap.Main calls stop() which calls
				// JLineShellComponent.deactivate() and that calls closeShell()
			}
		}, "Spring Shell JLine Shutdown Hook"));

		// Handle any "execute-then-quit" operation

		final String rooArgs = System.getProperty("roo.args");
		if ((rooArgs != null) && !"".equals(rooArgs)) {
			setShellStatus(Status.USER_INPUT);
			final boolean success = executeCommand(rooArgs);
			if (this.exitShellRequest == null) {
				// The command itself did not specify an exit shell code, so
				// we'll fall back to something sensible here
				executeCommand("quit"); // ROO-839
				this.exitShellRequest = success ? ExitShellRequest.NORMAL_EXIT
				        : ExitShellRequest.FATAL_EXIT;
			}
			setShellStatus(Status.SHUTTING_DOWN);
		} else {
			// Normal RPEL processing
			promptLoop();
		}

	}

	/**
	 * read history commands from history log. the history size if determined by
	 * --histsize options.
	 * 
	 * @return history commands
	 */
	private String[] filterLogEntry() {
		final ArrayList<String> entries = new ArrayList<String>();
		try {
			final ReversedLinesFileReader reader = new ReversedLinesFileReader(
			        new File(this.historyFileName), 4096,
			        Charset.forName("UTF-8"));
			int size = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("//")) {
					size++;
					if (size > this.historySize) {
						break;
					} else {
						entries.add(line);
					}
				}
			}
		} catch (final IOException e) {
			this.logger.warning("read history file failed. Reason:"
			        + e.getMessage());
		}
		Collections.reverse(entries);
		return entries.toArray(new String[0]);
	}

	/**
	 * Creates new jline ConsoleReader. On Windows if jansi is available, uses
	 * createAnsiWindowsReader(). Otherwise, always creates a default
	 * ConsoleReader. Sub-classes of this class can plug in their version of
	 * ConsoleReader by overriding this method, if required.
	 * 
	 * @return a jline ConsoleReader instance
	 */
	protected ConsoleReader createConsoleReader() {
		ConsoleReader consoleReader = null;
		try {
			if (JANSI_AVAILABLE && OsUtils.isWindows()) {
				try {
					consoleReader = createAnsiWindowsReader();
				} catch (final Exception e) {
					// Try again using default ConsoleReader constructor
					this.logger
					        .warning("Can't initialize jansi AnsiConsole, falling back to default: "
					                + e);
				}
			}
			if (consoleReader == null) {
				consoleReader = new ConsoleReader(this.input, new PrintWriter(
				        this.output));
			}
		} catch (final IOException ioe) {
			throw new IllegalStateException("Cannot start console class", ioe);
		}
		return consoleReader;
	}

	public void printBannerAndWelcome() {
		if (this.printBanner) {
			this.logger.info(this.version);
			this.logger.info(getWelcomeMessage());
		}
	}

	public String getStartupNotifications() {
		return null;
	}

	private void removeHandlers(final Logger l) {
		final Handler[] handlers = l.getHandlers();
		if ((handlers != null) && (handlers.length > 0)) {
			for (final Handler h : handlers) {
				l.removeHandler(h);
			}
		}
	}

	@Override
	public void setPromptPath(final String path) {
		setPromptPath(path, false);
	}

	@Override
	public void setPromptPath(final String path, final boolean overrideStyle) {
		if (this.reader.getTerminal().isANSISupported()) {
			final ANSIBuffer ansi = JLineLogHandler.getANSIBuffer();
			if ((path == null) || "".equals(path)) {
				shellPrompt = ansi.yellow(this.promptText).toString();
			} else {
				if (overrideStyle) {
					ansi.append(path);
				} else {
					ansi.cyan(path);
				}
				shellPrompt = ansi.yellow(" " + this.promptText).toString();
			}
		} else {
			// The superclass will do for this non-ANSI terminal
			super.setPromptPath(path);
		}

		// The shellPrompt is now correct; let's ensure it now gets used
		this.reader.setDefaultPrompt(AbstractShell.shellPrompt);
	}

	protected ConsoleReader createAnsiWindowsReader() throws Exception {
		// Get decorated OutputStream that parses ANSI-codes
		final OutputStream ansiOut = (PrintStream) ClassUtils
		        .forName(ANSI_CONSOLE_CLASSNAME,
		                JLineShell.class.getClassLoader())
		        .getMethod("wrapOutputStream", OutputStream.class)
		        .invoke(this.output);
		final WindowsTerminal ansiTerminal = new WindowsTerminal() {
			@Override
			public boolean isANSISupported() {
				return true;
			}
		};
		ansiTerminal.initializeTerminal();
		// Make sure to reset the original shell's colors on shutdown by closing
		// the stream
		this.statusListener = new ShellStatusListener() {
			@Override
			public void onShellStatusChange(final ShellStatus oldStatus,
			        final ShellStatus newStatus) {
				if (newStatus.getStatus().equals(Status.SHUTTING_DOWN)) {
					try {
						ansiOut.close();
					} catch (final IOException e) {
						// Ignore
					}
				}
			}
		};
		addShellStatusListener(this.statusListener);

		return new ConsoleReader(this.input, new PrintWriter(
		        new OutputStreamWriter(ansiOut,
		        // Default to Cp850 encoding for Windows console output
		        // (ROO-439)
		                System.getProperty(
		                        "jline.WindowsTerminal.output.encoding",
		                        "Cp850"))), null, ansiTerminal);
	}

	private void flashMessageRenderer() {
		if (!this.reader.getTerminal().isANSISupported()) {
			return;
		}
		// Setup a thread to ensure flash messages are displayed and cleared
		// correctly
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!JLineShell.this.shellStatus.getStatus().equals(
				        Status.SHUTTING_DOWN)
				        && !JLineShell.this.shutdownHookFired) {
					synchronized (JLineShell.this.flashInfoMap) {
						final long now = System.currentTimeMillis();

						final Set<String> toRemove = new HashSet<String>();
						for (final String slot : JLineShell.this.flashInfoMap.keySet()) {
							final FlashInfo flashInfo = JLineShell.this.flashInfoMap
							        .get(slot);

							if (flashInfo.flashMessageUntil < now) {
								// Message has expired, so clear it
								toRemove.add(slot);
								doAnsiFlash(flashInfo.rowNumber, Level.ALL, "");
							} else {
								// The expiration time for this message has not
								// been reached, so preserve it
								doAnsiFlash(flashInfo.rowNumber,
								        flashInfo.flashLevel,
								        flashInfo.flashMessage);
							}
						}
						for (final String slot : toRemove) {
							JLineShell.this.flashInfoMap.remove(slot);
						}
					}
					try {
						Thread.sleep(200);
					} catch (final InterruptedException ignore) {
					}
				}
			}
		}, "Spring Roo JLine Flash Message Manager");
		t.start();
	}

	@Override
	public void flash(final Level level, final String message, final String slot) {
		Assert.notNull(level, "Level is required for a flash message");
		Assert.notNull(message, "Message is required for a flash message");
		Assert.hasText(slot, "Slot name must be specified for a flash message");

		if (Shell.WINDOW_TITLE_SLOT.equals(slot)) {
			if ((this.reader != null)
			        && this.reader.getTerminal().isANSISupported()) {
				// We can probably update the window title, as requested
				if (StringUtils.isBlank(message)) {
					System.out.println("No text");
				}

				final ANSIBuffer buff = JLineLogHandler.getANSIBuffer();
				buff.append(ESCAPE + "]0;").append(message).append(BEL);
				final String stg = buff.toString();
				try {
					this.reader.printString(stg);
					this.reader.flushConsole();
				} catch (final IOException ignored) {
				}
			}

			return;
		}
		if (((this.reader != null) && !this.reader.getTerminal()
		        .isANSISupported())) {
			super.flash(level, message, slot);
			return;
		}
		synchronized (this.flashInfoMap) {
			FlashInfo flashInfo = this.flashInfoMap.get(slot);

			if ("".equals(message)) {
				// Request to clear the message, but give the user some time to
				// read it first
				if (flashInfo == null) {
					// We didn't have a record of displaying it in the first
					// place, so just quit
					return;
				}
				flashInfo.flashMessageUntil = System.currentTimeMillis() + 1500;
			} else {
				// Display this message displayed until further notice
				if (flashInfo == null) {
					// Find a row for this new slot; we basically take the first
					// line number we discover
					flashInfo = new FlashInfo();
					flashInfo.rowNumber = Integer.MAX_VALUE;
					outer: for (int i = 1; i < Integer.MAX_VALUE; i++) {
						for (final FlashInfo existingFlashInfo : this.flashInfoMap
						        .values()) {
							if (existingFlashInfo.rowNumber == i) {
								// Veto, let's try the new candidate row number
								continue outer;
							}
						}
						// If we got to here, nobody owns this row number, so
						// use it
						flashInfo.rowNumber = i;
						break outer;
					}

					// Store it
					this.flashInfoMap.put(slot, flashInfo);
				}
				// Populate the instance with the latest data
				flashInfo.flashMessageUntil = Long.MAX_VALUE;
				flashInfo.flashLevel = level;
				flashInfo.flashMessage = message;

				// Display right now
				doAnsiFlash(flashInfo.rowNumber, flashInfo.flashLevel,
				        flashInfo.flashMessage);
			}
		}
	}

	// Externally synchronized via the two calling methods having a mutex on
	// flashInfoMap
	private void doAnsiFlash(final int row, final Level level,
	        final String message) {
		final ANSIBuffer buff = JLineLogHandler.getANSIBuffer();
		if (APPLE_TERMINAL) {
			buff.append(ESCAPE + "7");
		} else {
			buff.append(ANSICodes.save());
		}

		// Figure out the longest line we're presently displaying (or were) and
		// erase the line from that position
		int mostFurtherLeftColNumber = Integer.MAX_VALUE;
		for (final Integer candidate : this.rowErasureMap.values()) {
			if (candidate < mostFurtherLeftColNumber) {
				mostFurtherLeftColNumber = candidate;
			}
		}

		if (mostFurtherLeftColNumber == Integer.MAX_VALUE) {
			// There is nothing to erase
		} else {
			buff.append(ANSICodes.gotoxy(row, mostFurtherLeftColNumber));
			buff.append(ANSICodes.clreol()); // Clear what was present on the
			                                 // line
		}

		if (("".equals(message))) {
			// They want the line blank; we've already achieved this if needed
			// via the erasing above
			// Just need to record we no longer care about this line the next
			// time doAnsiFlash is invoked
			this.rowErasureMap.remove(row);
		} else {
			if (this.shutdownHookFired) {
				return; // ROO-1599
			}
			// They want some message displayed
			int startFrom = this.reader.getTermwidth() - message.length() + 1;
			if (startFrom < 1) {
				startFrom = 1;
			}
			buff.append(ANSICodes.gotoxy(row, startFrom));
			buff.reverse(message);
			// Record we want to erase from this positioning next time (so we
			// clean up after ourselves)
			this.rowErasureMap.put(row, startFrom);
		}
		if (APPLE_TERMINAL) {
			buff.append(ESCAPE + "8");
		} else {
			buff.append(ANSICodes.restore());
		}

		final String stg = buff.toString();
		try {
			this.reader.printString(stg);
			this.reader.flushConsole();
		} catch (final IOException ignored) {
		}
	}

	@Override
	public void promptLoop() {
		setShellStatus(Status.USER_INPUT);
		try {
			String line;
			while ((this.exitShellRequest == null)
			        && ((line = this.reader.readLine()) != null)) {
				JLineLogHandler.resetMessageTracking();
				setShellStatus(Status.USER_INPUT);

				if ("".equals(line)) {
					continue;
				}

				executeCommand(line);
			}
		} catch (final IOException ioe) {
			throw new IllegalStateException("Shell line reading failure", ioe);
		}
		setShellStatus(Status.SHUTTING_DOWN);
	}

	private void openFileLogIfPossible() {
		try {
			this.fileLog = new FileWriter(this.historyFileName, true);
			// First write, so let's record the date and time of the first user
			// command
			this.fileLog.write("// Spring Roo " + versionInfo()
			        + " log opened at " + this.df.format(new Date()) + "\n");
			this.fileLog.flush();
		} catch (final IOException ignoreIt) {
		}
	}

	@Override
	protected void logCommandToOutput(final String processedLine) {
		if (this.fileLog == null) {
			openFileLogIfPossible();
			if (this.fileLog == null) {
				// Still failing, so give up
				return;
			}
		}
		try {
			this.fileLog.write(processedLine + "\n"); // Unix line endings only
			                                          // from
			// Roo
			this.fileLog.flush(); // So tail -f will show it's working
			if (getExitShellRequest() != null) {
				// Shutting down, so close our file (we can always reopen it
				// later if needed)
				this.fileLog
				        .write("// Spring Roo " + versionInfo()
				                + " log closed at "
				                + this.df.format(new Date()) + "\n");
				IOUtils.closeQuietly(this.fileLog);
				this.fileLog = null;
			}
		} catch (final IOException ignoreIt) {
		}
	}

	/**
	 * Obtains the "shell.home" from the system property, falling back to the
	 * current working directory if missing.
	 * 
	 * @return the 'shell.home' system property
	 */
	@Override
	protected String getHomeAsString() {
		String shellHome = System.getProperty("shell.home");
		if (shellHome == null) {
			try {
				shellHome = new File(".").getCanonicalPath();
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return shellHome;
	}

	/**
	 * Should be called by a subclass before deactivating the shell.
	 */
	protected void closeShell() {
		// Notify we're closing down (normally our status is already
		// shutting_down, but if it was a CTRL+C via the o.s.r.bootstrap.Main
		// hook)
		setShellStatus(Status.SHUTTING_DOWN);
		if (this.statusListener != null) {
			removeShellStatusListener(this.statusListener);
		}
	}

	private static class FlashInfo {
		String	flashMessage;
		long	flashMessageUntil;
		Level	flashLevel;
		int		rowNumber;
	}

	public void costomizePlugin() {
		this.historyFileName = getHistoryFileName();
		this.promptText = getPromptText();
		this.version = getBannerText()[0];
		this.welcomeMessage = getBannerText()[1];
	}

	/**
	 * get history file name from provider. The provider has highest order
	 * <link>vnet.sms.common.core.Ordered.getOder</link> will win.
	 * 
	 * @return history file name
	 */
	private String getHistoryFileName() {
		return this.pluginProvidersRegistry.highestPriorityProviderOfType(
		        HistoryFileNameProvider.class).getHistoryFileName();
	}

	/**
	 * get prompt text from provider. The provider has highest order
	 * <link>vnet.sms.common.core.Ordered.getOder</link> will win.
	 * 
	 * @return prompt text
	 */
	private String getPromptText() {
		return this.pluginProvidersRegistry.highestPriorityProviderOfType(
		        PromptProvider.class).getPromptText();
	}

	/**
	 * Get Banner and Welcome Message from provider. The provider has highest
	 * order <link>vnet.sms.common.core.Ordered.getOder</link> will win.
	 * 
	 * @return BannerText[0]: Banner BannerText[1]: Welcome Message.
	 */
	private String[] getBannerText() {
		final String[] bannerText = new String[2];
		final BannerProvider provider = this.pluginProvidersRegistry
		        .highestPriorityProviderOfType(BannerProvider.class);
		bannerText[0] = provider.getBanner();
		bannerText[1] = provider.getWelcomMessage();
		return bannerText;
	}

	/**
	 * get the version information
	 * 
	 */
	@Override
	public String version(final String text) {
		return this.version;
	}

	/**
	 * get the welcome message at start.
	 * 
	 * @return welcome message
	 */
	public String getWelcomeMessage() {
		return this.welcomeMessage;
	}

	/**
	 * @param printBanner
	 *            the printBanner to set
	 */
	public void setPrintBanner(final boolean printBanner) {
		this.printBanner = printBanner;
	}

	/**
	 * @return the historySize
	 */
	public int getHistorySize() {
		return this.historySize;
	}

	/**
	 * @param historySize
	 *            the historySize to set
	 */
	public void setHistorySize(final int historySize) {
		this.historySize = historySize;
	}

}
