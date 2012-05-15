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

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import vnet.sms.common.shell.springshell.Constant;
import vnet.sms.common.shell.springshell.internal.plugin.DefaultPromptProvider;
import vnet.sms.common.shell.springshell.plugin.PromptProvider;

/**
 * @author Jarred Li
 * 
 */
public class DefaultPromptProviderTest {

	private static PromptProvider	prompt;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		prompt = new DefaultPromptProvider();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		prompt = null;
	}

	/**
	 * Test method for
	 * {@link vnet.sms.common.shell.springshell.internal.plugin.DefaultPromptProvider#getPromptText()}
	 * .
	 */
	@Test
	public void testGetPromptText() {
		assertEquals(Constant.COMMAND_LINE_PROMPT, prompt.getPromptText());
	}

}
