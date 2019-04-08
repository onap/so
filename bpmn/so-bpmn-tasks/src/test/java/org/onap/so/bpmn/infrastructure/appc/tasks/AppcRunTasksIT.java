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
package org.onap.so.bpmn.infrastructure.appc.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.springframework.beans.factory.annotation.Autowired;

public class AppcRunTasksIT extends BaseIntegrationTest {

    @Autowired
    private AppcRunTasks appcRunTasks;

    private GenericVnf genericVnf;
    private RequestContext requestContext;
    private String msoRequestId;

    @Before
    public void before() {
        genericVnf = setGenericVnf();
        msoRequestId = UUID.randomUUID().toString();
        requestContext = setRequestContext();
        requestContext.setMsoRequestId(msoRequestId);
        gBBInput.setRequestContext(requestContext);
    }

    @Test
    public void preProcessActivityTest() throws Exception {
        appcRunTasks.preProcessActivity(execution);
        assertEquals(execution.getVariable("actionQuiesceTraffic"), Action.QuiesceTraffic);
        assertEquals(execution.getVariable("rollbackQuiesceTraffic"), false);
    }

    @Test
    public void runAppcCommandTest() throws Exception {
        Action action = Action.QuiesceTraffic;
        ControllerSelectionReference controllerSelectionReference = new ControllerSelectionReference();
        controllerSelectionReference.setControllerName("testName");
        controllerSelectionReference.setActionCategory(action.toString());
        controllerSelectionReference.setVnfType("testVnfType");

        doReturn(controllerSelectionReference).when(catalogDbClient)
                .getControllerSelectionReferenceByVnfTypeAndActionCategory(genericVnf.getVnfType(),
                        Action.QuiesceTraffic.toString());

        execution.setVariable("aicIdentity", "testAicIdentity");

        String vnfId = genericVnf.getVnfId();
        genericVnf.setIpv4OamAddress("testOamIpAddress");
        String payload = "{\"testName\":\"testValue\",}";
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setPayload(payload);
        gBBInput.getRequestContext().setRequestParameters(requestParameters);

        String controllerType = "testName";
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName1");
        payloadInfo.put("aicIdentity", "testAicIdentity");
        payloadInfo.put("vnfHostIpAddress", "testOamIpAddress");
        payloadInfo.put("vserverIdList", null);
        payloadInfo.put("vfModuleId", null);
        payloadInfo.put("identityUrl", null);
        payloadInfo.put("vmIdList", null);

        doNothing().when(appCClient).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo,
                controllerType);

        appcRunTasks.runAppcCommand(execution, action);
        verify(appCClient, times(1)).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo,
                controllerType);
    }
}
