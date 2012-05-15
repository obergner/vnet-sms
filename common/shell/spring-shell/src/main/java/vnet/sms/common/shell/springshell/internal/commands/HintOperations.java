package vnet.sms.common.shell.springshell.internal.commands;

import java.util.SortedSet;

public interface HintOperations {

	String hint(String topic);

	SortedSet<String> getCurrentTopics();
}
