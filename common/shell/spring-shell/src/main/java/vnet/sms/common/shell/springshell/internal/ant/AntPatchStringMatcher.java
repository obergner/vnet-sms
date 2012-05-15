package vnet.sms.common.shell.springshell.internal.ant;

import java.util.Map;

/**
 * Package-protected helper class for {@link AntPathMatcher}. Tests whether or
 * not a string matches against a pattern. The pattern may contain special
 * characters:<br>
 * '*' means zero or more characters<br>
 * '?' means one and only one character, '{' and '}' indicate a uri template
 * pattern
 * 
 * @author Arjen Poutsma
 * @since 3.0
 */
class AntPatchStringMatcher {

	// Fields
	private final char[]	          patArr;

	private final char[]	          strArr;

	private int	                      patIdxStart	= 0;

	private int	                      patIdxEnd;

	private int	                      strIdxStart	= 0;

	private int	                      strIdxEnd;

	private char	                  ch;

	private final Map<String, String>	uriTemplateVariables;

	/** Constructs a new instance of the <code>AntPatchStringMatcher</code>. */
	AntPatchStringMatcher(final String pattern, final String str,
	        final Map<String, String> uriTemplateVariables) {
		this.patArr = pattern.toCharArray();
		this.strArr = str.toCharArray();
		this.uriTemplateVariables = uriTemplateVariables;
		this.patIdxEnd = this.patArr.length - 1;
		this.strIdxEnd = this.strArr.length - 1;
	}

	private void addTemplateVariable(final int curlyIdxStart,
	        final int curlyIdxEnd, final int valIdxStart, final int valIdxEnd) {
		if (this.uriTemplateVariables != null) {
			final String varName = new String(this.patArr, curlyIdxStart + 1,
			        curlyIdxEnd - curlyIdxStart - 1);
			final String varValue = new String(this.strArr, valIdxStart,
			        valIdxEnd - valIdxStart + 1);
			this.uriTemplateVariables.put(varName, varValue);
		}
	}

	/**
	 * Main entry point.
	 * 
	 * @return <code>true</code> if the string matches against the pattern, or
	 *         <code>false</code> otherwise.
	 */
	boolean matchStrings() {
		if (shortcutPossible()) {
			return doShortcut();
		}
		if (patternContainsOnlyStar()) {
			return true;
		}
		if (patternContainsOneTemplateVariable()) {
			addTemplateVariable(0, this.patIdxEnd, 0, this.strIdxEnd);
			return true;
		}
		if (!matchBeforeFirstStarOrCurly()) {
			return false;
		}
		if (allCharsUsed()) {
			return onlyStarsLeft();
		}
		if (!matchAfterLastStarOrCurly()) {
			return false;
		}
		if (allCharsUsed()) {
			return onlyStarsLeft();
		}
		// Process pattern between stars. padIdxStart and patIdxEnd point always
		// to a '*'.
		while ((this.patIdxStart != this.patIdxEnd)
		        && (this.strIdxStart <= this.strIdxEnd)) {
			int patIdxTmp;
			if (this.patArr[this.patIdxStart] == '{') {
				patIdxTmp = findClosingCurly();
				addTemplateVariable(this.patIdxStart, patIdxTmp,
				        this.strIdxStart, this.strIdxEnd);
				this.patIdxStart = patIdxTmp + 1;
				this.strIdxStart = this.strIdxEnd + 1;
				continue;
			}
			patIdxTmp = findNextStarOrCurly();
			if (consecutiveStars(patIdxTmp)) {
				continue;
			}
			// Find the pattern between padIdxStart & padIdxTmp in str between
			// strIdxStart & strIdxEnd
			final int patLength = (patIdxTmp - this.patIdxStart - 1);
			final int strLength = (this.strIdxEnd - this.strIdxStart + 1);
			int foundIdx = -1;
			strLoop: for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					this.ch = this.patArr[this.patIdxStart + j + 1];
					if (this.ch != '?') {
						if (this.ch != this.strArr[this.strIdxStart + i + j]) {
							continue strLoop;
						}
					}
				}

				foundIdx = this.strIdxStart + i;
				break;
			}

			if (foundIdx == -1) {
				return false;
			}

			this.patIdxStart = patIdxTmp;
			this.strIdxStart = foundIdx + patLength;
		}

		return onlyStarsLeft();
	}

	private boolean consecutiveStars(final int patIdxTmp) {
		if ((patIdxTmp == this.patIdxStart + 1)
		        && (this.patArr[this.patIdxStart] == '*')
		        && (this.patArr[patIdxTmp] == '*')) {
			// Two stars next to each other, skip the first one.
			this.patIdxStart++;
			return true;
		}
		return false;
	}

	private int findNextStarOrCurly() {
		for (int i = this.patIdxStart + 1; i <= this.patIdxEnd; i++) {
			if ((this.patArr[i] == '*') || (this.patArr[i] == '{')) {
				return i;
			}
		}
		return -1;
	}

	private int findClosingCurly() {
		for (int i = this.patIdxStart + 1; i <= this.patIdxEnd; i++) {
			if (this.patArr[i] == '}') {
				return i;
			}
		}
		return -1;
	}

	private boolean onlyStarsLeft() {
		for (int i = this.patIdxStart; i <= this.patIdxEnd; i++) {
			if (this.patArr[i] != '*') {
				return false;
			}
		}
		return true;
	}

	private boolean allCharsUsed() {
		return this.strIdxStart > this.strIdxEnd;
	}

	private boolean shortcutPossible() {
		for (final char ch : this.patArr) {
			if ((ch == '*') || (ch == '{') || (ch == '}')) {
				return false;
			}
		}
		return true;
	}

	private boolean doShortcut() {
		if (this.patIdxEnd != this.strIdxEnd) {
			return false; // Pattern and string do not have the same size
		}
		for (int i = 0; i <= this.patIdxEnd; i++) {
			this.ch = this.patArr[i];
			if (this.ch != '?') {
				if (this.ch != this.strArr[i]) {
					return false;// Character mismatch
				}
			}
		}
		return true; // String matches against pattern
	}

	private boolean patternContainsOnlyStar() {
		return ((this.patIdxEnd == 0) && (this.patArr[0] == '*'));
	}

	private boolean patternContainsOneTemplateVariable() {
		if (((this.patIdxEnd >= 2) && (this.patArr[0] == '{') && (this.patArr[this.patIdxEnd] == '}'))) {
			for (int i = 1; i < this.patIdxEnd; i++) {
				if (this.patArr[i] == '}') {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private boolean matchBeforeFirstStarOrCurly() {
		while (((this.ch = this.patArr[this.patIdxStart]) != '*')
		        && (this.ch != '{') && (this.strIdxStart <= this.strIdxEnd)) {
			if (this.ch != '?') {
				if (this.ch != this.strArr[this.strIdxStart]) {
					return false;
				}
			}
			this.patIdxStart++;
			this.strIdxStart++;
		}
		return true;
	}

	private boolean matchAfterLastStarOrCurly() {
		while (((this.ch = this.patArr[this.patIdxEnd]) != '*')
		        && (this.ch != '}') && (this.strIdxStart <= this.strIdxEnd)) {
			if (this.ch != '?') {
				if (this.ch != this.strArr[this.strIdxEnd]) {
					return false;
				}
			}
			this.patIdxEnd--;
			this.strIdxEnd--;
		}
		return true;
	}
}
