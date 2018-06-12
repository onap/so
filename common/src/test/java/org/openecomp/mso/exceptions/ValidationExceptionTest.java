package org.openecomp.mso.exceptions;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ANANDSAN on 4/12/2018.
 */
public class ValidationExceptionTest {
    @Test
    public void validationExceptionOverrideMessageFalse(){
        ValidationException e = new ValidationException("testMessage", false);
        Assert.assertEquals("testMessage", e.getMessage());
    }

    @Test
    public void validationExceptionOverrideMessageTrue(){
        ValidationException e = new ValidationException("testMessage", true);
        Assert.assertEquals("No valid testMessage is specified", e.getMessage());
    }

    @Test
    public void validationException(){
        ValidationException e = new ValidationException("testMessage");
        Assert.assertEquals("No valid testMessage is specified", e.getMessage());
    }

    @Test
    public void validationExceptionVersion(){
        ValidationException e = new ValidationException("testMessage", "1.0");
        Assert.assertEquals("testMessage is not valid in the 1.0 version", e.getMessage());
    }
}
