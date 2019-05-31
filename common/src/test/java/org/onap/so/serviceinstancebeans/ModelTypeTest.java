package org.onap.so.serviceinstancebeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ModelTypeTest {



    @Test
    public void reflectionTest() {
        TestServiceInstanceBean a = new TestServiceInstanceBean();
        TestServiceInstanceBean b = new TestServiceInstanceBean();

        a.setServiceInstanceId("my-id-a");
        a.setServiceInstanceName("my-name-a");

        b.setServiceInstanceId("my-id-b");
        b.setServiceInstanceName("my-name-b");

        assertEquals("my-id-a", ModelType.service.getId(a));
        assertEquals("my-name-a", ModelType.service.getName(a));
        ModelType.service.setName(a, ModelType.service.getName(b));
        ModelType.service.setId(a, ModelType.service.getId(b));
        assertEquals("my-name-b", ModelType.service.getName(a));
        assertEquals("my-id-b", ModelType.service.getId(a));
    }

    @Test
    public void testSilentFail() {
        TestServiceInstanceBean a = new TestServiceInstanceBean();

        a.setServiceInstanceId("my-id-a");
        a.setServiceInstanceName("my-name-a");
        assertNull(ModelType.service.get(a, "NoField"));
    }
}
