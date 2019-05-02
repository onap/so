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
    public void testInstanceResourceList() throws IOException {
        String uuiRequest = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "InstanceResourceList" + ".json")));
        List<Resource> instanceResourceList =
                InstanceResourceList.getInstanceResourceList(createResourceSequence(), uuiRequest);
        Assert.assertEquals(4, instanceResourceList.size());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(0).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(1).getResourceType());
        Assert.assertEquals(ResourceType.VNF, instanceResourceList.get(2).getResourceType());
        Assert.assertEquals(ResourceType.GROUP, instanceResourceList.get(3).getResourceType());
    }

    private List<Resource> createResourceSequence() {
        List<Resource> resourceList = new ArrayList<>();
        VnfResource vnfResource = new VnfResource();
        vnfResource.setResourceInput("{\"a\":\"[sdwansiteresource_list,INDEX,sdwansiteresource_list]\"}");

        VnfcResource vnfcResource = new VnfcResource();
        vnfcResource.setResourceInput("{\"a\":\"[sdwansitewan_list,INDEX,test]\"}");

        GroupResource groupResource = new GroupResource();
        groupResource.setVnfcs(Arrays.asList(vnfcResource));

        return Arrays.asList(vnfResource, groupResource);
    }
}
