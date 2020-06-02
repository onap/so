package org.onap.so.bpmn.common.resource;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.bpmn.core.domain.GroupResource;
import org.onap.so.bpmn.core.domain.ModelInfo;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ResourceType;
import org.onap.so.bpmn.core.domain.VnfResource;
import org.onap.so.bpmn.core.domain.VnfcResource;
import org.onap.so.bpmn.core.domain.ModuleResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstnaceResourceListTest {

    public static String RESOURCE_PATH = "src/test/resources/__files/InstanceResourceList/";

    @Test
    public void testInstanceResourceList() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        List<Resource> instanceResourceList =
                InstanceResourceList.getInstanceResourceList(createResourceSequence(), uuiRequest);
        Assert.assertEquals(9, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals("device", instanceResourceList.get(1).getModelInfo().getModelName());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(2).getResourceType());
        Assert.assertEquals("sitewan", instanceResourceList.get(2).getModelInfo().getModelName());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(3).getResourceType());
        Assert.assertEquals("sitewan", instanceResourceList.get(3).getModelInfo().getModelName());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(4).getResourceType());
        Assert.assertEquals("dummy", instanceResourceList.get(4).getModelInfo().getModelName());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(5).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(6).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(7).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(8).getResourceType());
    }

    // Test when PK is empty
    @Test
    public void testSimpleVFResource() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"ipaddress|127.0.0.1\"}");
        List<Resource> instanceResourceList = InstanceResourceList.getInstanceResourceList(vnfResource, uuiRequest);
        Assert.assertEquals(1, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
    }

    @Test
    public void testSimpleVFResourceWithGroup() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"ipaddress|127.0.0.1\"}");

        createGroupKeyResource(vnfResource);

        List<Resource> instanceResourceList = InstanceResourceList.getInstanceResourceList(vnfResource, uuiRequest);
        Assert.assertEquals(2, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals("wan", instanceResourceList.get(1).getModelInfo().getModelName());
    }

    @Test
    public void testVFResourceWithEmptyGroup() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"[emptygroup_list2,INDEX,name]\"}");

        List<Resource> instanceResourceList = InstanceResourceList.getInstanceResourceList(vnfResource, uuiRequest);
        Assert.assertEquals(1, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
    }

    @Test
    public void testVFResourceWithModule() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"ipaddress|127.0.0.1\"}");

        // Come from package org.onap.so.bpmn.core.domain.VnfResourceTest
        List<ModuleResource> moduleResources;
        moduleResources = new ArrayList<>();
        ModuleResource moduleresource = getModuleResource();
        moduleResources.add(moduleresource);
        vnfResource.setModules(moduleResources);

        List<Resource> instanceResourceList = InstanceResourceList.getInstanceResourceList(vnfResource, uuiRequest);
        Assert.assertEquals(2, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.MODULE, instanceResourceList.get(1).getResourceType());
    }

    private ModuleResource getModuleResource() {
        ModuleResource moduleresource = new ModuleResource();
        moduleresource.setVfModuleName("vfModuleName");
        moduleresource.setHeatStackId("heatStackId");
        moduleresource.setIsBase(true);
        moduleresource.setVfModuleLabel("vfModuleLabel");
        moduleresource.setInitialCount(0);
        moduleresource.setVfModuleType("vfModuleType");
        moduleresource.setHasVolumeGroup(true);
        return moduleresource;
    }

    // Test when PK is not empty and PK does not contain any groups
    @Test
    public void testVFWithEmptyGroupResource() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"[emptygroup_list,INDEX,name]\"}");
        List<Resource> instanceResourceList = InstanceResourceList.getInstanceResourceList(vnfResource, uuiRequest);
        Assert.assertEquals(1, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
    }

    // Test when PK is not empty and contains a group which SK is empty
    @Test
    public void testVFWithEmptyGroupKeyResource() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"[emptygroup_list,INDEX,name]\"}");

        createGroupKeyResource(vnfResource);

        List<Resource> instanceResourceList = InstanceResourceList.getInstanceResourceList(vnfResource, uuiRequest);
        Assert.assertEquals(2, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals("wan", instanceResourceList.get(1).getModelInfo().getModelName());
    }

    private void createGroupKeyResource(VnfResource vnfResource) {
        GroupResource groupResource = prepareGroupResource("{\"a\":\"test|default_value\"}", "wan");

        vnfResource.setGroupOrder("wan");
        vnfResource.setGroups(Arrays.asList(groupResource));
    }

    private VnfResource createResourceSequence() {
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"[sdwansiteresource_list,INDEX,sdwansiteresource_list]\"}");

        GroupResource groupResource = prepareGroupResource("{\"a\":\"[sdwansitewan_list,INDEX,test]\"}", "sitewan");
        GroupResource groupResource2 = prepareGroupResource("{\"a\":\"[sdwandevice_list,INDEX,test]\"}", "device");
        GroupResource groupDummyResource = prepareGroupResource("{\"a\":\"[dummy,INDEX,test]\"}", "dummy");

        vnfResource.setGroupOrder("device,sitewan,dummy");
        vnfResource.setGroups(Arrays.asList(groupResource, groupResource2, groupDummyResource));
        return vnfResource;
    }

    private GroupResource prepareGroupResource(String sourceInput, String modelName) {
        VnfcResource vnfcDeviceResource = new VnfcResource();
        vnfcDeviceResource.setResourceInput(sourceInput);
        GroupResource groupResource = new GroupResource();
        groupResource.setVnfcs(Arrays.asList(vnfcDeviceResource));
        ModelInfo deviceModel = new ModelInfo();
        deviceModel.setModelName(modelName);
        groupResource.setModelInfo(deviceModel);
        return groupResource;
    }
}
