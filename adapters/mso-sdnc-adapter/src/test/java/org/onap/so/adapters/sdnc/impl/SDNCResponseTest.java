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
package org.onap.so.adapters.sdnc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class SDNCResponseTest {

    private SDNCResponse sdncresponse = new SDNCResponse(null, 0, null);

    @Test
    public void testSDNCResponse() {
        sdncresponse.setReqId("reqId");
        sdncresponse.setRespCode(0);
        sdncresponse.setRespMsg("respMsg");
        sdncresponse.setSdncRespXml("sdncRespXml");
        assertEquals(sdncresponse.getReqId(), "reqId");
        assertEquals(sdncresponse.getRespCode(), 0);
        assertEquals(sdncresponse.getRespMsg(), "respMsg");
        assertEquals(sdncresponse.getSdncRespXml(), "sdncRespXml");
    }

    @Test
    public void testtoString() {
        assertNotNull(sdncresponse.toString());
    }
}

