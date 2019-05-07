package org.onap.so.bpmn.common.resource;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.bpmn.core.domain.GroupResource;
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
    public void testInstanceResourceTwoVF() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        List<Resource> instanceResourceList =
                InstanceResourceList.getInstanceResourceList(createResourceSequence1(), uuiRequest);
        Assert.assertEquals(2, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(1).getResourceType());
    }


    @Test
    public void testInstanceResourceListWithOneVFOneGrp() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        List<Resource> instanceResourceList =
                InstanceResourceList.getInstanceResourceList(createResourceSequence2(), uuiRequest);
        Assert.assertEquals(4, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(2).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(3).getResourceType());

    }

    @Test
    public void testInstanceResourceList3() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        List<Resource> instanceResourceList =
                InstanceResourceList.getInstanceResourceList(createResourceSequence3(), uuiRequest);
        Assert.assertEquals(6, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(2).getResourceType());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(3).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(4).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(5).getResourceType());

    }

    private List<Resource> createResourceSequence1() {
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"[sdwansiteresource_list,INDEX,sdwansiteresource_list]\"}");

        return Arrays.asList(vnfResource);
    }

    private List<Resource> createResourceSequence2() {

        VnfcResource vnfcResource = new VnfcResource();
        vnfcResource.setResourceInput("{\"a\":\"[sdwansitewan_list,INDEX,test]\"}");

        GroupResource groupResource = new GroupResource();
        groupResource.setVnfcs(Arrays.asList(vnfcResource));

        ArrayList<Resource> seq = new ArrayList<>();
        seq.addAll(createResourceSequence1());
        seq.add(groupResource);
        return seq;
    }

    private List<Resource> createResourceSequence3() {
        VnfcResource vnfcResource = new VnfcResource();
        vnfcResource.setResourceInput("{\"b\":\"[sdwandevice_list,INDEX,test]\"}");

        GroupResource groupResource = new GroupResource();
        groupResource.setVnfcs(Arrays.asList(vnfcResource));

        ArrayList<Resource> seq = new ArrayList<>();
        seq.addAll(createResourceSequence2());
        seq.add(groupResource);
        return seq;
    }
}
