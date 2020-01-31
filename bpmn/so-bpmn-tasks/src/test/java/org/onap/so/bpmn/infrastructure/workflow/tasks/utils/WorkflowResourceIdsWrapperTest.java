package org.onap.so.bpmn.infrastructure.workflow.tasks.utils;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import java.util.function.Supplier;
import static org.junit.Assert.assertEquals;


public class WorkflowResourceIdsWrapperTest {

    private WorkflowResourceIdsWrapper workflowResourceIdsWrapper;

    @Before
    public void setUp() {
        workflowResourceIdsWrapper = new WorkflowResourceIdsWrapper();
    }

    @Test
    public void shouldProperlySetServiceInstanceId() {
        assertProperFieldSet(WorkflowType.SERVICE, "serviceId", workflowResourceIdsWrapper::getServiceInstanceId);
    }

    @Test
    public void shouldProperlySetVnfId() {
        assertProperFieldSet(WorkflowType.VNF, "vnfId", workflowResourceIdsWrapper::getVnfId);

    }

    @Test
    public void shouldProperlySetPnfId() {
        assertProperFieldSet(WorkflowType.PNF, "pnfId", workflowResourceIdsWrapper::getPnfId);
    }

    @Test
    public void shouldProperlySetVfModuleId() {
        assertProperFieldSet(WorkflowType.VFMODULE, "vfModuleId", workflowResourceIdsWrapper::getVfModuleId);
    }

    @Test
    public void shouldProperlySetVolumeGroupId() {
        assertProperFieldSet(WorkflowType.VOLUMEGROUP, "volumeGroupId", workflowResourceIdsWrapper::getVolumeGroupId);
    }

    @Test
    public void shouldProperlySetNetworkId() {
        assertProperFieldSet(WorkflowType.NETWORK, "networkId", workflowResourceIdsWrapper::getNetworkId);
    }

    @Test
    public void shouldProperlySetNetworkCollectionId() {
        assertProperFieldSet(WorkflowType.NETWORKCOLLECTION, "networkCollectionId",
                workflowResourceIdsWrapper::getNetworkCollectionId);

    }

    @Test
    public void shouldProperlySetConfigurationId() {
        assertProperFieldSet(WorkflowType.CONFIGURATION, "configurationId",
                workflowResourceIdsWrapper::getConfigurationId);
    }

    @Test
    public void shouldProperlySetInstanceGroupId() {
        assertProperFieldSet(WorkflowType.INSTANCE_GROUP, "instanceGroupId",
                workflowResourceIdsWrapper::getInstanceGroupId);
    }

    private void assertProperFieldSet(WorkflowType workflowType, String expectedId, Supplier<String> getter) {
        workflowResourceIdsWrapper.setResourceId(workflowType, expectedId);
        assertEquals(expectedId, getter.get());
    }
}
