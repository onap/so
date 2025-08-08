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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;

public class ConfigurationScaleOutTest extends BaseTaskTest {

    @InjectMocks
    private ConfigurationScaleOut configurationScaleOut = new ConfigurationScaleOut();

    private GenericVnf genericVnf;
    private VfModule vfModule;
    private RequestContext requestContext;
    private String msoRequestId;
    private List<Map<String, String>> configurationParameters = new ArrayList<>();
    private Map<String, String> configParamsMap = new HashMap<>();



    @Before
    public void before() throws BBObjectNotFoundException {
        genericVnf = setGenericVnf();
        vfModule = setVfModule();
        msoRequestId = UUID.randomUUID().toString();
        requestContext = setRequestContext();
        requestContext.setMsoRequestId(msoRequestId);
        configParamsMap.put("availability-zone",
                "$.vnf-topology.vnf-resource-assignments.availability-zones.availability-zone[0]");
        configParamsMap.put("vnf-id", "$.vnf-topology.vnf-topology-identifier-structure.vnf-id");
        configurationParameters.add(configParamsMap);
        requestContext.setConfigurationParameters(configurationParameters);
        gBBInput.setRequestContext(requestContext);

        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

    }

    @Test
    public void setParamsForConfigurationScaleOutTest() throws Exception {
        ControllerSelectionReference controllerSelectionReference = new ControllerSelectionReference();
        controllerSelectionReference.setControllerName("testName");
        controllerSelectionReference.setActionCategory("testAction");
        controllerSelectionReference.setVnfType("testVnfType");
        String sdncResponse =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/SDNCClientGetResponse.json")));
        String expectedPayload = "{\"request-parameters\":{\"vnf-host-ip-address\":\"10.222.22.2\","
                + "\"vf-module-id\":\"testVfModuleId1\"},\"configuration-parameters\""
                + ":{\"vnf-id\":\"66dac89b-2a5b-4cb9-b22e-a7e4488fb3db\",\"availability-zone\":\"AZ-MN02\"}}";
        execution.setVariable("SDNCQueryResponse_" + vfModule.getVfModuleId(), sdncResponse);

        doReturn(controllerSelectionReference).when(catalogDbClient)
                .getControllerSelectionReferenceByVnfTypeAndActionCategory(genericVnf.getVnfType(),
                        Action.ConfigScaleOut.toString());

        configurationScaleOut.setParamsForConfigurationScaleOut(execution);

        assertEquals(genericVnf.getVnfId(), execution.getVariable("vnfId"));
        assertEquals(genericVnf.getVnfName(), execution.getVariable("vnfName"));
        assertEquals("ConfigScaleOut", execution.getVariable("action"));
        assertEquals(requestContext.getMsoRequestId(), execution.getVariable("msoRequestId"));
        assertEquals(controllerSelectionReference.getControllerName(), execution.getVariable("controllerType"));
        assertEquals(vfModule.getVfModuleId(), execution.getVariable("vfModuleId"));
        assertEquals(expectedPayload, execution.getVariable("payload"));
    }

    @Test
    public void callAppcClientTest() {
        Action action = Action.ConfigScaleOut;
        String vnfId = genericVnf.getVnfId();
        String controllerType = "testType";
        String payload = "{\"request-parameters\":{\"vnf-host-ip-address\":\"10.222.22.2\","
                + "\"vf-module-id\":\"testVfModuleId1\"},\"configuration-parameters\""
                + ":{\"vnf-id\":\"66dac89b-2a5b-4cb9-b22e-a7e4488fb3db\",\"availability-zone\":\"AZ-MN02\"}}";
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        payloadInfo.put("vfModuleId", "testVfModuleId");

        execution.setVariable("action", Action.ConfigScaleOut.toString());
        execution.setVariable("msoRequestId", msoRequestId);
        execution.setVariable("controllerType", controllerType);
        execution.setVariable("vnfId", "testVnfId1");
        execution.setVariable("vnfName", "testVnfName");
        execution.setVariable("vfModuleId", "testVfModuleId");
        execution.setVariable("payload", payload);

        doNothing().when(appCClient).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo,
                controllerType);

        configurationScaleOut.callAppcClient(execution);
        verify(appCClient, times(1)).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo,
                controllerType);
    }

    @Test
    public void setParamsForConfigurationScaleOutBadPathTest() throws Exception {
        ControllerSelectionReference controllerSelectionReference = new ControllerSelectionReference();
        controllerSelectionReference.setControllerName("testName");
        controllerSelectionReference.setActionCategory("testAction");
        controllerSelectionReference.setVnfType("testVnfType");
        String sdncResponse = new String(
                Files.readAllBytes(Paths.get("src/test/resources/__files/SDNCClientResponseIncorrectPath.json")));
        String expectedPayload = "{\"request-parameters\":{\"vnf-host-ip-address\":\"10.222.22.2\","
                + "\"vf-module-id\":\"testVfModuleId1\"},\"configuration-parameters\""
                + ":{\"vnf-id\":\"66dac89b-2a5b-4cb9-b22e-a7e4488fb3db\",\"availability-zone\":null}}";
        execution.setVariable("SDNCQueryResponse_" + vfModule.getVfModuleId(), sdncResponse);

        doReturn(controllerSelectionReference).when(catalogDbClient)
                .getControllerSelectionReferenceByVnfTypeAndActionCategory(genericVnf.getVnfType(),
                        Action.ConfigScaleOut.toString());

        configurationScaleOut.setParamsForConfigurationScaleOut(execution);

        assertEquals(genericVnf.getVnfId(), execution.getVariable("vnfId"));
        assertEquals(genericVnf.getVnfName(), execution.getVariable("vnfName"));
        assertEquals("ConfigScaleOut", execution.getVariable("action"));
        assertEquals(requestContext.getMsoRequestId(), execution.getVariable("msoRequestId"));
        assertEquals(controllerSelectionReference.getControllerName(), execution.getVariable("controllerType"));
        assertEquals(vfModule.getVfModuleId(), execution.getVariable("vfModuleId"));
        assertEquals(expectedPayload, execution.getVariable("payload"));
    }

    @Test
    public void callAppcClientExceptionTest() {
        expectedException.expect(BpmnError.class);
        Action action = Action.ConfigScaleOut;
        String vnfId = genericVnf.getVnfId();
        String controllerType = "testType";
        String payload = "{\"request-parameters\":{\"vnf-host-ip-address\":\"10.222.22.2\","
                + "\"vf-module-id\":\"testVfModuleId1\"},\"configuration-parameters\""
                + ":{\"vnf-id\":\"66dac89b-2a5b-4cb9-b22e-a7e4488fb3db\",\"availability-zone\":\"AZ-MN02\"}}";
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        payloadInfo.put("vfModuleId", "testVfModuleId");

        execution.setVariable("action", Action.ConfigScaleOut.toString());
        execution.setVariable("msoRequestId", msoRequestId);
        execution.setVariable("controllerType", controllerType);
        execution.setVariable("vnfId", "testVnfId1");
        execution.setVariable("vnfName", "testVnfName");
        execution.setVariable("vfModuleId", "testVfModuleId");
        execution.setVariable("payload", payload);


        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1002), eq("APPC Client Failed"));
        doThrow(new RuntimeException("APPC Client Failed")).when(appCClient).runAppCCommand(action, msoRequestId, vnfId,
                Optional.of(payload), payloadInfo, controllerType);
        configurationScaleOut.callAppcClient(execution);
        verify(appCClient, times(1)).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo,
                controllerType);
    }
}
