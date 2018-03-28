/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *l
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.vnf;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class BpelRestClientTest {

    @Test
    public void testBpelRestClient() {

        BpelRestClient test = new BpelRestClient();

        assertEquals(test.getSocketTimeout(),5);
        assertEquals(test.getConnectTimeout(),5);
        assertEquals(test.getRetryCount(),5);
        test.setRetryCount(6);
        assertEquals(test.getRetryCount(),6);
        assertEquals(test.getRetryInterval(),-15);
        test.setRetryInterval(5);
        assertEquals(test.getRetryInterval(),5);
        test.setCredentials("credentials");
        assertEquals(test.getCredentials(),"credentials");
        test.setRetryList("1, 2, 3");
        assertEquals(test.getRetryList(),"1, 2, 3");
        assertEquals(test.getLastResponseCode(),0);
        assertEquals(test.getLastResponse(),"");
        assertTrue(test.bpelPost("bpelStr","url",true));

    }
}
