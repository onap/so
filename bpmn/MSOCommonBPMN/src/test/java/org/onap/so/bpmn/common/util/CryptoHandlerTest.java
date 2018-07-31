/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved. 
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.BaseTest;

public class CryptoHandlerTest {
	private static final String plainPswd = "mso0206";
	private static final String encryptPwd = "C1FC4A39E16419DD41DFC1212843F440";
	private CryptoHandler cryptoHandler;
	
	@Before
	public void setup() {
		cryptoHandler = new CryptoHandler();
	}
	
	@Test
	@Ignore // ignored until we can mock the properties file.
	public void getMsoAaiPasswordTest() {
		assertEquals(plainPswd, cryptoHandler.getMsoAaiPassword());
	}
	
	@Test
	public void encryptMsoPasswordTest() {
		assertEquals(encryptPwd, cryptoHandler.encryptMsoPassword(plainPswd));
	}
	
	@Test
	public void decryptMsoPasswordTest() {
		assertEquals(plainPswd, cryptoHandler.decryptMsoPassword(encryptPwd));
	}
}