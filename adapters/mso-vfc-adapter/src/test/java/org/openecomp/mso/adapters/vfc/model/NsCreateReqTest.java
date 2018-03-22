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

public class NsCreateReqTest {
    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    NsCreateReq nsCreateReq = new NsCreateReq();

    @Test
    public void getContext() throws Exception {
        nsCreateReq.getContext();
    }

    @Test
    public void setContext() throws Exception {
        nsCreateReq.setContext(new CustomerModel());
    }

    @Test
    public void getCsarId() throws Exception {
        nsCreateReq.getCsarId();
    }

    @Test
    public void setCsarId() throws Exception {
        nsCreateReq.setCsarId("csarid");
    }

    @Test
    public void getNsName() throws Exception {
        nsCreateReq.getNsName();
    }

    @Test
    public void setNsName() throws Exception {
        nsCreateReq.setNsName("nsname");
    }

    @Test
    public void getDescription() throws Exception {
        nsCreateReq.getDescription();
    }

    @Test
    public void setDescription() throws Exception {
        nsCreateReq.setDescription("desc");
    }

}