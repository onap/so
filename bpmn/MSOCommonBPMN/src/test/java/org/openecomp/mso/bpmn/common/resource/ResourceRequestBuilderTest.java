package org.openecomp.mso.bpmn.common.resource;

import org.junit.Test;

import java.util.HashMap;

public class ResourceRequestBuilderTest {
    @Test
    public void buildResouceRequestTest() throws Exception {
        ResourceRequestBuilder.buildResouceRequest("aa4535",
                "a1074969-944f-4ddc-b687-9550b0c8cd57", new HashMap<>());
    }

}