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


package org.onap.so.adapters.sdnc.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class SDNCAdapterCallbackRequestTest {

    static SDNCAdapterCallbackRequest sdc = new SDNCAdapterCallbackRequest();
    static CallbackHeader ch = new CallbackHeader("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", "200", "OK");

    @Test
    public void testSDNCAdapterCallbackRequest() {
        sdc.setCallbackHeader(ch);
        sdc.setRequestData("data");
        assertNotNull(sdc.getCallbackHeader());
        assertNotNull(sdc.getRequestData());
        assertEquals(ch, sdc.getCallbackHeader());
        assertEquals("data", sdc.getRequestData());

    }

    @Test
    public void testtoString() {
        assertNotNull(ch.toString());
        assertNotNull(sdc.toString());
    }

}


