package org.openecomp.mso.bpmn.common.resource;

import org.junit.Test;

import java.util.HashMap;

public class ResourceRequestBuilderTest {
    @Test
    public void buildResouceRequestTest() throws Exception {
        ResourceRequestBuilder.buildResouceRequest("aa4535",
                "95034", new HashMap<>());
    }

}