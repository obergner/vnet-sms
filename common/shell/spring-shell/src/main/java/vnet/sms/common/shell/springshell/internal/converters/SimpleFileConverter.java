package vnet.sms.common.shell.springshell.internal.converters;

import java.io.File;

import vnet.sms.common.shell.springshell.Shell;

public class SimpleFileConverter extends FileConverter {

	private Shell	shell;

	@Override
	protected File getWorkingDirectory() {
		return this.shell.getHome();
	}
}
