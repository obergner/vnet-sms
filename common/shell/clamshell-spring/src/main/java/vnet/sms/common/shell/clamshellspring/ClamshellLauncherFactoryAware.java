/**
 * 
 */
package vnet.sms.common.shell.clamshellspring;

/**
 * @author obergner
 * 
 */
public interface ClamshellLauncherFactoryAware {

	void setClamshellLauncherFactory(
	        ClamshellLauncher.Factory clamshellLauncherFactory);
}
