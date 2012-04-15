/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import static org.apache.commons.lang.Validate.notNull;

import java.io.InputStream;
import java.io.OutputStream;

import org.clamshellcli.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vnet.sms.common.shell.clamshellspring.ClamshellLauncher;

/**
 * @author obergner
 * 
 */
public final class EmbeddedClamshellLauncher implements ClamshellLauncher {

	private final Logger	log	= LoggerFactory.getLogger(getClass());

	private final Context	context;

	/**
	 * @param context
	 */
	public EmbeddedClamshellLauncher(final Context context) {
		notNull(context, "Argument 'context' must not be null");
		this.context = context;
	}

	/**
	 * @see vnet.sms.common.shell.clamshellspring.ClamshellLauncher#launch(java.io.InputStream,
	 *      java.io.OutputStream)
	 */
	@Override
	public void launch(final InputStream input, final OutputStream output) {
		this.log.info(
		        "Launching a new clamshell using input = {} and output = {} ...",
		        input, output);

		this.context.putValue(Context.KEY_INPUT_STREAM, input);
		this.context.putValue(Context.KEY_OUTPUT_STREAM, output);

		this.context.getShell().plug(this.context);

		this.log.info("Clamshell has been terminated");
	}
}
