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

package org.onap.so.bpmn.infrastructure.adapter.vnf.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.FileUtil;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class VnfAdapterImplTest extends BaseTaskTest {

    @InjectMocks
    private VnfAdapterImpl vnfAdapterImpl = new VnfAdapterImpl();

    private RequestContext requestContext;
    private ServiceInstance serviceInstance;
    private GenericVnf genericVnf;
    private VfModule vfModule;
    private VolumeGroup volumeGroup;

    private static final String VNF_ADAPTER_REST_DELETE_RESPONSE =
            FileUtil.readResourceFile("__files/VfModularity/VNFAdapterRestDeleteResponse.xml");
    private static final String VNF_ADAPTER_REST_CREATE_RESPONSE =
            FileUtil.readResourceFile("__files/VfModularity/VNFAdapterRestCreateCallback.xml");
    private static final String VNF_ADAPTER_VOLUME_CREATE_RESPONSE =
            FileUtil.readResourceFile("__files/VfModularity/CreateVfModuleVolumeCallbackResponse.xml");
    private static final String VNF_ADAPTER_VOLUME_DELETE_RESPONSE =
            FileUtil.readResourceFile("__files/VfModularity/DeleteVfModuleVolumeCallbackResponse.xml");
    private static final String TEST_VFMODULE_HEATSTACK_ID = "slowburn";
    private static final String TEST_VOLUME_HEATSTACK_ID = "testHeatStackId1";
    private static final String TEST_CONTRAIL_SERVICE_INSTANCE_FQDN = "default-domain:MSOTest:MsoNW-RA";
    private static final String TEST_OAM_MANAGEMENT_V4_ADDRESS = "127.0.0.1";
    private static final String TEST_OAM_MANAGEMENT_V6_ADDRESS = "2000:abc:bce:1111";
    private static final String TEST_CONTRAIL_NETWORK_POLICY_FQDNS =
            "MSOTest:DefaultPolicyFQDN2,MSOTest:DefaultPolicyFQDN1";

    @Before
    public void before() throws BBObjectNotFoundException {
        requestContext = setRequestContext();
        serviceInstance = setServiceInstance();
        genericVnf = setGenericVnf();
        vfModule = setVfModule();
        volumeGroup = setVolumeGroup();
        vfModule.setHeatStackId(null);
        volumeGroup.setHeatStackId(null);
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VOLUME_GROUP_ID)))
                .thenReturn(volumeGroup);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);
    }

    @Test
    public void preProcessVnfAdapterTest() {
        vnfAdapterImpl.preProcessVnfAdapter(execution);

        assertEquals(requestContext.getMsoRequestId(), execution.getVariable("mso-request-id"));
        assertEquals(serviceInstance.getServiceInstanceId(), execution.getVariable("mso-service-instance-id"));
    }

    @Test
    public void postProcessVnfAdapter_CreateResponseTest() {
        execution.setVariable("WorkflowResponse", VNF_ADAPTER_REST_CREATE_RESPONSE);
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertEquals(TEST_VFMODULE_HEATSTACK_ID, vfModule.getHeatStackId());
        assertEquals(TEST_CONTRAIL_SERVICE_INSTANCE_FQDN, vfModule.getContrailServiceInstanceFqdn());
        assertEquals(TEST_CONTRAIL_SERVICE_INSTANCE_FQDN, execution.getVariable("contrailServiceInstanceFqdn"));
        assertEquals(TEST_OAM_MANAGEMENT_V4_ADDRESS, genericVnf.getIpv4OamAddress());
        assertEquals(TEST_OAM_MANAGEMENT_V4_ADDRESS, execution.getVariable("oamManagementV4Address"));
        assertEquals(TEST_OAM_MANAGEMENT_V6_ADDRESS, genericVnf.getManagementV6Address());
        assertEquals(TEST_OAM_MANAGEMENT_V6_ADDRESS, execution.getVariable("oamManagementV6Address"));
        assertEquals(TEST_CONTRAIL_NETWORK_POLICY_FQDNS, execution.getVariable("contrailNetworkPolicyFqdnList"));
    }


    @Test
    public void postProcessVnfAdapter_CreateResponseTest_EmptyCreateVfModuleResponseTag() {
        expectedException.expect(BpmnError.class);
        execution.setVariable("WorkflowResponse", "<vfModuleStackId></vfModuleStackId>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
    }

    @Test
    public void postProcessVnfAdapter_CreateResponseTest_EmptyVfModuleStackIdTag() {
        execution.setVariable("WorkflowResponse", "<createVfModuleResponse></createVfModuleResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_CreateResponseTest_EmptyHeatStackId() {
        execution.setVariable("WorkflowResponse",
                "<createVfModuleResponse><vfModuleStackId></vfModuleStackId></createVfModuleResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_CreateResponseTest_EmptyVfModuleOutputs() {
        execution.setVariable("WorkflowResponse",
                "<createVfModuleResponse><vfModuleOutputs></vfModuleOutputs></createVfModuleResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
        assertNull(vfModule.getContrailServiceInstanceFqdn());
        assertNull(execution.getVariable("contrailServiceInstanceFqdn"));
        assertNotEquals(TEST_OAM_MANAGEMENT_V4_ADDRESS, genericVnf.getIpv4OamAddress());
        assertNull(execution.getVariable("oamManagementV4Address"));
        assertNull(genericVnf.getManagementV6Address());
        assertNull(execution.getVariable("oamManagementV6Address"));
        assertNull(execution.getVariable("contrailNetworkPolicyFqdnList"));
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest() {
        vfModule.setHeatStackId(TEST_VFMODULE_HEATSTACK_ID);
        execution.setVariable("WorkflowResponse", VNF_ADAPTER_REST_DELETE_RESPONSE);
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
        assertEquals(vfModule.getContrailServiceInstanceFqdn(), "");
        assertEquals(execution.getVariable("contrailServiceInstanceFqdn"), "");
        assertEquals(genericVnf.getIpv4OamAddress(), "");
        assertEquals(execution.getVariable("oamManagementV4Address"), "");
        assertEquals(genericVnf.getManagementV6Address(), "");
        assertEquals(execution.getVariable("oamManagementV6Address"), "");
        assertEquals(TEST_CONTRAIL_NETWORK_POLICY_FQDNS, execution.getVariable("contrailNetworkPolicyFqdnList"));
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_EmptyVfModuleOutputs() {
        execution.setVariable("WorkflowResponse",
                "<createVfModuleResponse><vfModuleOutputs></vfModuleOutputs></createVfModuleResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
        assertNull(vfModule.getContrailServiceInstanceFqdn());
        assertNull(execution.getVariable("contrailServiceInstanceFqdn"));
        assertNull(execution.getVariable("oamManagementV4Address"));
        assertNull(execution.getVariable("oamManagementV6Address"));
        assertNull(execution.getVariable("contrailNetworkPolicyFqdnList"));
    }

    @Test
    public void postProcessVnfAdapter_ResponseNullTest() {
        execution.setVariable("WorkflowResponse", null);
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_ResponseEmptyTest() {
        execution.setVariable("WorkflowResponse", "");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_VfModuleDeletedFalse() {
        vfModule.setHeatStackId(TEST_VFMODULE_HEATSTACK_ID);
        execution.setVariable("WorkflowResponse",
                "<deleteVfModuleResponse><vfModuleDeleted>false</vfModuleDeleted></deleteVfModuleResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertEquals(TEST_VFMODULE_HEATSTACK_ID, vfModule.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_EmptyDeleteVfModuleResponseTag() {
        expectedException.expect(BpmnError.class);
        execution.setVariable("WorkflowResponse", "<vfModuleDeleted></vfModuleDeleted>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_EmptyVfModuleDeletedTag() {
        vfModule.setHeatStackId(TEST_VFMODULE_HEATSTACK_ID);
        execution.setVariable("WorkflowResponse", "<deleteVfModuleResponse></deleteVfModuleResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertEquals(TEST_VFMODULE_HEATSTACK_ID, vfModule.getHeatStackId());
    }

    @Test
    public void preProcessVnfAdapterExceptionTest() throws BBObjectNotFoundException {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(extractPojosForBB).extractByKey(any(),
                ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID));

        vnfAdapterImpl.preProcessVnfAdapter(execution);
    }

    @Test
    public void postProcessVnfAdapter_CreateVolumeResponseTest() {
        execution.setVariable("WorkflowResponse", VNF_ADAPTER_VOLUME_CREATE_RESPONSE);
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertEquals(TEST_VOLUME_HEATSTACK_ID, volumeGroup.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_CreateVolumeEmptyResponseTest() {
        expectedException.expect(BpmnError.class);
        execution.setVariable("WorkflowResponse", "<createVolumeGroupResponse></createVolumeGroupResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(volumeGroup.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_DeleteVolumeGroup() {
        volumeGroup.setHeatStackId(TEST_VOLUME_HEATSTACK_ID);
        execution.setVariable("WorkflowResponse", VNF_ADAPTER_VOLUME_DELETE_RESPONSE);
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(volumeGroup.getHeatStackId());
    }


    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_VolumeGroupDeletedFalse() {
        volumeGroup.setHeatStackId(TEST_VOLUME_HEATSTACK_ID);
        execution.setVariable("WorkflowResponse",
                "<deleteVolumeGroupResponse><volumeGroupDeleted>false</volumeGroupDeleted></deleteVolumeGroupResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertEquals(TEST_VOLUME_HEATSTACK_ID, volumeGroup.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_EmptyDeleteVolumeGroupResponseTag() {
        expectedException.expect(BpmnError.class);
        execution.setVariable("WorkflowResponse", "<volumeGroupDeleted></volumeGroupDeleted>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
    }

    @Test
    public void postProcessVnfAdapter_DeleteResponseTest_EmptyVolumeGroupDeletedTag() {
        volumeGroup.setHeatStackId(TEST_VOLUME_HEATSTACK_ID);
        execution.setVariable("WorkflowResponse", "<deleteVolumeGroupResponse></deleteVolumeGroupResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertEquals(TEST_VOLUME_HEATSTACK_ID, volumeGroup.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapterExceptionTest() throws BBObjectNotFoundException {
        doThrow(RuntimeException.class).when(extractPojosForBB).extractByKey(any(),
                ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID));

        execution.setVariable("WorkflowResponse", VNF_ADAPTER_REST_CREATE_RESPONSE);
        expectedException.expect(BpmnError.class);

        vnfAdapterImpl.postProcessVnfAdapter(execution);
    }
}
