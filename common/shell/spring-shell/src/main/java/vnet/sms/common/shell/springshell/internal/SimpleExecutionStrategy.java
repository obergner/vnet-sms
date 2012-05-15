package vnet.sms.common.shell.springshell.internal;

import vnet.sms.common.shell.springshell.ExecutionStrategy;
import vnet.sms.common.shell.springshell.ParseResult;
import vnet.sms.common.shell.springshell.internal.util.Assert;
import vnet.sms.common.shell.springshell.internal.util.ReflectionUtils;

public class SimpleExecutionStrategy implements ExecutionStrategy {

	private final Class<?>	mutex	= SimpleExecutionStrategy.class;

	@Override
	public Object execute(final ParseResult parseResult)
	        throws RuntimeException {
		Assert.notNull(parseResult, "Parse result required");
		synchronized (this.mutex) {
			Assert.isTrue(isReadyForCommands(),
			        "SimpleExecutionStrategy not yet ready for commands");
			return ReflectionUtils.invokeMethod(parseResult.getMethod(),
			        parseResult.getInstance(), parseResult.getArguments());
		}
	}

	@Override
	public boolean isReadyForCommands() {
		return true;
	}

	@Override
	public void terminate() {
	}
}
