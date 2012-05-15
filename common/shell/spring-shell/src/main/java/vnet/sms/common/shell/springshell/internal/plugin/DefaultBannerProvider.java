/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vnet.sms.common.shell.springshell.internal.plugin;

import static vnet.sms.common.shell.springshell.internal.util.StringUtils.LINE_SEPARATOR;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import vnet.sms.common.shell.springshell.Constant;
import vnet.sms.common.shell.springshell.command.CommandMarker;
import vnet.sms.common.shell.springshell.internal.util.VersionUtils;
import vnet.sms.common.shell.springshell.plugin.BannerProvider;

/**
 * Default Banner provider.
 * 
 * @author Jarred Li
 * 
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultBannerProvider implements BannerProvider, CommandMarker {

	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public String getBanner() {
		final StringBuilder sb = new StringBuilder();

		// @formatter:off
		sb.append("                                                                                             ").append(LINE_SEPARATOR);
		sb.append("  ----   ---- -----   ---------- -----------           ---------   ----     ---------        ").append(LINE_SEPARATOR);
		sb.append("  \\   \\ /   /\\      \\ \\_   _____/\\__    ___/          /   _____/  /     \\  /   _____/ ").append(LINE_SEPARATOR);
		sb.append("   \\   Y   / /   |   \\ |    __)_   |    |     ______  \\_____  \\  /  \\ /  \\ \\_____  \\ ").append(LINE_SEPARATOR);
		sb.append("    \\     / /    |    \\|        \\  |    |    /_____/  /        \\/    Y    \\/        \\  ").append(LINE_SEPARATOR);
		sb.append("     \\___/  \\____|__  /_______  /  |____|            /_______  /\\____|__  /_______  /     ").append(LINE_SEPARATOR);
		sb.append("                    \\/        \\/                             \\/         \\/        \\/    ").append(" ").append(getVersion()).append(LINE_SEPARATOR);
		sb.append(LINE_SEPARATOR);
		// @formatter:on

		return sb.toString();
	}

	@Override
	public String getVersion() {
		return VersionUtils.versionInfo();
	}

	@Override
	public String getWelcomMessage() {
		return Constant.WELCOME_MESSAGE;
	}

	@Override
	public String name() {
		return "default banner provider";
	}
}
