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
package org.onap.so.montoring.db.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.montoring.db.service.DatabaseServiceProvider;
import org.onap.so.montoring.db.service.DatabaseServiceProviderImpl;
import org.onap.so.montoring.model.SoInfraRequest;

/**
 * @author waqas.ikram@ericsson.com
 */
public class DatabaseServiceProviderTest {

    @Test
    public void test_GetSoInfraRequest_WithEmptyFilters_EmptyList() {
        final InfraActiveRequestsRepository mockRepository = mock(InfraActiveRequestsRepository.class);
        Mockito.when(mockRepository.getInfraActiveRequests(Mockito.any())).thenReturn(Collections.emptyList());

        final DatabaseServiceProvider objUnderTest = new DatabaseServiceProviderImpl(mockRepository);
        assertTrue(objUnderTest.getSoInfraRequest(Collections.emptyMap()).isEmpty());
    }

    @Test
    public void test_GetSoInfraRequest_WithFilters_InfraActiveRequestsList() {
        final String requestID = UUID.randomUUID().toString();
        final String action = "action";
        final Map<String, String[]> filters = new HashMap<>();
        filters.put("requestId", new String[] {"EQ", requestID});

        final InfraActiveRequestsRepository mockRepository = mock(InfraActiveRequestsRepository.class);
        final List<InfraActiveRequests> asList = Arrays.asList(new InfraActiveRequests(requestID, action));
        Mockito.when(mockRepository.getInfraActiveRequests(filters)).thenReturn(asList);

        final DatabaseServiceProvider objUnderTest = new DatabaseServiceProviderImpl(mockRepository);
        List<SoInfraRequest> actualList = objUnderTest.getSoInfraRequest(filters);
        assertFalse(actualList.isEmpty());
        assertEquals(requestID, actualList.get(0).getRequestId());
    }
}
