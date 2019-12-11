package org.onap.so.bpmn.servicedecomposition.tasks.exceptions;

import org.junit.Assert;
import org.junit.Test;


public class ServiceModelNotFoundExceptionTest {

    @Test
    public void testRequestValidationException() {

        ServiceModelNotFoundException serviceModelNotFoundException = new ServiceModelNotFoundException();
        Assert.assertNull(serviceModelNotFoundException.getMessage());

        serviceModelNotFoundException = new ServiceModelNotFoundException("test message");
        Assert.assertEquals("test message", serviceModelNotFoundException.getMessage());

    }
}
