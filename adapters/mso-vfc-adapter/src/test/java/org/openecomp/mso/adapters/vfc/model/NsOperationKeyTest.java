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

public class NsOperationKeyTest {
    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    NsOperationKey nsOperationKey = new NsOperationKey();

    @Test
    public void getGlobalSubscriberId() throws Exception {
        nsOperationKey.getGlobalSubscriberId();
    }

    @Test
    public void setGlobalSubscriberId() throws Exception {
        nsOperationKey.setGlobalSubscriberId("subscriberid");
    }

    @Test
    public void getServiceType() throws Exception {
        nsOperationKey.getServiceType();
    }

    @Test
    public void setServiceType() throws Exception {
        nsOperationKey.setServiceType("servicetype");
    }

    @Test
    public void getServiceId() throws Exception {
        nsOperationKey.getServiceId();
    }

    @Test
    public void setServiceId() throws Exception {
        nsOperationKey.setServiceId("serviceid");
    }

    @Test
    public void getOperationId() throws Exception {
        nsOperationKey.getOperationId();
    }

    @Test
    public void setOperationId() throws Exception {
        nsOperationKey.setOperationId("test");
    }

    @Test
    public void getNodeTemplateUUID() throws Exception {
        nsOperationKey.getNodeTemplateUUID();
    }

    @Test
    public void setNodeTemplateUUID() throws Exception {
        nsOperationKey.setNodeTemplateUUID("nodeTemplateid");
    }

}