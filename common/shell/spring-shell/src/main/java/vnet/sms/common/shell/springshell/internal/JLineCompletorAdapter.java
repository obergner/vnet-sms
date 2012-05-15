package vnet.sms.common.shell.springshell.internal;

import java.util.ArrayList;
import java.util.List;

import jline.Completor;
import vnet.sms.common.shell.springshell.Completion;
import vnet.sms.common.shell.springshell.Parser;
import vnet.sms.common.shell.springshell.internal.logging.JLineLogHandler;
import vnet.sms.common.shell.springshell.internal.util.Assert;

/**
 * An implementation of JLine's {@link Completor} interface that delegates to a
 * {@link Parser}.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class JLineCompletorAdapter implements Completor {

	// Fields
	private final Parser	parser;

	public JLineCompletorAdapter(final Parser parser) {
		Assert.notNull(parser, "Parser required");
		this.parser = parser;
	}

	@Override
	public int complete(final String buffer, final int cursor,
	        final List candidates) {
		int result;
		try {
			JLineLogHandler.cancelRedrawProhibition();
			final List<Completion> completions = new ArrayList<Completion>();
			result = this.parser.completeAdvanced(buffer, cursor, completions);
			for (final Completion completion : completions) {
				candidates
				        .add(new jline.Completion(completion.getValue(),
				                completion.getFormattedValue(), completion
				                        .getHeading()));
			}
		} finally {
			JLineLogHandler.prohibitRedraw();
		}
		return result;
	}
}
