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

package org.onap.so.client.appc;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.Status;
import org.onap.so.bpmn.appc.payload.PayloadClient;
import org.onap.so.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;


public class ApplicationControllerActionTest extends BaseTest {

    private ApplicationControllerAction appCAction;

    @Mock
    private ApplicationControllerOrchestrator client;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        appCAction = new ApplicationControllerAction();
        appCAction.client = client;
    }

    @Test
    public void runAppCCommand_ResumeTraffic_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        Action action = Action.ResumeTraffic;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        Optional<String> otherPayload = PayloadClient.resumeTrafficFormat(payloadInfo.get("vnfName"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);
    }

    @Test
    public void runAppCCommand_Start_Test() throws ApplicationControllerOrchestratorException, JsonProcessingException {
        runAppCCommand_StartStop_Test(Action.Start);
    }

    @Test
    public void runAppCCommand_Stop_Test() throws ApplicationControllerOrchestratorException, JsonProcessingException {
        runAppCCommand_StartStop_Test(Action.Stop);
    }

    private void runAppCCommand_StartStop_Test(Action action)
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        Optional<String> otherPayload = PayloadClient.startStopFormat(payloadInfo.get("vnfName"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);
    }

    @Test
    public void runAppCCommand_Unlock_Test() throws ApplicationControllerOrchestratorException {
        runAppCCommand_LockUnlock_Test(Action.Unlock);
    }

    @Test
    public void runAppCCommand_Lock_Test() throws ApplicationControllerOrchestratorException {
        runAppCCommand_LockUnlock_Test(Action.Lock);
    }

    private void runAppCCommand_LockUnlock_Test(Action action) throws ApplicationControllerOrchestratorException {
        // Prepare method
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        Optional<String> otherPayload = Optional.empty();
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);
    }

    @Test
    public void runAppCCommand_QuiesceTraffic_PayloadPresent_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        Action action = Action.QuiesceTraffic;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.of("testPayload");
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        Optional<String> modifiedPayload = PayloadClient.quiesceTrafficFormat(payload, payloadInfo.get("vnfName"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, modifiedPayload,
                controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverId, modifiedPayload, controllerType);
    }

    @Test
    public void runAppCCommand_QuiesceTraffic_NoPayload_Test() throws ApplicationControllerOrchestratorException {
        // Prepare method
        Action action = Action.QuiesceTraffic;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        String controllerType = "testControllerType";

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify non call
        verify(client, times(0)).vnfCommand(any(), any(), any(), any(), any(), any());
        assertEquals("Payload is not present for " + action.toString(), appCAction.getErrorMessage());
    }

    @Test
    public void runAppCCommand_HealthCheck_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        Action action = Action.HealthCheck;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        payloadInfo.put("vnfHostIpAddress", "testVnfHostIpAddress");
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        Optional<String> otherPayload =
                PayloadClient.healthCheckFormat(payloadInfo.get("vnfName"), payloadInfo.get("vnfHostIpAddress"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);
    }

    @Test
    public void runAppCCommand_Snapshot_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        Action action = Action.Snapshot;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("identityUrl", "testIdentityUrl");
        ArrayList<String> vmIdList = new ArrayList<String>();
        String vmId1 = "testlink:testVmId1";
        vmIdList.add(vmId1);
        String vmId2 = "testlink:testVmId2";
        vmIdList.add(vmId2);
        JSONObject vmIdListJson = new JSONObject();
        vmIdListJson.put("vmIds", vmIdList);
        payloadInfo.put("vmIdList", vmIdListJson.toString());
        ArrayList<String> vserverIdList = new ArrayList<String>();
        String vserverId1 = "testVserverId1";
        Optional<String> vserverIdString1 = Optional.of(vserverId1);
        vserverIdList.add(vserverId1);
        String vserverId2 = "testVserverId2";
        Optional<String> vserverIdString2 = Optional.of(vserverId2);
        vserverIdList.add(vserverId2);

        JSONObject vserverIdListJson = new JSONObject();
        vserverIdListJson.put("vserverIds", vserverIdList);
        payloadInfo.put("vserverIdList", vserverIdListJson.toString());
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        Optional<String> otherPayloadVm1 = PayloadClient.snapshotFormat(vmId1, payloadInfo.get("identityUrl"));
        Optional<String> otherPayloadVm2 = PayloadClient.snapshotFormat(vmId2, payloadInfo.get("identityUrl"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverIdString1, otherPayloadVm1,
                controllerType);
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverIdString2, otherPayloadVm2,
                controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverIdString1, otherPayloadVm1,
                controllerType);
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverIdString2, otherPayloadVm2,
                controllerType);
    }

    @Test
    public void runAppCCommand_ConfigModify__PayloadPresent_Test() throws ApplicationControllerOrchestratorException {
        // Prepare method
        Action action = Action.ConfigModify;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.of("testPayload");
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, payload, controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverId, payload, controllerType);
    }

    @Test
    public void runAppCCommand_ConfigModify__NoPayload_Test() throws ApplicationControllerOrchestratorException {
        // Prepare method
        Action action = Action.ConfigModify;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        String controllerType = "testControllerType";

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify non call
        verify(client, times(0)).vnfCommand(any(), any(), any(), any(), any(), any());
        assertEquals("Payload is not present for " + action.toString(), appCAction.getErrorMessage());
    }

    @Test
    public void runAppCCommand_UpgradePreCheck_PayloadPresent_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        runAppCCommand_Upgrade_PayloadPresent_Test(Action.UpgradePreCheck);
    }

    @Test
    public void runAppCCommand_UpgradePostCheck_PayloadPresent_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        runAppCCommand_Upgrade_PayloadPresent_Test(Action.UpgradePostCheck);
    }

    @Test
    public void runAppCCommand_UpgradeSoftware_PayloadPresent_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        runAppCCommand_Upgrade_PayloadPresent_Test(Action.UpgradeSoftware);
    }

    @Test
    public void runAppCCommand_UpgradeBackup_PayloadPresent_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        runAppCCommand_Upgrade_PayloadPresent_Test(Action.UpgradeBackup);
    }

    private void runAppCCommand_Upgrade_PayloadPresent_Test(Action action)
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.of("testPayload");
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        Optional<String> modifiedPayload = PayloadClient.upgradeFormat(payload, payloadInfo.get("vnfName"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, modifiedPayload,
                controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        verify(client, times(1)).vnfCommand(action, msoRequestId, vnfId, vserverId, modifiedPayload, controllerType);
    }

    @Test
    public void runAppCCommand_UpgradePreCheck_NoPayload_Test() throws ApplicationControllerOrchestratorException {
        runAppCCommand_Upgrade_NoPayload_Test(Action.UpgradePreCheck);
    }

    @Test
    public void runAppCCommand_UpgradePostCheck_NoPayload_Test() throws ApplicationControllerOrchestratorException {
        runAppCCommand_Upgrade_NoPayload_Test(Action.UpgradePostCheck);
    }

    @Test
    public void runAppCCommand_UpgradeSoftware_NoPayload_Test() throws ApplicationControllerOrchestratorException {
        runAppCCommand_Upgrade_NoPayload_Test(Action.UpgradeSoftware);
    }

    @Test
    public void runAppCCommand_UpgradeBackup_NoPayload_Test() throws ApplicationControllerOrchestratorException {
        runAppCCommand_Upgrade_NoPayload_Test(Action.UpgradeBackup);
    }

    private void runAppCCommand_Upgrade_NoPayload_Test(Action action)
            throws ApplicationControllerOrchestratorException {
        // Prepare method
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        String controllerType = "testControllerType";

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify non call
        verify(client, times(0)).vnfCommand(any(), any(), any(), any(), any(), any());
        assertEquals("Payload is not present for " + action.toString(), appCAction.getErrorMessage());
    }

    @Test
    public void runAppCCommand_InvalidAppCAction_Test() throws ApplicationControllerOrchestratorException {
        // Prepare method
        Action action = Action.ActionStatus;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        String controllerType = "testControllerType";

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify non call
        verify(client, times(0)).vnfCommand(any(), any(), any(), any(), any(), any());
        // TODO For original author/architect: it appears that whoever coded this wanted the error message to be "Unable
        // to idenify Action request for AppCClient" and this is not the case because of the
        // ApplicationControllerSupport.getCategoryOf(appCStatus) call with a null appCStatus, so this may be something
        // worth looking into
    }

    @Test
    public void runAppCCommand_NormalAppCStatusGetErrorCode_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        Action action = Action.Start;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        String controllerType = "testControllerType";

        // Prepare mocks
        Status status = new Status();
        status.setCode(100);
        Optional<String> otherPayload = PayloadClient.startStopFormat(payloadInfo.get("vnfName"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, vserverId, otherPayload, controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify call
        String expectedErrorCode = "0";
        assertEquals(expectedErrorCode, appCAction.getErrorCode());
    }

    @Test
    public void getErrorCode_Test() {
        String defaultErrorCode = "1002";
        // Verify default error code
        assertEquals(defaultErrorCode, appCAction.getErrorCode());
    }

    @Test
    public void getErrorMessage_Test() {
        String defaultErrorMessage = "Unable to reach App C Servers";
        // Verify default error message
        assertEquals(defaultErrorMessage, appCAction.getErrorMessage());
    }

    @Test
    public void applicationControllerOrchestratorExceptionCaught_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        // Prepare method
        Action action = Action.Start;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        Optional<String> vserverId = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName");
        String controllerType = "testControllerType";

        // Prepare mocks
        Optional<String> otherPayload = PayloadClient.startStopFormat(payloadInfo.get("vnfName"));
        String expectedErrorMessage = "Test appc orchestrator error message";
        doThrow(new ApplicationControllerOrchestratorException(expectedErrorMessage, 0)).when(client).vnfCommand(action,
                msoRequestId, vnfId, vserverId, otherPayload, controllerType);

        // Run method
        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        // Verify error
        assertEquals(expectedErrorMessage, appCAction.getErrorMessage());
        String expectedErrorCode = "1002";
        assertEquals(expectedErrorCode, appCAction.getErrorCode());
    }

    @Test
    public void runAppCCommand_Snapshot_vmIdList_Empty_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        Action action = Action.Snapshot;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("identityUrl", "testIdentityUrl");
        String controllerType = "testControllerType";

        Status status = new Status();
        Optional<String> otherPayloadVm = PayloadClient.snapshotFormat("", "identityUrl");
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);

        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        verify(client, times(0)).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);
    }

    @Test
    public void runAppCCommand_Snapshot_vmId_null_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        Action action = Action.Snapshot;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("identityUrl", "testIdentityUrl");

        JSONObject vmIdListJson = new JSONObject();
        payloadInfo.put("vmIdList", vmIdListJson.toString());
        String controllerType = "testControllerType";

        Status status = new Status();
        Optional<String> otherPayloadVm = PayloadClient.snapshotFormat("", payloadInfo.get("identityUrl"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);

        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        verify(client, times(0)).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);
    }

    @Test
    public void runAppCCommand_Snapshot_vserverIdList_Empty_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        Action action = Action.Snapshot;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("identityUrl", "testIdentityUrl");
        ArrayList<String> vmIdList = new ArrayList<String>();
        String vmId = "testlink:testVmId";
        vmIdList.add(vmId);
        JSONObject vmIdListJson = new JSONObject();
        vmIdListJson.put("vmIds", vmIdList);
        payloadInfo.put("vmIdList", vmIdListJson.toString());
        String controllerType = "testControllerType";

        Status status = new Status();
        Optional<String> otherPayloadVm = PayloadClient.snapshotFormat(vmId, payloadInfo.get("identityUrl"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);

        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        verify(client, times(0)).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);
    }

    @Test
    public void runAppCCommand_Snapshot_vserverId_null_Test()
            throws ApplicationControllerOrchestratorException, JsonProcessingException {
        Action action = Action.Snapshot;
        String msoRequestId = "testMsoRequestId";
        String vnfId = "testVnfId";
        Optional<String> payload = Optional.empty();
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("identityUrl", "testIdentityUrl");
        ArrayList<String> vmIdList = new ArrayList<String>();
        String vmId = "testlink:testVmId1";
        vmIdList.add(vmId);
        JSONObject vmIdListJson = new JSONObject();
        vmIdListJson.put("vmIds", vmIdList);
        payloadInfo.put("vmIdList", vmIdListJson.toString());
        JSONObject vserverIdListJson = new JSONObject();
        payloadInfo.put("vserverIdList", vserverIdListJson.toString());
        String controllerType = "testControllerType";

        Status status = new Status();
        Optional<String> otherPayloadVm = PayloadClient.snapshotFormat(vmId, payloadInfo.get("identityUrl"));
        doReturn(status).when(client).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);

        appCAction.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);

        verify(client, times(0)).vnfCommand(action, msoRequestId, vnfId, null, otherPayloadVm, controllerType);
    }
}
