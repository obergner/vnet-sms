package vnet.sms.common.shell.springshell.internal.commands;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import vnet.sms.common.shell.springshell.AbstractShell;
import vnet.sms.common.shell.springshell.internal.util.StringUtils;

public class ResourceBundleHintOperations implements HintOperations {

	private static ResourceBundle	bundle	= ResourceBundle
	                                               .getBundle(HintCommands.class
	                                                       .getName());

	@Override
	public String hint(String topic) {
		if (StringUtils.isBlank(topic)) {
			topic = determineTopic();
		}
		try {
			final String message = bundle.getString(topic);
			return message.replace("\r", StringUtils.LINE_SEPARATOR).replace(
			        "${completion_key}", AbstractShell.completionKeys);
		} catch (final MissingResourceException exception) {
			return "Cannot find topic '" + topic + "'";
		}

	}

	@Override
	public SortedSet<String> getCurrentTopics() {
		final SortedSet<String> result = new TreeSet<String>();
		final String topic = determineTopic();
		if ("general".equals(topic)) {
			for (final Enumeration<String> keys = bundle.getKeys(); keys
			        .hasMoreElements();) {
				result.add(keys.nextElement());
			}
			// result.addAll(bundle.keySet()); ResourceBundle.keySet() method in
			// JDK 6+
		} else {
			result.add(topic);
		}
		return result;
	}

	private String determineTopic() {
		return "start";
	}
}
