/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 IBM.
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

package org.onap.so.db.request.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class InfraRequestsTest {
    
    private InfraRequests infraRequests;
    private Timestamp timeStamp;
    
    @Before
    public void setUp() throws ParseException
    {
        infraRequests= new InfraRequests() {
        };
        infraRequests.setRequestId("testRequestId");
        infraRequests.setClientRequestId("testClientRequestId");
        infraRequests.setAction("testAction");
        infraRequests.setAaiServiceId("testAAiServiceId");
        infraRequests.setAicCloudRegion("testaaiclouderegion");
        infraRequests.setAicNodeClli("testAicNodeClli");
        infraRequests.setCallBackUrl("testUrl");
        infraRequests.setClientRequestId("testClientReqId");
        infraRequests.setConfigurationId("testConfigId");
        infraRequests.setConfigurationName("testConfigName");
        infraRequests.setCorrelator("testCorrelator");
        infraRequests.setEndTime(timeStamp);
        infraRequests.setLastModifiedBy("lastModifier");
        infraRequests.setNetworkId("testNetworkId");
        infraRequests.setNetworkName("testNetworkName");
        infraRequests.setNetworkType("testNetworkType");
        infraRequests.setOperationalEnvId("testEnvId");
        infraRequests.setOperationalEnvName("testEnvName");
        infraRequests.setProgress(1L);
        infraRequests.setProvStatus("testProvStatus");
        infraRequests.setRequestAction("testReqAction");
        infraRequests.setRequestBody("testReqBody");
        infraRequests.setRequestorId("testRequestorId");
        infraRequests.setRequestScope("testReqScope");
        infraRequests.setRequestStatus("testReqStatus");
        infraRequests.setRequestType("testReqType");
        infraRequests.setResponseBody("testRespBody");
        infraRequests.setServiceInstanceId("testServiceInstanceId");
        infraRequests.setServiceInstanceName("testServiceInstanceName");
        infraRequests.setServiceType("testServiceType");
        infraRequests.setSource("testSource");
        infraRequests.setStartTime(timeStamp);
        infraRequests.setStatusMessage("testStatusMsg");
        infraRequests.setTenantId("testTenantId");
        infraRequests.setVolumeGroupName("testVolGrpName");
        infraRequests.setVolumeGroupId("testVolGrpId");
        infraRequests.setVnfType("testVnfType");
        infraRequests.setVnfParams("testVnfParams");
        infraRequests.setVnfOutputs("testVnfOutputs");
        infraRequests.setVnfName("testVnfName");
        infraRequests.setVnfId("testVnfId");
        infraRequests.setVfModuleName("testVfModuleName");
        infraRequests.setVfModuleModelName("testVnfModuleModelName");
        infraRequests.setVfModuleId("testVnfModuleId");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = dateFormat.parse("23/09/2007");
        long time = date.getTime();
        timeStamp= new Timestamp(time);
    }
    

    @Test
    public void testEquals()
    {
        InfraRequests requests1= new InfraRequests() {
        };
        InfraRequests requests2= new InfraRequests() {
        };
        requests1.setRequestId("reqId");
        requests2.setRequestId("reqId");
        assertEquals(true, requests1.equals(requests2));
        requests1.setRequestId("reqId1");
        requests2.setRequestId("reqId2");
        assertEquals(false, requests1.equals(requests2));
    }
    
    @Test
    public void testToString()
    {
        
        assertTrue(infraRequests.toString() instanceof String);
    }
    
    
}
