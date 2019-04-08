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


import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.so.bpmn.common.adapter.sdnc.RequestHeader;

public class RequestHeaderTest {
    RequestHeader rh = new RequestHeader();

    @Test
    public void testRequestHeader() {
        rh.setRequestId("requestId");
        rh.setSvcInstanceId("svcInstanceId");
        rh.setSvcAction("svcAction");
        rh.setSvcOperation("svcOperation");
        rh.setCallbackUrl("callbackUrl");
        rh.setMsoAction("msoAction");
        assertEquals(rh.getRequestId(), "requestId");
        assertEquals(rh.getSvcInstanceId(), "svcInstanceId");
        assertEquals(rh.getSvcAction(), "svcAction");
        assertEquals(rh.getSvcOperation(), "svcOperation");
        assertEquals(rh.getCallbackUrl(), "callbackUrl");
        assertEquals(rh.getMsoAction(), "msoAction");
        assert (rh.toString() != null);
    }
}
