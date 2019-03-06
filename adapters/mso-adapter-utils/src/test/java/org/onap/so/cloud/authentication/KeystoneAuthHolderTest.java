/*
 * ============LICENSE_START==========================================
 *  ONAP - SO
 * ===================================================================
 *  Copyright (c) 2019 IBM.
 * ===================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 * ============LICENSE_END=============================================
 * ====================================================================
 */
package org.onap.so.cloud.authentication;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class KeystoneAuthHolderTest {

    private KeystoneAuthHolder keystoneAuthHolder;
    private Calendar calendar = Calendar.getInstance();

    @Before
    public void setUp() {
        keystoneAuthHolder = new KeystoneAuthHolder();
    }

    @Test
    public void testGetId() {
        keystoneAuthHolder.setId("001");
        assertEquals("001", keystoneAuthHolder.getId());
    }

    @Test
    public void testGetexpiration() {
        keystoneAuthHolder.setexpiration(calendar);
        assertEquals(calendar, keystoneAuthHolder.getexpiration());
    }

    @Test
    public void testGetServiceUrl() {
        keystoneAuthHolder.setHeatUrl("testURL");
        assertEquals("testURL", keystoneAuthHolder.getServiceUrl());
    }

}
