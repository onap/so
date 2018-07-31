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

package org.onap.so.apihandlerinfra.e2eserviceinstancebeans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class E2EServiceInstanceDeleteRequestTest {


    E2EServiceInstanceDeleteRequest test = new E2EServiceInstanceDeleteRequest();

    @Test
    public void verifyE2ESerInstanceDelReq() throws Exception {

        test.setGlobalSubscriberId("id");
        assertEquals(test.getGlobalSubscriberId(),"id");
        test.setServiceType("type");
        assertEquals(test.getServiceType(),"type");
    }

}
