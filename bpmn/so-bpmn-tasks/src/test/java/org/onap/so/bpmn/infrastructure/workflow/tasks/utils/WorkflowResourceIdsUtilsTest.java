/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.utils;

import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import java.util.function.Supplier;
import static org.junit.Assert.assertEquals;


public class WorkflowResourceIdsUtilsTest {

    private static final String SERVICE_ID = "serviceId";
    private static final String NETWORK_ID = "networkId";
    private static final String VF_MODULE_ID = "vfModuleId";
    private static final String VNF_ID = "vnfId";
    private static final String VOLUME_GROUP_ID = "volumeGroupId";
    private static final String INSTANCE_GROUP_ID = "instanceGroupId";
    private static final String PNF_ID = "pnfId";
    private static final String NETWORK_COLLECTION_ID = "networkCollectionId";
    private static final String CONFIGURATION_ID = "configurationId";
    private static final String VNF_INSTANCE_NAME = "vnfInstanceNameId";
    private static final String VF_MODULE_INSTANCE_NAME = "vfModuleInstanceNameId";
    private static final String PNF_INSTANCE_NAME = "pnfInstanceNameId";
    private WorkflowResourceIds workflowResourceIds;

    @Before
    public void setUp() {
        workflowResourceIds = new WorkflowResourceIds();
    }

    @Test
    public void shouldProperlySetFieldsFromExecution() {
        DelegateExecutionFake execution = new DelegateExecutionFake();
        execution.setVariable("serviceInstanceId", SERVICE_ID);
        execution.setVariable("networkId", NETWORK_ID);
        execution.setVariable("vfModuleId", VF_MODULE_ID);
        execution.setVariable("vnfId", VNF_ID);
        execution.setVariable("volumeGroupId", VOLUME_GROUP_ID);
        execution.setVariable("instanceGroupId", INSTANCE_GROUP_ID);
        execution.setVariable("vnfInstanceName", VNF_INSTANCE_NAME);
        execution.setVariable("vfModuleInstanceName", VF_MODULE_INSTANCE_NAME);
        execution.setVariable("pnfInstanceName", PNF_INSTANCE_NAME);

        workflowResourceIds = WorkflowResourceIdsUtils.getWorkflowResourceIdsFromExecution(execution);

        assertEquals(SERVICE_ID, workflowResourceIds.getServiceInstanceId());
        assertEquals(NETWORK_ID, workflowResourceIds.getNetworkId());
        assertEquals(VF_MODULE_ID, workflowResourceIds.getVfModuleId());
        assertEquals(VNF_ID, workflowResourceIds.getVnfId());
        assertEquals(VOLUME_GROUP_ID, workflowResourceIds.getVolumeGroupId());
        assertEquals(INSTANCE_GROUP_ID, workflowResourceIds.getInstanceGroupId());
        assertEquals(VNF_INSTANCE_NAME, workflowResourceIds.getVnfInstanceName());
        assertEquals(VF_MODULE_INSTANCE_NAME, workflowResourceIds.getVfModuleInstanceName());
        assertEquals(PNF_INSTANCE_NAME, workflowResourceIds.getPnfInstanceName());
    }

    @Test
    public void shouldProperlySetServiceInstanceId() {
        assertFieldSetProperly(WorkflowType.SERVICE, SERVICE_ID, workflowResourceIds::getServiceInstanceId);
        assertEquals(SERVICE_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.SERVICE));
    }

    @Test
    public void shouldProperlySetVnfId() {
        assertFieldSetProperly(WorkflowType.VNF, VNF_ID, workflowResourceIds::getVnfId);
        assertEquals(VNF_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.VNF));
    }

    @Test
    public void shouldProperlySetPnfId() {
        assertFieldSetProperly(WorkflowType.PNF, PNF_ID, workflowResourceIds::getPnfId);
        assertEquals(PNF_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.PNF));
    }

    @Test
    public void shouldProperlySetVfModuleId() {
        assertFieldSetProperly(WorkflowType.VFMODULE, VF_MODULE_ID, workflowResourceIds::getVfModuleId);
        assertEquals(VF_MODULE_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.VFMODULE));
    }

    @Test
    public void shouldProperlySetVolumeGroupId() {
        assertFieldSetProperly(WorkflowType.VOLUMEGROUP, VOLUME_GROUP_ID, workflowResourceIds::getVolumeGroupId);
        assertEquals(VOLUME_GROUP_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.VOLUMEGROUP));
    }

    @Test
    public void shouldProperlySetNetworkId() {
        assertFieldSetProperly(WorkflowType.NETWORK, NETWORK_ID, workflowResourceIds::getNetworkId);
        assertEquals(NETWORK_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.NETWORK));
    }

    @Test
    public void shouldProperlySetNetworkCollectionId() {
        assertFieldSetProperly(WorkflowType.NETWORKCOLLECTION, NETWORK_COLLECTION_ID,
                workflowResourceIds::getNetworkCollectionId);
        assertEquals(NETWORK_COLLECTION_ID, WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds,
                WorkflowType.NETWORKCOLLECTION));
    }

    @Test
    public void shouldProperlySetConfigurationId() {
        assertFieldSetProperly(WorkflowType.CONFIGURATION, CONFIGURATION_ID, workflowResourceIds::getConfigurationId);
        assertEquals(CONFIGURATION_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.CONFIGURATION));
    }

    @Test
    public void shouldProperlySetInstanceGroupId() {
        assertFieldSetProperly(WorkflowType.INSTANCE_GROUP, INSTANCE_GROUP_ID, workflowResourceIds::getInstanceGroupId);
        assertEquals(INSTANCE_GROUP_ID,
                WorkflowResourceIdsUtils.getResourceIdByWorkflowType(workflowResourceIds, WorkflowType.INSTANCE_GROUP));
    }

    @Test
    public void shouldPropertySetVnfInstanceName() {
        assertFieldSetProperlyInstanceName(WorkflowType.VNF, VNF_INSTANCE_NAME,
                workflowResourceIds::getVnfInstanceName);
    }

    @Test
    public void shouldPropertySetVfModuleInstanceName() {
        assertFieldSetProperlyInstanceName(WorkflowType.VFMODULE, VF_MODULE_INSTANCE_NAME,
                workflowResourceIds::getVfModuleInstanceName);
    }

    @Test
    public void shouldPropertySetPnfInstanceName() {
        assertFieldSetProperlyInstanceName(WorkflowType.PNF, PNF_INSTANCE_NAME,
                workflowResourceIds::getPnfInstanceName);
    }

    private void assertFieldSetProperly(WorkflowType workflowType, String expectedId,
            Supplier<String> testedObjectField) {
        WorkflowResourceIdsUtils.setResourceIdByWorkflowType(workflowResourceIds, workflowType, expectedId);
        assertEquals(expectedId, testedObjectField.get());
    }

    private void assertFieldSetProperlyInstanceName(WorkflowType workflowType, String expectedId,
            Supplier<String> testedObjectField) {
        WorkflowResourceIdsUtils.setInstanceNameByWorkflowType(workflowResourceIds, workflowType, expectedId);
        assertEquals(expectedId, testedObjectField.get());
    }
}
