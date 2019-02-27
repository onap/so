/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import org.onap.so.utils.CryptoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoHandler implements ICryptoHandler {
	private static final Logger logger = LoggerFactory.getLogger(CryptoHandler.class);
	private static final String GENERAL_SECURITY_EXCEPTION_PREFIX = "GeneralSecurityException :";
	private static final String MSO_KEY = "aa3871669d893c7fb8abbcda31b88b4f";
	private static final String PROPERTY_KEY = "mso.AaiEncrypted.Pwd";

    @Override
	public String getMsoAaiPassword() {
    	Properties keyProp = new Properties ();
		try {
			keyProp.load (Thread.currentThread ().getContextClassLoader ().getResourceAsStream ("urn.properties"));
			return CryptoUtils.decrypt((String) keyProp.get(PROPERTY_KEY), MSO_KEY);
		} catch (GeneralSecurityException | IOException e) {
			logger.error(GENERAL_SECURITY_EXCEPTION_PREFIX + e.getMessage(), e);
			return null;
		}
	}


	@Override
	public String encryptMsoPassword(String plainMsoPwd) {
		try {
			return CryptoUtils.encrypt(plainMsoPwd, MSO_KEY);
		} catch (GeneralSecurityException e) {
			logger.error(GENERAL_SECURITY_EXCEPTION_PREFIX + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public String decryptMsoPassword(String encryptedPwd) {
		try {
			return CryptoUtils.decrypt(encryptedPwd, MSO_KEY);
		} catch (GeneralSecurityException e) {
			logger.error(GENERAL_SECURITY_EXCEPTION_PREFIX + e.getMessage(), e);
			return null;
		}
	}
}
