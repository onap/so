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
package org.openecomp.mso.adapters.vfc.model;

import org.junit.Test;

public class NSResourceInputParameterTest {
    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    NSResourceInputParameter nsResourceInputParameter = new NSResourceInputParameter();

    @Test
    public void getNsServiceName() throws Exception {
        nsResourceInputParameter.getNsServiceName();
    }

    @Test
    public void setNsServiceName() throws Exception {
        nsResourceInputParameter.setNsServiceName("service");
    }

    @Test
    public void getNsServiceDescription() throws Exception {
        nsResourceInputParameter.getNsServiceDescription();
    }

    @Test
    public void setNsServiceDescription() throws Exception {
        nsResourceInputParameter.setNsServiceDescription("desc");
    }

    @Test
    public void getNsParameters() throws Exception {
        nsResourceInputParameter.getNsParameters();
    }

    @Test
    public void setNsParameters() throws Exception {
        nsResourceInputParameter.setNsParameters(new NsParameters());
    }

    @Test
    public void getNsOperationKey() throws Exception {
        nsResourceInputParameter.getNsOperationKey();
    }

    @Test
    public void setNsOperationKey() throws Exception {
        nsResourceInputParameter.setNsOperationKey(new NsOperationKey());
    }

}