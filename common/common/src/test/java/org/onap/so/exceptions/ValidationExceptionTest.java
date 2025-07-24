/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.exceptions;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ANANDSAN on 4/12/2018.
 */
public class ValidationExceptionTest {
    @Test
    public void validationExceptionOverrideMessageFalse() {
        ValidationException e = new ValidationException("testMessage", false);
        Assert.assertEquals("testMessage", e.getMessage());
    }

    @Test
    public void validationExceptionOverrideMessageTrue() {
        ValidationException e = new ValidationException("testMessage", true);
        Assert.assertEquals("No valid testMessage is specified", e.getMessage());
    }

    @Test
    public void validationException() {
        ValidationException e = new ValidationException("testMessage");
        Assert.assertEquals("No valid testMessage is specified", e.getMessage());
    }

    @Test
    public void validationExceptionVersion() {
        ValidationException e = new ValidationException("testMessage", "secondTestMessage");
        Assert.assertEquals("testMessage does not match secondTestMessage", e.getMessage());
    }
}
