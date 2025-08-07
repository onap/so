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
package org.onap.so.bpmn.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class ServiceInstanceTest {

    private ServiceInstance si = new ServiceInstance();
    Map serviceParams;
    Configuration config = new Configuration();
    ModelInfo model = new ModelInfo();

    @Test
    public void testServiceInstance() {
        si.setServiceType("serviceType");
        si.setServiceId("serviceId");
        si.setServiceParams(serviceParams);
        si.setInstanceId("instanceId");
        si.setInstanceName("instanceName");
        si.setOrchestrationStatus("orchestrationStatus");
        si.setConfiguration(config);
        si.setModelInfo(model);
        si.setEnvironmentContext("environmentContext");
        si.setWorkloadContext("workloadContext");
        assertEquals(si.getServiceType(), "serviceType");
        assertEquals(si.getServiceId(), "serviceId");
        assertEquals(si.getServiceParams(), serviceParams);
        assertEquals(si.getInstanceId(), "instanceId");
        assertEquals(si.getInstanceName(), "instanceName");
        assertEquals(si.getOrchestrationStatus(), "orchestrationStatus");
        assertEquals(si.getConfiguration(), config);
        assertEquals(si.getModelInfo(), model);
        assertEquals(si.getEnvironmentContext(), "environmentContext");
        assertEquals(si.getWorkloadContext(), "workloadContext");


    }

    @Test
    public void serviceInstanceMapperTest() throws IOException {
        String jsonStr = "{\"workloadContext\": \"code123\", \"resourceOrder\": \"sample\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        ServiceInstance serviceInstance = objectMapper.readValue(jsonStr, ServiceInstance.class);
        assertTrue(serviceInstance != null);
    }

}
