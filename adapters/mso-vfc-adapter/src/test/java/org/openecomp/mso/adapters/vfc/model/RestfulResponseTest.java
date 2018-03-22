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

import java.util.HashMap;

public class RestfulResponseTest {
    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    RestfulResponse restfulResponse = new RestfulResponse();

    @Test
    public void getStatus() throws Exception {
        restfulResponse.getStatus();
    }

    @Test
    public void setStatus() throws Exception {
        restfulResponse.setStatus(1);
    }

    @Test
    public void getRespHeaderMap() throws Exception {
        restfulResponse.getRespHeaderMap();
    }

    @Test
    public void setRespHeaderMap() throws Exception {
        restfulResponse.setRespHeaderMap(new HashMap<>());
    }

    @Test
    public void getRespHeaderInt() throws Exception {
        restfulResponse.getRespHeaderInt("1");
    }

    @Test
    public void getRespHeaderLong() throws Exception {
        restfulResponse.getRespHeaderLong("1");
    }

    @Test
    public void getRespHeaderStr() throws Exception {
        restfulResponse.getRespHeaderStr("test");
    }

    @Test
    public void getResponseContent() throws Exception {
        restfulResponse.getResponseContent();
    }

    @Test
    public void setResponseContent() throws Exception {
        restfulResponse.setResponseContent("responseString");
    }

}