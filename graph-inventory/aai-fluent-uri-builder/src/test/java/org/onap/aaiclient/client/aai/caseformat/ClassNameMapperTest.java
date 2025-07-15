package org.onap.aaiclient.client.aai.caseformat;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Test;
import org.onap.aai.domain.yang.ServiceInstance;

public class ClassNameMapperTest {

    @Test
    public void classToLowerHyphen() {
        assertEquals("service-instance", ClassNameMapper.getInstance().toLowerHyphen(ServiceInstance.class));
    }

    @Test
    public void classToLowerHyphenNotFound() {
        assertNull(ClassNameMapper.getInstance().toLowerHyphen(String.class));
    }

    @Test
    public void classToUpperCamel() {
        assertEquals("service-instance", ClassNameMapper.getInstance().toUpperCamel(ServiceInstance.class));
    }

    @Test
    public void classToUpperCamelNotFound() {
        assertNull(ClassNameMapper.getInstance().toUpperCamel(String.class));
    }
}
