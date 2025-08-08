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

package org.onap.so.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.mockito.Mockito.doReturn;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SDNCHandlerTest extends BaseBPMNTest {
    @Test
    public void sunnyDay_SDNCHandler_Sync_Final_Test()
            throws MapperException, BadResponseException, IOException {
        final String sdncResponse =
                new String(Files.readAllBytes(Paths.get("src/test/resources/SDNCClientPut200Response.json")));
        doReturn(sdncResponse).when(sdncClient).post(createSDNCRequest().getSDNCPayload(), SDNCTopology.CONFIGURATION);
        Map<String, Object> startVariables = new HashMap<>();
        startVariables.put("SDNCRequest", createSDNCRequest());
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("SDNCHandler", startVariables);
        assertThat(pi).isNotNull().isStarted()
                .hasPassedInOrder("SDNC_Start", "SNDC_SetupCallback", "Call_SDNC", "isAsync_Gateway", "SDNC_End")
                .isEnded();
    }


    public SDNCRequest createSDNCRequest() {
        SDNCRequest request = new SDNCRequest();
        request.setCorrelationName("correlationName");
        request.setCorrelationValue("correlationValue");
        request.setTopology(SDNCTopology.CONFIGURATION);
        ObjectMapper mapper = new ObjectMapper();
        try {
            GenericResourceApiServiceOperationInformation sdncReq =
                    mapper.readValue(Files.readAllBytes(Paths.get("src/test/resources/SDNC_Client_Request.json")),
                            GenericResourceApiServiceOperationInformation.class);
            request.setSDNCPayload(sdncReq);
        } catch (JsonParseException e) {

        } catch (JsonMappingException e) {

        } catch (IOException e) {

        }

        return request;
    }
}
