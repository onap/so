/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.sdnc.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class CallbackHeaderTest {

    static CallbackHeader cb = new CallbackHeader();

    @Test
    public final void testCallbackHeader() {
        cb.setRequestId("413658f4-7f42-482e-b834-23a5c15657da-1474471336781");
        cb.setResponseCode("200");
        cb.setResponseMessage("OK");
        assertNotNull(cb.getRequestId());
        assertNotNull(cb.getResponseCode());
        assertNotNull(cb.getResponseMessage());
        assertEquals("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", cb.getRequestId());
        assertEquals("200", cb.getResponseCode());
        assertEquals("OK", cb.getResponseMessage());
    }

    @Test
    public void testtoString() {
        assertNotNull(cb.toString());
    }
}
