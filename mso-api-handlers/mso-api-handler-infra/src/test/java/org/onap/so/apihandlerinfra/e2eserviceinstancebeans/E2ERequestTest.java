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

public class E2ERequestTest {

    E2ERequest test = new E2ERequest();

    @Test
    public void verifyE2ERequest() {

        test.setOperationId("operationId");
        assertEquals(test.getOperationId(), "operationId");
        test.setOperation("operation");
        assertEquals(test.getOperation(), "operation");
        test.setResult("result");
        assertEquals(test.getResult(), "result");
        test.setReason("test");
        assertEquals(test.getReason(), "test");
        test.setUserId("userId");
        assertEquals(test.getUserId(), "userId");
        test.setOperationContent("operation");
        assertEquals(test.getOperationContent(), "operation");
        test.setProgress(123);
        assertEquals(test.getProgress(), 123);
        test.setOperateAt("operate");
        assertEquals(test.getOperateAt(), "operate");
        test.setFinishedAt("finished");
        assertEquals(test.getFinishedAt(), "finished");
    }

}
