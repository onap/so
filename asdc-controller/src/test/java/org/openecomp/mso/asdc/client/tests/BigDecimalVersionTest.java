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

package org.openecomp.mso.asdc.client.tests;


import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import org.openecomp.mso.asdc.installer.BigDecimalVersion;


public class BigDecimalVersionTest {

    @Test
    public final void versionCastTest() {

        BigDecimal versionDecimal = BigDecimalVersion.castAndCheckNotificationVersion("12.0");
        assertTrue(versionDecimal.equals(new BigDecimal("12.0")));
        assertTrue("12.0".equals(BigDecimalVersion.castAndCheckNotificationVersionToString("12.0")));

        versionDecimal = BigDecimalVersion.castAndCheckNotificationVersion("12.0.2");
        assertTrue(versionDecimal.equals(new BigDecimal("12.02")));
        assertTrue("12.02".equals(BigDecimalVersion.castAndCheckNotificationVersionToString("12.0.2")));

        versionDecimal = BigDecimalVersion.castAndCheckNotificationVersion("10");
        assertTrue(versionDecimal.equals(new BigDecimal("10")));
        assertTrue("10".equals(BigDecimalVersion.castAndCheckNotificationVersionToString("10")));

        versionDecimal = BigDecimalVersion.castAndCheckNotificationVersion("10.1.2.6");
        assertTrue(versionDecimal.equals(new BigDecimal("10.126")));
        assertTrue("10.126".equals(BigDecimalVersion.castAndCheckNotificationVersionToString("10.1.2.6")));

    }
}
