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
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.vfc.model;

import org.junit.Test;

public class ResponseDescriptorTest {
    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    ResponseDescriptor responseDescriptor = new ResponseDescriptor();

    @Test
    public void getStatus() throws Exception {
        responseDescriptor.getStatus();
    }

    @Test
    public void setStatus() throws Exception {
        responseDescriptor.setStatus("test");
    }

    @Test
    public void getProgress() throws Exception {
        responseDescriptor.getProgress();
    }

    @Test
    public void setProgress() throws Exception {
        responseDescriptor.setProgress("10");
    }

    @Test
    public void getStatusDescription() throws Exception {
        responseDescriptor.getStatusDescription();
    }

    @Test
    public void setStatusDescription() throws Exception {
        responseDescriptor.setStatusDescription("test");
    }

    @Test
    public void getErrorCode() throws Exception {
        responseDescriptor.getErrorCode();
    }

    @Test
    public void setErrorCode() throws Exception {
        responseDescriptor.setErrorCode(-1);
    }

    @Test
    public void getResponseId() throws Exception {
        responseDescriptor.getResponseId();
    }

    @Test
    public void setResponseId() throws Exception {
        responseDescriptor.setResponseId(1);
    }

}
