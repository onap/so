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

package org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans;

import org.junit.Test;

public class E2ERequestTest {

    E2ERequest test = new E2ERequest();

    @Test
    public void getOperationIdTest() throws Exception {
        test.getOperationId();
    }

    @Test
    public void setOperationIdTest() throws Exception {
        test.setOperationId("id");
    }

    @Test
    public void getOperationTest() throws Exception {
        test.getOperation();
    }

    @Test
    public void setOperationTest() throws Exception {
        test.setOperation("operation");
    }

    @Test
    public void getResultTest() throws Exception {
        test.getResult();
    }

    @Test
    public void setResultTest() throws Exception {
        test.setResult("result");
    }

    @Test
    public void getReasonTest() throws Exception {
        test.getReason();
    }

    @Test
    public void setReasonTest() throws Exception {
        test.setReason("test");
    }

    @Test
    public void getUserIdTest() throws Exception {
        test.getUserId();
    }

    @Test
    public void setUserIdTest() throws Exception {
        test.setUserId("userId");
    }

    @Test
    public void getOperationContentTest() throws Exception {
        test.getOperationContent();
    }

    @Test
    public void setOperationContentTest() throws Exception {
        test.setOperationContent("operation");
    }

    @Test
    public void getProgressTest() throws Exception {
        test.getProgress();
    }

    @Test
    public void setProgressTest() throws Exception {
        test.setProgress(123);
    }

    @Test
    public void getOperateAtTest() throws Exception {
        test.getOperateAt();
    }

    @Test
    public void setOperateAtTest() throws Exception {
        test.setOperateAt("operate");
    }

    @Test
    public void getFinishedAtTest() throws Exception {
        test.getFinishedAt();
    }

    @Test
    public void setFinishedAtTest() throws Exception {
        test.setFinishedAt("finished");
    }
}
