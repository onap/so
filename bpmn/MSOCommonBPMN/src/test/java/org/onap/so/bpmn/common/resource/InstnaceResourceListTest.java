package org.onap.so.bpmn.common.resource;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.bpmn.core.domain.GroupResource;
import org.onap.so.bpmn.core.domain.ModelInfo;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ResourceType;
import org.onap.so.bpmn.core.domain.VnfResource;
import org.onap.so.bpmn.core.domain.VnfcResource;
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
        Assert.assertEquals(7, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals("device", instanceResourceList.get(1).getModelInfo().getModelName());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(2).getResourceType());
        Assert.assertEquals("sitewan", instanceResourceList.get(2).getModelInfo().getModelName());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(3).getResourceType());
        Assert.assertEquals("sitewan", instanceResourceList.get(3).getModelInfo().getModelName());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(4).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(5).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(6).getResourceType());
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

        VnfcResource vnfcResource = new VnfcResource();
        vnfcResource.setResourceInput("{\"a\":\"test|default_value\"}");
        GroupResource groupResource = new GroupResource();
        groupResource.setVnfcs(Arrays.asList(vnfcResource));
        ModelInfo wanModel = new ModelInfo();
        wanModel.setModelName("wan");
        groupResource.setModelInfo(wanModel);

        vnfResource.setGroupOrder("wan");
        vnfResource.setGroups(Arrays.asList(groupResource));

        List<Resource> instanceResourceList = InstanceResourceList.getInstanceResourceList(vnfResource, uuiRequest);
        Assert.assertEquals(2, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals("wan", instanceResourceList.get(1).getModelInfo().getModelName());
    }

    private VnfResource createResourceSequence() {
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"[sdwansiteresource_list,INDEX,sdwansiteresource_list]\"}");

        VnfcResource vnfcResource = new VnfcResource();
        vnfcResource.setResourceInput("{\"a\":\"[sdwansitewan_list,INDEX,test]\"}");

        GroupResource groupResource = new GroupResource();
        groupResource.setVnfcs(Arrays.asList(vnfcResource));
        ModelInfo wanModel = new ModelInfo();
        wanModel.setModelName("sitewan");
        groupResource.setModelInfo(wanModel);

        VnfcResource vnfcDeviceResource = new VnfcResource();
        vnfcDeviceResource.setResourceInput("{\"a\":\"[sdwandevice_list,INDEX,test]\"}");
        GroupResource groupResource2 = new GroupResource();
        groupResource2.setVnfcs(Arrays.asList(vnfcDeviceResource));
        ModelInfo deviceModel = new ModelInfo();
        deviceModel.setModelName("device");
        groupResource2.setModelInfo(deviceModel);

        vnfResource.setGroupOrder("device,sitewan");
        vnfResource.setGroups(Arrays.asList(groupResource, groupResource2));
        return vnfResource;
    }
}
