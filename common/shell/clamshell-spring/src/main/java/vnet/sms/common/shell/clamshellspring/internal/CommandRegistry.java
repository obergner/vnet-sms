/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import java.util.List;
import java.util.Map;

import org.clamshellcli.api.Command;

/**
 * @author obergner
 * 
 */
interface CommandRegistry {

	List<Command> getCommands();

	List<Command> getCommandsByNamespace(final String namespace);

	Map<String, Command> mapCommands(final List<Command> commands);
}
