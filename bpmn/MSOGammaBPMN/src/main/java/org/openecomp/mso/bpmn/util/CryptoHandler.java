/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.util;

import java.security.GeneralSecurityException;

public class CryptoHandler implements ICryptoHandler {

	private static String msoKey = "aa3871669d893c7fb8abbcda31b88b4f";
	//private static String msoAaiPwd = "mso0206";
	private static String msoAaiEncryptedPwd = "C1FC4A39E16419DD41DFC1212843F440";

	public String getMsoAaiPassword() {
		try {
			return CryptoUtils.decrypt(msoAaiEncryptedPwd, msoKey);
		} catch (GeneralSecurityException e) {
			return null;
		}
	}


	public String encryptMsoPassword(String plainMsoPwd) {
		try {
			return CryptoUtils.encrypt(plainMsoPwd, msoKey);
		} catch (GeneralSecurityException e) {
			return null;
		}
	}


	public String decryptMsoPassword(String encryptedPwd) {
		try {
			return CryptoUtils.decrypt(encryptedPwd, msoKey);
		} catch (GeneralSecurityException e) {
			return null;
		}
	}

}
