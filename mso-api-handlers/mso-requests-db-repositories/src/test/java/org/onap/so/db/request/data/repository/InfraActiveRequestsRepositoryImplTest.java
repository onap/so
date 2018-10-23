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
package org.onap.so.db.request.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.db.request.data.repository.InfraActiveRequestsRepositoryImpl.ACTION;
import static org.onap.so.db.request.data.repository.InfraActiveRequestsRepositoryImpl.REQUEST_ID;
import static org.onap.so.db.request.data.repository.InfraActiveRequestsRepositoryImpl.SERVICE_INSTANCE_ID;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class InfraActiveRequestsRepositoryImplTest {

    private static final int MAX_LIMIT = 1;
    private static final long END_TIME_IN_MILISEC = 1482580740000l;      // December 23, 2016 23:59 PM
    private static final long START_TIME_IN_MILISEC = 1482282000000l;    // December 21, 2016 01:00 AM
    private static final String REQUEST_ID_VALUE = "00032ab7-3fb3-42e5-965d-8ea592502017";
    private static final String SERVICE_INSTANCE_ID_VALUE = "e3b5744d-2ad1-4cdd-8390-c999a38829bc";

    @Autowired
    private InfraActiveRequestsRepository objUnderTest;

    @Test
    public void test_GetInfraActiveRequests_emptyFiltersMap() {
        final List<InfraActiveRequests> actualRequests = objUnderTest.getInfraActiveRequests(Collections.emptyMap(),
                START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, null);
        assertFalse(actualRequests.isEmpty());
    }

    @Test
    public void test_GetInfraActiveRequests_invalidFiltersMap() {
        final Map<String, String[]> filters = new HashMap<>();
        filters.put("OverTheMoon", new String[] {"Humpty Dumpty Sat On The Wall"});
        final long startTime = START_TIME_IN_MILISEC - TimeUnit.DAYS.toMillis(20);
        final long endTime = END_TIME_IN_MILISEC - TimeUnit.DAYS.toMillis(20);
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(filters, startTime, endTime, null);
        assertTrue(actualRequests.isEmpty());
    }

    @Test
    public void test_GetInfraActiveRequests_invalidFiltersMapWithInvalidKey() {
        final Map<String, String[]> filters = new HashMap<>();
        filters.put("OverTheMoon", new String[] {"Avengers", "Humpty Dumpty Sat On The Wall"});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(filters, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, null);
        assertFalse(actualRequests.isEmpty());
    }

    @Test
    public void test_GetInfraActiveRequestsData_withEqualServiceInstanceId() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(SERVICE_INSTANCE_ID, new String[] {QueryOperationType.EQ.name(), SERVICE_INSTANCE_ID_VALUE});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, 1);
        assertFalse(actualRequests.isEmpty());

        assertEquals(SERVICE_INSTANCE_ID_VALUE, actualRequests.get(0).getServiceInstanceId());
    }


    @Test
    public void test_GetInfraActiveRequestsData_withLikeRequestID() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(REQUEST_ID, new String[] {QueryOperationType.LIKE.name(), "00032ab7"});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, 1);
        assertFalse(actualRequests.isEmpty());

        assertEquals(REQUEST_ID_VALUE, actualRequests.get(0).getRequestId());
        assertEquals(SERVICE_INSTANCE_ID_VALUE, actualRequests.get(0).getServiceInstanceId());
    }


    @Test
    public void test_GetInfraActiveRequestsData_withLikeRequestIDAndEqualToServiceInstanceId() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(REQUEST_ID, new String[] {QueryOperationType.LIKE.name(), "00032ab7"});
        values.put(SERVICE_INSTANCE_ID, new String[] {QueryOperationType.EQ.name(), SERVICE_INSTANCE_ID_VALUE});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, 1);
        assertFalse(actualRequests.isEmpty());

        assertEquals(REQUEST_ID_VALUE, actualRequests.get(0).getRequestId());
        assertEquals(SERVICE_INSTANCE_ID_VALUE, actualRequests.get(0).getServiceInstanceId());
    }


    @Test
    public void test_GetInfraActiveRequestsData_withNotEqualAction() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(ACTION, new String[] {QueryOperationType.NEQ.name(), "createInstance"});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, null);
        assertFalse(actualRequests.isEmpty());
        for (final InfraActiveRequests actualActiveRequests : actualRequests) {
            assertNotEquals("createInstance", actualActiveRequests.getAction());
        }
    }


    @Test
    public void test_GetInfraActiveRequestsData_withNotEqualToServiceInstanceNameAndServiceInstanceIdNul() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(ACTION, new String[] {QueryOperationType.NEQ.name(), "createInstance"});
        values.put(SERVICE_INSTANCE_ID, new String[] {QueryOperationType.EQ.name(), SERVICE_INSTANCE_ID_VALUE});

        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, null);
        assertFalse(actualRequests.isEmpty());
        for (final InfraActiveRequests actualActiveRequests : actualRequests) {
            assertNotEquals("createInstance", actualActiveRequests.getAction());
            assertEquals(SERVICE_INSTANCE_ID_VALUE, actualActiveRequests.getServiceInstanceId());
        }
    }

    @Test
    public void test_GetInfraActiveRequestsData_withStartEndDateTimeNotEqualAction() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(ACTION, new String[] {QueryOperationType.NEQ.name(), "createInstance"});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, null);
        assertFalse(actualRequests.isEmpty());
        for (final InfraActiveRequests actualActiveRequests : actualRequests) {
            assertNotEquals("createInstance", actualActiveRequests.getAction());
        }
    }

    @Test
    public void test_GetInfraActiveRequestsData_withLimitNotEqualAction() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(ACTION, new String[] {QueryOperationType.NEQ.name(), "createInstance"});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, MAX_LIMIT);
        assertFalse(actualRequests.isEmpty());
        for (final InfraActiveRequests actualActiveRequests : actualRequests) {
            assertNotEquals("createInstance", actualActiveRequests.getAction());
        }
    }

    @Test
    public void test_GetInfraActiveRequestsData_withNullFilters() {
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(null, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, MAX_LIMIT);
        assertTrue(actualRequests.isEmpty());
    }
    
    @Test
    public void checkInstanceNameDuplicateNullInstanceNameTest(){
    	Map<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", "e05864f0-ab35-47d0-8be4-56fd9619ba3b");
    	InfraActiveRequests results = objUnderTest.checkInstanceNameDuplicate((HashMap<String, String>)instanceIdMap, null, "vnf");
    	assertNull(results);
    }
}
