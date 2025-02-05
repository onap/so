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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.onap.so.db.request.data.repository.InfraActiveRequestsRepositoryImpl.REQUEST_ID;
import static org.onap.so.db.request.data.repository.InfraActiveRequestsRepositoryImpl.SERVICE_INSTANCE_ID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class InfraActiveRequestsRepositoryImplTest {

    /**
     * January 1, 2019 2:00:00 PM
     */
    private static final long END_TIME = 1546351200000l;

    /**
     * January 1, 2019 12:45:00 PM
     */
    private static final long START_TIME = 1546346700000l;
    private static final int MAX_LIMIT = 1;

    /**
     * December 23, 2016 23:59 PM
     */
    private static final long END_TIME_IN_MILISEC = 1482580740000l;

    /**
     * December 21, 2016 01:00 AM
     */
    private static final long START_TIME_IN_MILISEC = 1482282000000l;
    private static final String REQUEST_ID_VALUE = "00032ab7-3fb3-42e5-965d-8ea592502017";
    private static final String SERVICE_INSTANCE_ID_VALUE = "e3b5744d-2ad1-4cdd-8390-c999a38829bc";

    @Autowired
    private InfraActiveRequestsRepositoryImpl objUnderTest;

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
        final List<Predicate> predicates = objUnderTest.getPredicates(filters, mock(CriteriaBuilder.class),
                (Root<InfraActiveRequests>) mock(Root.class));
        assertTrue(predicates.isEmpty());
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
    public void test_GetInfraActiveRequestsData_withNullFilters() {
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(null, START_TIME_IN_MILISEC, END_TIME_IN_MILISEC, MAX_LIMIT);
        assertTrue(actualRequests.isEmpty());
    }

    @Test
    public void checkInstanceNameDuplicateNullInstanceNameTest() {
        final Map<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", "e05864f0-ab35-47d0-8be4-56fd9619ba3b");
        final InfraActiveRequests results =
                objUnderTest.checkInstanceNameDuplicate((HashMap<String, String>) instanceIdMap, null, "vnf");
        assertNull(results);
    }

    @Test
    public void test_GetInfraActiveRequestsData_returnRecordWithNullEndTime() {
        final Map<String, String[]> values = new HashMap<>();
        values.put(SERVICE_INSTANCE_ID,
                new String[] {QueryOperationType.EQ.name(), "f7712652-b516-4925-a243-64550d26fd84"});
        final List<InfraActiveRequests> actualRequests =
                objUnderTest.getInfraActiveRequests(values, START_TIME, END_TIME, null);
        assertFalse(actualRequests.isEmpty());

        assertEquals(3, actualRequests.size());
        assertEquals("ShouldReturnInSearchQuery_1,ShouldReturnInSearchQuery_2,ShouldReturnInSearchQuery_3",
                actualRequests.stream().map(item -> item.getServiceInstanceName()).collect(Collectors.joining(",")));
    }

    @Test
    public void test_getOrchestrationFiltersFromInfraActive_between_dates() {
        Map<String, List<String>> orchestrationMap = new HashMap<>();
        orchestrationMap.put("startTime", Arrays.asList("BETWEEN_DATES", "12-01-2018", "12-31-2018"));
        List<InfraActiveRequests> result = objUnderTest.getOrchestrationFiltersFromInfraActive(orchestrationMap);

        assertEquals(1, result.size());
    }

}
