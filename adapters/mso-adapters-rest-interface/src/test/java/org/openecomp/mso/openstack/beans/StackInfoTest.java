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
package org.openecomp.mso.openstack.beans;

import org.junit.Test;

import java.util.HashMap;

public class StackInfoTest {

    StackInfo stackInfo = new StackInfo();

    @Test
    public void getName() throws Exception {
        stackInfo.getName();
    }

    @Test
    public void setName() throws Exception {
        stackInfo.setName("test");
    }

    @Test
    public void getCanonicalName() throws Exception {
        stackInfo.getCanonicalName();
    }

    @Test
    public void setCanonicalName() throws Exception {
        stackInfo.setCanonicalName("test");
    }

    @Test
    public void getStatus() throws Exception {
        stackInfo.getStatus();
    }

    @Test
    public void setStatus() throws Exception {
        stackInfo.setStatus(HeatStatus.BUILDING);
    }

    @Test
    public void getStatusMessage() throws Exception {
        stackInfo.getStatusMessage();
    }

    @Test
    public void setStatusMessage() throws Exception {
        stackInfo.setStatusMessage("test");
    }

    @Test
    public void getOutputs() throws Exception {
        stackInfo.getOutputs();
    }

    @Test
    public void setOutputs() throws Exception {
        stackInfo.setOutputs(new HashMap<>());
    }

    @Test
    public void getParameters() throws Exception {
        stackInfo.getParameters();
    }

    @Test
    public void setParameters() throws Exception {
        stackInfo.setParameters(new HashMap<>());
    }

}