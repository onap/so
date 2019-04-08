/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.sdnc.impl;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.sdnc.SDNCAdapterApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SDNCAdapterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MapRequestTunablesTest {

    @Autowired
    private MapRequestTunables tunableMapper;

    @Test
    public void test_setTunables() {
        RequestTunables expectedResult = new RequestTunables("testReqId", "", "vnf-topology-operation", "assign");
        expectedResult.setAsyncInd("N");
        expectedResult.setSdncUrl("https://localhost:8443/restconf/operations/VNF-API:vnf-topology-operation");
        expectedResult.setTimeout("270000");
        expectedResult.setReqMethod("POST");
        expectedResult.setHeaderName("sdnc-request-header");
        expectedResult.setNamespace("org:openecomp:sdnctl:vnf");

        RequestTunables testMapper = new RequestTunables("testReqId", "", "vnf-topology-operation", "assign");

        RequestTunables mappedTunable = tunableMapper.setTunables(testMapper);

        assertThat(mappedTunable, sameBeanAs(expectedResult));
    }

    @Test
    public void test_setTunables_EmptyOperation_EmptyMSOAction() {
        RequestTunables expectedResult = new RequestTunables("testReqId", "", "", "query");
        expectedResult.setAsyncInd("N");
        expectedResult.setSdncUrl("http://localhost:8443/restconf/operations/GENERIC-RESOURCE-API:");
        expectedResult.setTimeout("60000");
        expectedResult.setReqMethod("GET");
        expectedResult.setHeaderName("sdnc-request-header");
        expectedResult.setNamespace("");

        RequestTunables testMapper = new RequestTunables("testReqId", "", "", "query");

        RequestTunables mappedTunable = tunableMapper.setTunables(testMapper);

        assertThat(mappedTunable, sameBeanAs(expectedResult));
    }

    @Test
    public void test_setTunables_EmptyOperation() {
        RequestTunables expectedResult = new RequestTunables("testReqId", "infra", "", "query");
        expectedResult.setAsyncInd("N");
        expectedResult.setSdncUrl("https://localhost:8443/restconf/config");
        expectedResult.setTimeout("60000");
        expectedResult.setReqMethod("GET");
        expectedResult.setHeaderName("sdnc-request-header");
        expectedResult.setNamespace("");

        RequestTunables testMapper = new RequestTunables("testReqId", "infra", "", "query");

        RequestTunables mappedTunable = tunableMapper.setTunables(testMapper);

        assertThat(mappedTunable, sameBeanAs(expectedResult));
    }

    @Test
    public void test_setTunables_EmptyOperation_EmptyMSOActionPUT() {
        RequestTunables expectedResult = new RequestTunables("testReqId", "", "", "put");
        expectedResult.setAsyncInd("N");
        expectedResult.setSdncUrl("https://localhost:8443/restconf/config");
        expectedResult.setTimeout("60000");
        expectedResult.setReqMethod("PUT");
        expectedResult.setHeaderName("sdnc-request-header");
        expectedResult.setNamespace("");

        RequestTunables testMapper = new RequestTunables("testReqId", "", "", "put");

        RequestTunables mappedTunable = tunableMapper.setTunables(testMapper);

        assertThat(mappedTunable, sameBeanAs(expectedResult));
    }


    @Test
    public void test_setTunables_EmptyOperation_EmptyMSOActionRESTDELETE() {
        RequestTunables expectedResult = new RequestTunables("testReqId", "", "", "restdelete");
        expectedResult.setAsyncInd("N");
        expectedResult.setSdncUrl("https://localhost:8443/restconf/config");
        expectedResult.setTimeout("60000");
        expectedResult.setReqMethod("DELETE");
        expectedResult.setHeaderName("sdnc-request-header");
        expectedResult.setNamespace("");

        RequestTunables testMapper = new RequestTunables("testReqId", "", "", "restdelete");

        RequestTunables mappedTunable = tunableMapper.setTunables(testMapper);

        assertThat(mappedTunable, sameBeanAs(expectedResult));
    }

}
