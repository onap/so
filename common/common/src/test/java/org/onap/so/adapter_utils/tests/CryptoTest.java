/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
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

package org.onap.so.adapter_utils.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.security.GeneralSecurityException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.utils.CryptoUtils;

/**
 * This class implements all test methods of the CryptoUtils features.
 *
 *
 */
public class CryptoTest {

    private static String testKey = "546573746F736973546573746F736973";

    /**
     * This method is called before any test occurs. It creates a fake tree from scratch
     */
    @BeforeClass
    public static final void prepare() {

    }

    /**
     * This method implements a test of tree structure, mainly the storage of the leaves structure.
     * 
     * @throws GeneralSecurityException
     */
    @Test
    public final void testEncryption() throws GeneralSecurityException {
        String hexString = CryptoUtils.byteArrayToHexString("testosistestosi".getBytes());

        final String testData = "This is a test string";
        final String nonTestData = "This is not the right String";

        String encodeString = CryptoUtils.encrypt(testData, testKey);

        assertNotNull(encodeString);

        assertTrue(testData.equals(CryptoUtils.decrypt(encodeString, testKey)));
        assertFalse(nonTestData.equals(CryptoUtils.decrypt(encodeString, testKey)));

        String encode2String = CryptoUtils.encrypt(testData, testKey);
        assertNotNull(encode2String);

        assertEquals(CryptoUtils.decrypt(encodeString, testKey), CryptoUtils.decrypt(encode2String, testKey));

        encodeString = CryptoUtils.encryptCloudConfigPassword(testData);

        assertEquals(testData, CryptoUtils.decryptCloudConfigPassword(encodeString));

        System.out.println(CryptoUtils.encrypt("poBpmn:password1$", "aa3871669d893c7fb8abbcda31b88b4f"));
    }

}
