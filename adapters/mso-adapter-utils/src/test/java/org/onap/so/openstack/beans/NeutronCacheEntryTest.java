/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;

public class NeutronCacheEntryTest {

    private static final String NEUTRON_URL = "testNeutronUrl";
    private static final String TOKEN = "testToken";

    @Test
    public void isExpiredTrueTest() {
        Calendar expires = new GregorianCalendar(2013, 0, 31);
        NeutronCacheEntry neutronCacheEntry = new NeutronCacheEntry(NEUTRON_URL, TOKEN, expires);
        assertTrue(neutronCacheEntry.isExpired());
    }

    @Test
    public void isExpiredFalseTest() {
        Calendar expires = new GregorianCalendar(2100, 0, 31);
        NeutronCacheEntry neutronCacheEntry = new NeutronCacheEntry(NEUTRON_URL, TOKEN, expires);
        assertFalse(neutronCacheEntry.isExpired());
    }

    @Test
    public void isExpiredNullTest() {
        Calendar expires = null;
        NeutronCacheEntry neutronCacheEntry = new NeutronCacheEntry(NEUTRON_URL, TOKEN, expires);
        assertTrue(neutronCacheEntry.isExpired());
    }
}
