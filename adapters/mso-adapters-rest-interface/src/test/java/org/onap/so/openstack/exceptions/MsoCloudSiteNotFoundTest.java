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

public class MsoCloudSiteNotFoundTest {

    @Test
    public void testConstructor() {
        MsoCloudSiteNotFound msoCloudSiteNotFound = new MsoCloudSiteNotFound();
        Assert.assertEquals("Cloud site not found", msoCloudSiteNotFound.getMessage());
        Assert.assertEquals("Cloud site not found", msoCloudSiteNotFound.toString());
        Assert.assertEquals(MsoExceptionCategory.USERDATA, msoCloudSiteNotFound.category);
    }

    @Test
    public void testOverloadedConstructor() {
        MsoCloudSiteNotFound msoCloudSiteNotFound = new MsoCloudSiteNotFound("test");
        Assert.assertEquals("Cloud Site [test] not found", msoCloudSiteNotFound.getMessage());
        Assert.assertEquals("Cloud Site [test] not found", msoCloudSiteNotFound.toString());
        Assert.assertEquals(MsoExceptionCategory.USERDATA, msoCloudSiteNotFound.category);
    }

}
