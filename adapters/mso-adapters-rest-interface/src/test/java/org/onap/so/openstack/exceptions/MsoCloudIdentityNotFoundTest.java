/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.onap.so.openstack.exceptions;

import org.junit.Test;
import org.junit.Assert;

public class MsoCloudIdentityNotFoundTest {

    @Test
    public void testConstructor() {
        MsoCloudIdentityNotFound msoCloudIdentityNotFound = new MsoCloudIdentityNotFound();
        Assert.assertEquals("Cloud Identity not found",msoCloudIdentityNotFound.getMessage());
        Assert.assertEquals("Cloud Identity not found",msoCloudIdentityNotFound.toString());
        Assert.assertEquals(MsoExceptionCategory.USERDATA, msoCloudIdentityNotFound.category);
    }

    @Test
    public void testOverloadedConstructor() {
        MsoCloudIdentityNotFound msoCloudIdentityNotFound = new MsoCloudIdentityNotFound("test");
        Assert.assertEquals("Cloud Identity [test] not found",msoCloudIdentityNotFound.getMessage());
        Assert.assertEquals("Cloud Identity [test] not found",msoCloudIdentityNotFound.toString());
        Assert.assertEquals(MsoExceptionCategory.USERDATA, msoCloudIdentityNotFound.category);
    }

}
