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

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import vnet.sms.common.shell.springshell.Constant;
import vnet.sms.common.shell.springshell.plugin.HistoryFileNameProvider;

/**
 * Default history file provider. Default file is
 * {@link vnet.sms.common.shell.springshell.Constant.HISTORY_FILE_NAME}
 * 
 * @author Jarred Li
 * 
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultHistoryFileNameProvider implements HistoryFileNameProvider {

	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public String getHistoryFileName() {
		return Constant.HISTORY_FILE_NAME;
	}

	@Override
	public String name() {
		return "default history file name provider";
	}
}
