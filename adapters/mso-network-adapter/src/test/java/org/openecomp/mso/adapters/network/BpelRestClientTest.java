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
package org.openecomp.mso.adapters.network;

import org.junit.Test;

public class BpelRestClientTest {

    BpelRestClient bpelRestClient = new BpelRestClient();

    @Test
    public void getSocketTimeoutTest() throws Exception {
        bpelRestClient.getSocketTimeout();
    }

    @Test
    public void setSocketTimeoutTest() throws Exception {
        bpelRestClient.setSocketTimeout(300);
    }

    @Test
    public void getConnectTimeoutTest() throws Exception {
        bpelRestClient.getConnectTimeout();
    }

    @Test
    public void setConnectTimeoutTest() throws Exception {
        bpelRestClient.setConnectTimeout(200);
    }

    @Test
    public void getRetryCountTest() throws Exception {
        bpelRestClient.getRetryCount();
    }

    @Test
    public void setRetryCountTest() throws Exception {
        bpelRestClient.setRetryCount(3);
    }

    @Test
    public void getRetryIntervalTest() throws Exception {
        bpelRestClient.getRetryInterval();
    }

    @Test
    public void setRetryIntervalTest() throws Exception {
        bpelRestClient.setRetryInterval(3);
    }

    @Test
    public void getCredentialsTest() throws Exception {
        bpelRestClient.getCredentials();
    }

    @Test
    public void setCredentialsTest() throws Exception {
        bpelRestClient.setCredentials("test");
    }

    @Test
    public void getRetryListTest() throws Exception {
        bpelRestClient.getRetryList();
    }

    @Test
    public void setRetryListTest() throws Exception {
        bpelRestClient.setRetryList("retry list");
    }

    @Test
    public void getLastResponseCodeTest() throws Exception {
        bpelRestClient.getLastResponseCode();
    }

    @Test
    public void getLastResponseTest() throws Exception {
        bpelRestClient.getLastResponse();
    }
}