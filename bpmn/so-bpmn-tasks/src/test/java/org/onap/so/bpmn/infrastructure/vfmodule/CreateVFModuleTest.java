package org.onap.so.bpmn.infrastructure.vfmodule;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Tenant;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.cloud.resource.beans.CloudInformation;
import org.onap.so.cloud.resource.beans.NodeType;



public class CreateVFModuleTest extends BaseTaskTest {

    @Spy
    @InjectMocks
    public CreateVFModule createVFModule;

    @Mock
    protected AAIResourcesClient aaiResourcesClient;

    @Mock
    protected AAIResultWrapper aaiIResultWrapper;

    @Mock
    protected Relationships relationships;

    @Mock
    protected BuildingBlockExecution execution;


    public GeneralBuildingBlock gbb;
    public CloudRegion cloudRegion;
    private GenericVnf genericVnf;
    private VfModule vfModule;
    private ServiceInstance service;

    @Before
    public void before() {
        cloudRegion = new CloudRegion();
        cloudRegion.setCloudOwner("CloudOwner");
        cloudRegion.setLcpCloudRegionId("testRegion");
        Tenant tenant = new Tenant();
        tenant.setTenantId("tenant-001");
        tenant.setTenantName("test-tenant");
        tenant.setTenantContext("testContext");
        service = setServiceInstance();
        genericVnf = setGenericVnf();
        vfModule = setVfModule();
        gbb = new GeneralBuildingBlock();
        gbb.setCloudRegion(cloudRegion);
        gbb.setTenant(tenant);
    }

    @Test
    public void createInventoryVariableTest() throws BBObjectNotFoundException {
        doReturn(gbb).when(execution).getGeneralBuildingBlock();
        doReturn(genericVnf).when(extractPojosForBB).extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        doReturn(vfModule).when(extractPojosForBB).extractByKey(execution, ResourceKey.VF_MODULE_ID);
        doReturn("heat-stack-id").when(execution).getVariable("heatStackId");
        doReturn(NodeType.GREENFIELD).when(createVFModule).getNodeType(any(CloudRegion.class));
        createVFModule.createInventoryVariable(execution);
        verify(execution).setVariable(eq("cloudInformation"), any(CloudInformation.class));
    }

    @Test
    public void getNodeTypeBrownfieldTest() {
        doReturn(aaiResourcesClient).when(createVFModule).getAAIClient();
        doReturn(aaiIResultWrapper).when(aaiResourcesClient).get(any(AAIResourceUri.class));
        doReturn(Optional.empty()).when(aaiIResultWrapper).getRelationships();

        assertEquals(NodeType.BROWNFIELD, createVFModule.getNodeType(cloudRegion));
    }

    @Test
    public void getNodeTypeGreenfieldTest() {
        doReturn(aaiResourcesClient).when(createVFModule).getAAIClient();
        doReturn(aaiIResultWrapper).when(aaiResourcesClient).get(any(AAIResourceUri.class));
        doReturn(aaiIResultWrapper).when(aaiResourcesClient).get(any(AAIPluralResourceUri.class));
        doReturn(Optional.of(relationships)).when(aaiIResultWrapper).getRelationships();

        assertEquals(NodeType.GREENFIELD, createVFModule.getNodeType(cloudRegion));
    }
}
