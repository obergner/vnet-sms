/**
 * 
 */
package vnet.sms.common.shell.clamshellspring;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author obergner
 * 
 */
public interface ClamshellLauncher {

	public interface Factory {

		ClamshellLauncher newLauncher();
	}

	void launch(InputStream input, OutputStream output);
}
