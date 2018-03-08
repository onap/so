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

package org.openecomp.mso.bpmn.infrastructure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.mock.FileUtil;

@Ignore
public class DoCreateVfModuleVolumeV2Test extends WorkflowTest {

    public static final String _prefix = "CVFMODVOL2_";

    private final CallbackSet callbacks = new CallbackSet();

    public DoCreateVfModuleVolumeV2Test() throws IOException {
        callbacks.put("volumeGroupCreate", FileUtil.readResourceFile(
                "__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeCallbackResponse.xml"));
        callbacks.put("volumeGroupRollback", FileUtil.readResourceFile(
                "__files/DoCreateVfModuleVolumeV1/RollbackVfModuleVolumeCallbackResponse.xml"));
    }

    /**
     * Happy Path
     *
     * @throws Exception
     */
    @Test
    //@Ignore
    @Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV2.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/vnfAdapterRestV1.bpmn",
            "subprocess/DoCreateVfModuleVolumeRollback.bpmn"})
    public void TestHappyPath() throws Exception {

        logStart();

//		DoCreateVfModuleVolume_Success();

        String businessKey = UUID.randomUUID().toString();
        String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");

        Map<String, Object> testVariables = new HashMap<>();
        testVariables.put("volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
        testVariables.put("vnfId", "TEST-VNF-ID-0123");
        testVariables.put("lcpCloudRegionId", "AAIAIC25");
        testVariables.put("test-volume-group-name", "MSOTESTVOL101a-vSAMP12_base_vol_module-01");
        testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        //testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
        TestAsyncResponse asyncResponse = invokeAsyncProcess("DoCreateVfModuleVolumeV2", "v1", businessKey, createVfModuleVolRequest, testVariables);

        injectVNFRestCallbacks(callbacks, "volumeGroupCreate");

        waitForProcessEnd(businessKey, 100000);
        checkVariable(businessKey, "DCVFMODVOLV2_SuccessIndicator", true);

        logEnd();
    }

    @Test
//	@Ignore
    @Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV2.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/vnfAdapterRestV1.bpmn",
            "subprocess/DoCreateVfModuleVolumeRollback.bpmn"})
    public void TestVolumeGroupExistError() throws Exception {

        logStart();

//		DoCreateVfModuleVolume_VolumeGroupExistsFail();

        String businessKey = UUID.randomUUID().toString();
        String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");

        Map<String, Object> testVariables = new HashMap<>();
        testVariables.put("DCVFMODVOLV2_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
        testVariables.put("vnf-id", "TEST-VNF-ID-0123");
        testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
        testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
        TestAsyncResponse asyncResponse = invokeAsyncProcess("DoCreateVfModuleVolumeV2", "v1", businessKey, createVfModuleVolRequest, testVariables);

        //injectVNFRestCallbacks(callbacks, "volumeGroupCreate");

        waitForProcessEnd(businessKey, 100000);
        WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "SavedWorkflowException1");
        Assert.assertTrue(wfe.getErrorCode() == 2500);
        Assert.assertTrue(wfe.getErrorMessage().startsWith("Generic vnf null was not found in AAI. Return code: 404."));
        checkVariable(businessKey, "DCVFMODVOLV2_SuccessIndicator", false);

        logEnd();
    }

    /**
     * Will trigger AAI create rollback
     *
     * @throws Exception
     */
    @Test
//	@Ignore
    @Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV2.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/vnfAdapterRestV1.bpmn",
            "subprocess/DoCreateVfModuleVolumeRollback.bpmn"})
    public void TestVnfVolumeGroupCreateError() throws Exception {

        logStart();

//		DoCreateVfModuleVolume_VnfCreateVolumeGroupFail();

        String businessKey = UUID.randomUUID().toString();
        String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");

        Map<String, Object> testVariables = new HashMap<>();
        testVariables.put("DCVFMODVOLV2_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
        testVariables.put("vnf-id", "TEST-VNF-ID-0123");
        testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
        testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
        TestAsyncResponse asyncResponse = invokeAsyncProcess("DoCreateVfModuleVolumeV2", "v1", businessKey, createVfModuleVolRequest, testVariables);

        //injectVNFRestCallbacks(callbacks, "volumeGroupCreate");

        waitForProcessEnd(businessKey, 100000);
        checkVariable(businessKey, "DCVFMODVOLV2_SuccessIndicator", false);

        logEnd();
    }

    /**
     * Will trigger AAI create rollback
     *
     * @throws Exception
     */
    @Test
//	@Ignore
    @Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV2.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/vnfAdapterRestV1.bpmn",
            "subprocess/DoCreateVfModuleVolumeRollback.bpmn"})
    public void TestUpdateAaiVolumeGroupError() throws Exception {

        logStart();

//		DoCreateVfModuleVolume_AaiVolumeGroupUpdateFail();

        String businessKey = UUID.randomUUID().toString();
        String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");

        Map<String, Object> testVariables = new HashMap<>();
        testVariables.put("DCVFMODVOLV2_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
        testVariables.put("vnf-id", "TEST-VNF-ID-0123");
        testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
        testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
        TestAsyncResponse asyncResponse = invokeAsyncProcess("DoCreateVfModuleVolumeV2", "v1", businessKey, createVfModuleVolRequest, testVariables);

        // VNF callback not needed fort this failure scenario
//		injectVNFRestCallbacks(callbacks, "volumeGroupCreate,volumeGroupRollback");

        waitForProcessEnd(businessKey, 100000);
        WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "SavedWorkflowException1");
        Assert.assertTrue(wfe.getErrorCode() == 2500);
        Assert.assertTrue(wfe.getErrorMessage().startsWith("Generic vnf null was not found in AAI. Return code: 404."));
        checkVariable(businessKey, "DCVFMODVOLV2_SuccessIndicator", false);

        logEnd();
    }

    /**
     * Will trigger not trigger rollback
     *
     * @throws Exception
     */
    @Test
//	@Ignore
    @Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV2.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/vnfAdapterRestV1.bpmn",
            "subprocess/DoCreateVfModuleVolumeRollback.bpmn"})
    public void TestUpdateAaiVolumeGroupErrorNoRollback() throws Exception {

        logStart();

//		DoCreateVfModuleVolume_AaiVolumeGroupUpdateFail();

        String businessKey = UUID.randomUUID().toString();
        String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeNoRollbackRequest.xml");

        Map<String, Object> testVariables = new HashMap<>();
        testVariables.put("DCVFMODVOLV2_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
        testVariables.put("vnf-id", "TEST-VNF-ID-0123");
        testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
        testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
        testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
        TestAsyncResponse asyncResponse = invokeAsyncProcess("DoCreateVfModuleVolumeV2", "v1", businessKey, createVfModuleVolRequest, testVariables);

        // VNF callback not needed fort this failure scenario
//		injectVNFRestCallbacks(callbacks, "volumeGroupCreate");

        waitForProcessEnd(businessKey, 100000);
        WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "SavedWorkflowException1");
        Assert.assertTrue(wfe.getErrorCode() == 2500);
        Assert.assertTrue(wfe.getErrorMessage().startsWith("Generic vnf null was not found in AAI. Return code: 404."));
        checkVariable(businessKey, "DCVFMODVOLV2_SuccessIndicator", false);

        logEnd();
    }
}
