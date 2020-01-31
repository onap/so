package org.onap.so.bpmn.infrastructure.workflow.tasks.utils;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import java.util.function.Supplier;
import static org.junit.Assert.assertEquals;


public class WorkflowResourceIdsExtensionTest {

    private WorkflowResourceIdsExtension workflowResourceIdsExtension;

    @Before
    public void setUp() {
        workflowResourceIdsExtension = new WorkflowResourceIdsExtension();
    }

    @Test
    public void shouldProperlySetServiceInstanceId() {
        assertProperFieldSet(WorkflowType.SERVICE, "serviceId", workflowResourceIdsExtension::getServiceInstanceId);
    }

    @Test
    public void shouldProperlySetVnfId() {
        assertProperFieldSet(WorkflowType.VNF, "vnfId", workflowResourceIdsExtension::getVnfId);

    }

    @Test
    public void shouldProperlySetPnfId() {
        assertProperFieldSet(WorkflowType.PNF, "pnfId", workflowResourceIdsExtension::getPnfId);
    }

    @Test
    public void shouldProperlySetVfModuleId() {
        assertProperFieldSet(WorkflowType.VFMODULE, "vfModuleId", workflowResourceIdsExtension::getVfModuleId);
    }

    @Test
    public void shouldProperlySetVolumeGroupId() {
        assertProperFieldSet(WorkflowType.VOLUMEGROUP, "volumeGroupId", workflowResourceIdsExtension::getVolumeGroupId);
    }

    @Test
    public void shouldProperlySetNetworkId() {
        assertProperFieldSet(WorkflowType.NETWORK, "networkId", workflowResourceIdsExtension::getNetworkId);
    }

    @Test
    public void shouldProperlySetNetworkCollectionId() {
        assertProperFieldSet(WorkflowType.NETWORKCOLLECTION, "networkCollectionId",
                workflowResourceIdsExtension::getNetworkCollectionId);

    }

    @Test
    public void shouldProperlySetConfigurationId() {
        assertProperFieldSet(WorkflowType.CONFIGURATION, "configurationId",
                workflowResourceIdsExtension::getConfigurationId);
    }

    @Test
    public void shouldProperlySetInstanceGroupId() {
        assertProperFieldSet(WorkflowType.INSTANCE_GROUP, "instanceGroupId",
                workflowResourceIdsExtension::getInstanceGroupId);
    }

    private void assertProperFieldSet(WorkflowType workflowType, String expectedId, Supplier<String> getter) {
        workflowResourceIdsExtension.setResourceId(workflowType, expectedId);
        assertEquals(expectedId, getter.get());
    }
}
