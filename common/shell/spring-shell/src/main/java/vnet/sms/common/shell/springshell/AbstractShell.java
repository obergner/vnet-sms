package vnet.sms.common.shell.springshell;

import static vnet.sms.common.shell.springshell.internal.util.StringUtils.LINE_SEPARATOR;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import vnet.sms.common.shell.springshell.command.CliCommand;
import vnet.sms.common.shell.springshell.command.CliOption;
import vnet.sms.common.shell.springshell.event.AbstractShellStatusPublisher;
import vnet.sms.common.shell.springshell.event.ShellStatus;
import vnet.sms.common.shell.springshell.event.ShellStatus.Status;
import vnet.sms.common.shell.springshell.internal.logging.HandlerUtils;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.IOUtils;
import vnet.sms.common.shell.springshell.internal.util.MathUtils;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;
import vnet.sms.common.shell.springshell.internal.util.VersionUtils;

/**
 * Provides a base {@link Shell} implementation.
 * 
 * @author Ben Alex
 */
public abstract class AbstractShell extends AbstractShellStatusPublisher
        implements Shell {

	// Constants
	private static final String	  MY_SLOT	       = AbstractShell.class
	                                                       .getName();

	protected static final String	DEFAULT_PROMPT	= "vnet-sms> ";

	public static String	      completionKeys	= "TAB";

	// Instance fields
	protected final Logger	      logger	       = HandlerUtils
	                                                       .getLogger(getClass());

	protected String	          shellPrompt	   = DEFAULT_PROMPT;

	protected boolean	          inBlockComment;

	protected ExitShellRequest	  exitShellRequest;

	/**
	 * Returns any classpath resources with the given path
	 * 
	 * @param path
	 *            the path for which to search (never null)
	 * @return <code>null</code> if the search can't be performed
	 * @since 1.2.0
	 */
	protected abstract Collection<URL> findResources(String path);

	protected abstract String getHomeAsString();

	protected abstract ExecutionStrategy getExecutionStrategy();

	protected abstract Parser getParser();

	@CliCommand(value = { "script" }, help = "Parses the specified resource file and executes its commands")
	public void script(
	        @CliOption(key = { "", "file" }, help = "The file to locate and execute", mandatory = true) final File script,
	        @CliOption(key = "lineNumbers", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "Display line numbers when executing the script") final boolean lineNumbers) {

		Assert.notNull(script, "Script file to parse is required");
		final double startedNanoseconds = System.nanoTime();
		final InputStream inputStream = openScript(script);

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			int i = 0;
			while ((line = in.readLine()) != null) {
				i++;
				if (lineNumbers) {
					this.logger.fine("Line " + i + ": " + line);
				} else {
					this.logger.fine(line);
				}
				if (!"".equals(line.trim())) {
					final boolean success = executeScriptLine(line);
					if (success
					        && ((line.trim().startsWith("q") || line.trim()
					                .startsWith("ex")))) {
						break;
					} else if (!success) {
						// Abort script processing, given something went wrong
						throw new IllegalStateException(
						        "Script execution aborted");
					}
				}
			}
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		} finally {
			IOUtils.closeQuietly(inputStream, in);
			final double executionDurationInSeconds = (System.nanoTime() - startedNanoseconds) / 1000000000D;
			this.logger.fine("Script required "
			        + MathUtils.round(executionDurationInSeconds, 3)
			        + " seconds to execute");
		}
	}

	/**
	 * Opens the given script for reading
	 * 
	 * @param script
	 *            the script to read (required)
	 * @return a non-<code>null</code> input stream
	 */
	private InputStream openScript(final File script) {
		try {
			return new BufferedInputStream(new FileInputStream(script));
		} catch (final FileNotFoundException fnfe) {
			// Try to find the script via the classloader
			final Collection<URL> urls = findResources(script.getName());

			// Handle search failure
			Assert.notNull(urls,
			        "Unexpected error looking for '" + script.getName() + "'");

			// Handle the search being OK but the file simply not being present
			Assert.notEmpty(urls, "Script '" + script
			        + "' not found on disk or in classpath");
			Assert.isTrue(urls.size() == 1, "More than one '" + script
			        + "' was found in the classpath; unable to continue");
			try {
				return urls.iterator().next().openStream();
			} catch (final IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Execute the single line from a script.
	 * <p>
	 * This method can be overridden by sub-classes to pre-process script lines.
	 */
	protected boolean executeScriptLine(final String line) {
		return executeCommand(line);
	}

	@Override
	public boolean executeCommand(String line) {
		// Another command was attempted
		setShellStatus(ShellStatus.Status.PARSING);

		final ExecutionStrategy executionStrategy = getExecutionStrategy();
		boolean flashedMessage = false;
		while ((executionStrategy == null)
		        || !executionStrategy.isReadyForCommands()) {
			// Wait
			try {
				Thread.sleep(500);
			} catch (final InterruptedException ignore) {
			}
			if (!flashedMessage) {
				flash(Level.INFO, "Please wait - still loading", MY_SLOT);
				flashedMessage = true;
			}
		}
		if (flashedMessage) {
			flash(Level.INFO, "", MY_SLOT);
		}

		ParseResult parseResult = null;
		try {
			// We support simple block comments; ie a single pair per line
			if (!this.inBlockComment && line.contains("/*")
			        && line.contains("*/")) {
				blockCommentBegin();
				final String lhs = line.substring(0, line.lastIndexOf("/*"));
				if (line.contains("*/")) {
					line = lhs + line.substring(line.lastIndexOf("*/") + 2);
					blockCommentFinish();
				} else {
					line = lhs;
				}
			}
			if (this.inBlockComment) {
				if (!line.contains("*/")) {
					return true;
				}
				blockCommentFinish();
				line = line.substring(line.lastIndexOf("*/") + 2);
			}
			// We also support inline comments (but only at start of line,
			// otherwise valid
			// command options like http://www.helloworld.com will fail as per
			// ROO-517)
			if (!this.inBlockComment
			        && (line.trim().startsWith("//") || line.trim().startsWith(
			                "#"))) { // # support in ROO-1116
				line = "";
			}
			// Convert any TAB characters to whitespace (ROO-527)
			line = line.replace('\t', ' ');
			if ("".equals(line.trim())) {
				setShellStatus(Status.EXECUTION_SUCCESS);
				return true;
			}
			parseResult = getParser().parse(line);
			if (parseResult == null) {
				return false;
			}

			setShellStatus(Status.EXECUTING);
			final Object result = executionStrategy.execute(parseResult);
			setShellStatus(Status.EXECUTION_RESULT_PROCESSING);
			if (result != null) {
				if (result instanceof ExitShellRequest) {
					this.exitShellRequest = (ExitShellRequest) result;
					// Give ProcessManager a chance to close down its threads
					// before the overall OSGi framework is terminated
					// (ROO-1938)
					executionStrategy.terminate();
				} else if (result instanceof Iterable<?>) {
					for (final Object o : (Iterable<?>) result) {
						this.logger.info(o.toString());
					}
				} else {
					this.logger.info(result.toString());
				}
			}

			logCommandIfRequired(line, true);
			setShellStatus(Status.EXECUTION_SUCCESS, line, parseResult);
			return true;
		} catch (final RuntimeException e) {
			setShellStatus(Status.EXECUTION_FAILED, line, parseResult);
			// We rely on execution strategy to log it
			try {
				logCommandIfRequired(line, false);
			} catch (final Exception ignored) {
			}
			return false;
		} finally {
			setShellStatus(Status.USER_INPUT);
		}
	}

	/**
	 * Allows a subclass to log the execution of a well-formed command. This is
	 * invoked after a command has completed, and indicates whether the command
	 * returned normally or returned an exception. Note that attempted commands
	 * that are not well-formed (eg they are missing a mandatory argument) will
	 * never be presented to this method, as the command execution is never
	 * actually attempted in those cases. This method is only invoked if an
	 * attempt is made to execute a particular command.
	 * 
	 * <p>
	 * Implementations should consider specially handling the "script" commands,
	 * and also indicating whether a command was successful or not.
	 * Implementations that wish to behave consistently with other
	 * {@link AbstractShell} subclasses are encouraged to simply override
	 * {@link #logCommandToOutput(String)} instead, and only override this
	 * method if you actually need to fine-tune the output logic.
	 * 
	 * @param line
	 *            the parsed line (any comments have been removed; never null)
	 * @param successful
	 *            if the command was successful or not
	 */
	protected void logCommandIfRequired(final String line,
	        final boolean successful) {
		if (line.startsWith("script")) {
			logCommandToOutput((successful ? "// " : "// [failed] ") + line);
		} else {
			logCommandToOutput((successful ? "" : "// [failed] ") + line);
		}
	}

	/**
	 * Allows a subclass to actually write the resulting logged command to some
	 * form of output. This frees subclasses from needing to implement the logic
	 * within {@link #logCommandIfRequired(String, boolean)}.
	 * 
	 * <p>
	 * Implementations should invoke {@link #getExitShellRequest()} to monitor
	 * any attempts to exit the shell and release resources such as output log
	 * files.
	 * 
	 * @param processedLine
	 *            the line that should be appended to some type of output
	 *            (excluding the \n character)
	 */
	protected void logCommandToOutput(final String processedLine) {
	}

	/**
	 * Base implementation of the {@link Shell#setPromptPath(String)} method,
	 * designed for simple shell implementations. Advanced implementations (eg
	 * those that support ANSI codes etc) will likely want to override this
	 * method and set the {@link #shellPrompt} variable directly.
	 * 
	 * @param path
	 *            to set (can be null or empty; must NOT be formatted in any
	 *            special way eg ANSI codes)
	 */
	@Override
	public void setPromptPath(final String path) {
		if ((path == null) || "".equals(path)) {
			this.shellPrompt = DEFAULT_PROMPT;
		} else {
			this.shellPrompt = path + " " + DEFAULT_PROMPT;
		}
	}

	/**
	 * Default implementation of {@link Shell#setPromptPath(String, boolean))}
	 * method to satisfy STS compatibility.
	 * 
	 * @param path
	 *            to set (can be null or empty)
	 * @param overrideStyle
	 */
	@Override
	public void setPromptPath(final String path, final boolean overrideStyle) {
		setPromptPath(path);
	}

	@Override
	public ExitShellRequest getExitShellRequest() {
		return this.exitShellRequest;
	}

	@CliCommand(value = { "//", ";" }, help = "Inline comment markers (start of line only)")
	public void inlineComment() {
	}

	@CliCommand(value = { "/*" }, help = "Start of block comment")
	public void blockCommentBegin() {
		Assert.isTrue(!this.inBlockComment,
		        "Cannot open a new block comment when one already active");
		this.inBlockComment = true;
	}

	@CliCommand(value = { "*/" }, help = "End of block comment")
	public void blockCommentFinish() {
		Assert.isTrue(this.inBlockComment,
		        "Cannot close a block comment when it has not been opened");
		this.inBlockComment = false;
	}

	@CliCommand(value = { "system properties" }, help = "Shows the shell's properties")
	public String props() {
		final Set<String> data = new TreeSet<String>(); // For repeatability
		for (final Entry<Object, Object> entry : System.getProperties()
		        .entrySet()) {
			data.add(entry.getKey() + " = " + entry.getValue());
		}

		return StringUtils.collectionToDelimitedString(data, LINE_SEPARATOR)
		        + LINE_SEPARATOR;
	}

	@CliCommand(value = { "date" }, help = "Displays the local date and time")
	public String date() {
		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL,
		        Locale.US).format(new Date());
	}

	// @CliCommand(value = { "version" }, help = "Displays shell version")
	public String version(
	        @CliOption(key = "", help = "Special version flags") final String extra) {
		final StringBuilder sb = new StringBuilder();

		// @formatter:off
		sb.append("  ----   ---- -----   ---------- -----------           ---------   ----     ---------            ").append(LINE_SEPARATOR);
		sb.append("  \\   \\ /   /\\      \\ \\_   _____/\\__    ___/          /   _____/  /     \\  /   _____/            ").append(LINE_SEPARATOR);
		sb.append("   \\   Y   / /   |   \\ |    __)_   |    |     ______  \\_____  \\  /  \\ /  \\ \\_____  \\             ").append(LINE_SEPARATOR);
		sb.append("    \\     / /    |    \\|        \\  |    |    /_____/  /        \\/    Y    \\/        \\            ").append(LINE_SEPARATOR);
		sb.append("     \\___/  \\____|__  /_______  /  |____|            /_______  /\\____|__  /_______  /            ").append(LINE_SEPARATOR);
		sb.append("                    \\/        \\/                             \\/         \\/        \\/             ").append(" ").append(versionInfo()).append(LINE_SEPARATOR);
		sb.append(LINE_SEPARATOR);
		// @formatter:on

		return sb.toString();
	}

	public String versionInfo() {
		return VersionUtils.versionInfo();
	}

	@Override
	public String getShellPrompt() {
		return this.shellPrompt;
	}

	/**
	 * Obtains the home directory for the current shell instance.
	 * 
	 * <p>
	 * Note: calls the {@link #getHomeAsString()} method to allow subclasses to
	 * provide the home directory location as string using different
	 * environment-specific strategies.
	 * 
	 * <p>
	 * If the path indicated by {@link #getHomeAsString()} exists and refers to
	 * a directory, that directory is returned.
	 * 
	 * <p>
	 * If the path indicated by {@link #getHomeAsString()} exists and refers to
	 * a file, an exception is thrown.
	 * 
	 * <p>
	 * If the path indicated by {@link #getHomeAsString()} does not exist, it
	 * will be created as a directory. If this fails, an exception will be
	 * thrown.
	 * 
	 * @return the home directory for the current shell instance (which is
	 *         guaranteed to exist and be a directory)
	 */
	@Override
	public File getHome() {
		final String shellHome = getHomeAsString();
		final File f = new File(shellHome);
		Assert.isTrue(!f.exists() || (f.exists() && f.isDirectory()), "Path '"
		        + f.getAbsolutePath()
		        + "' must be a directory, or it must not exist");
		if (!f.exists()) {
			f.mkdirs();
		}
		Assert.isTrue(
		        f.exists() && f.isDirectory(),
		        "Path '"
		                + f.getAbsolutePath()
		                + "' is not a directory; please specify shell.home system property correctly");
		return f;
	}

	/**
	 * Simple implementation of {@link #flash(Level, String, String)} that
	 * simply displays the message via the logger. It is strongly recommended
	 * shell implementations override this method with a more effective
	 * approach.
	 */
	@Override
	public void flash(final Level level, final String message, final String slot) {
		Assert.notNull(level, "Level is required for a flash message");
		Assert.notNull(message, "Message is required for a flash message");
		Assert.hasText(slot, "Slot name must be specified for a flash message");
		if (!("".equals(message))) {
			this.logger.log(level, message);
		}
	}
}
