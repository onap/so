/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
 * Copyright (c) 2022, Samsung Electronics. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.requestsdb.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class MsoRequestsDbExceptionTest {
    @Test
    public void testConstructorWithMessaqge() {
        String message = "testing message";
        MsoRequestsDbException msoRequestsDbException = new MsoRequestsDbException(message);
        Assert.assertNull(msoRequestsDbException.getCause());
        Assert.assertEquals(message, msoRequestsDbException.getLocalizedMessage());
        Assert.assertEquals(message, msoRequestsDbException.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        MsoRequestsDbException msoRequestsDbException = new MsoRequestsDbException(throwable);
        Assert.assertEquals(throwable, msoRequestsDbException.getCause());
        Assert.assertTrue(msoRequestsDbException.getLocalizedMessage().contains(message));
        Assert.assertTrue(msoRequestsDbException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessageAndThrowable() {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        MsoRequestsDbException msoRequestsDbException = new MsoRequestsDbException(message, throwable);
        Assert.assertEquals(throwable, msoRequestsDbException.getCause());
        Assert.assertTrue(msoRequestsDbException.getLocalizedMessage().contains(message));
        Assert.assertTrue(msoRequestsDbException.getMessage().contains(message));
    }

    /* test method */
    @Test
    public void testGetFaultInfo() {
        MsoRequestsDbExceptionBean faultInfo = new MsoRequestsDbExceptionBean();
        MsoRequestsDbException soRequestsDbException = new MsoRequestsDbException("message");
        soRequestsDbException.setFaultInfo(faultInfo);
        Assert.assertNotNull(soRequestsDbException.getFaultInfo());
        Assert.assertEquals(soRequestsDbException.getFaultInfo(), faultInfo);
    }

}

