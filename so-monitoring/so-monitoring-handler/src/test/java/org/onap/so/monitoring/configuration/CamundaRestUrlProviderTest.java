/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.configuration;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.onap.so.monitoring.configuration.camunda.CamundaRestUrlProvider;

/**
 * @author waqas.ikram@ericsson.com
 */
public class CamundaRestUrlProviderTest {
    private static final String DEFAULT = "default";
    private static final String CAMUNDA_REST_API_URL = "http://localhost:9080/engine-rest/engine/";
    private static final String BASE_URL = CAMUNDA_REST_API_URL + DEFAULT;
    private final CamundaRestUrlProvider objUnderTest = new CamundaRestUrlProvider(CAMUNDA_REST_API_URL, DEFAULT);
    private static final String ID = UUID.randomUUID().toString();


    @Test
    public void test_GetHistoryProcessInstanceUrl() {
        final String expectedUrl = BASE_URL + "/history/process-instance?variables=requestId_eq_" + ID;
        final String actualUrl = objUnderTest.getHistoryProcessInstanceUrl(ID);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void test_GetProcessInstanceUrl() {
        final String expectedUrl = BASE_URL + "/history/process-instance/" + ID;
        final String actualUrl = objUnderTest.getSingleProcessInstanceUrl(ID);
        assertEquals(expectedUrl, actualUrl);
    }


    @Test
    public void test_GetProcessDefinitionUrl() {
        final String expectedUrl = BASE_URL + "/process-definition/" + ID + "/xml";
        final String actualUrl = objUnderTest.getProcessDefinitionUrl(ID);
        assertEquals(expectedUrl, actualUrl);

    }

    @Test
    public void test_GetActivityIntanceUrl() {
        final String expectedUrl =
                BASE_URL + "/history/activity-instance?processInstanceId=" + ID + "&sortBy=startTime&sortOrder=asc";
        final String actualUrl = objUnderTest.getActivityInstanceUrl(ID);
        assertEquals(expectedUrl, actualUrl);

    }

    @Test
    public void test_GetProcessInstanceVariablesUrl() {
        final String expectedUrl = BASE_URL + "/history/variable-instance?processInstanceId=" + ID;
        final String actualUrl = objUnderTest.getProcessInstanceVariablesUrl(ID);
        assertEquals(expectedUrl, actualUrl);

    }

}
