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
package org.onap.so.monitoring.db.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.onap.so.monitoring.camunda.model.SoActiveInfraRequests;
import org.onap.so.monitoring.configuration.database.DatabaseUrlProvider;
import org.onap.so.monitoring.db.service.DatabaseServiceProvider;
import org.onap.so.monitoring.db.service.DatabaseServiceProviderImpl;
import org.onap.so.monitoring.model.SoInfraRequest;
import org.onap.so.monitoring.rest.service.HttpRestServiceProvider;

import com.google.common.base.Optional;


/**
 * @author waqas.ikram@ericsson.com
 */
public class DatabaseServiceProviderTest {

    private final static DatabaseUrlProvider URL_PROVIDER =
            new DatabaseUrlProvider("http://localhost:8081/infraActiveRequests/");

    @Test
    public void test_GetSoInfraRequest_WithEmptyFilters_EmptyList() {
        final HttpRestServiceProvider mockServiceProvider = mock(HttpRestServiceProvider.class);
        final String searchUrl = URL_PROVIDER.getSearchUrl(0, 0, null);
        final Optional<SoActiveInfraRequests[]> response = Optional.of(new SoActiveInfraRequests[] {});

        when(mockServiceProvider.postHttpRequest(eq(Collections.emptyMap()), eq(searchUrl),
                eq(SoActiveInfraRequests[].class))).thenReturn(response);

        final DatabaseServiceProvider objUnderTest = new DatabaseServiceProviderImpl(URL_PROVIDER, mockServiceProvider);

        assertTrue(objUnderTest.getSoInfraRequest(Collections.emptyMap(), 0, 0, null).isEmpty());
    }

    @Test
    public void test_GetSoInfraRequest_OptionalAbsent_EmptyList() {
        final HttpRestServiceProvider mockServiceProvider = mock(HttpRestServiceProvider.class);
        final String searchUrl = URL_PROVIDER.getSearchUrl(0, 0, null);
        final Optional<SoActiveInfraRequests[]> response = Optional.absent();

        when(mockServiceProvider.postHttpRequest(eq(Collections.emptyMap()), eq(searchUrl),
                eq(SoActiveInfraRequests[].class))).thenReturn(response);

        final DatabaseServiceProvider objUnderTest = new DatabaseServiceProviderImpl(URL_PROVIDER, mockServiceProvider);

        assertTrue(objUnderTest.getSoInfraRequest(Collections.emptyMap(), 0, 0, null).isEmpty());
    }


    @Test
    public void test_GetSoInfraRequest_WithFilters_InfraActiveRequestsList() {
        final String searchUrl = URL_PROVIDER.getSearchUrl(0, 0, null);
        final String requestID = UUID.randomUUID().toString();
        final Map<String, String[]> filters = new HashMap<>();
        filters.put("requestId", new String[] {"EQ", requestID});

        SoActiveInfraRequests soActiveInfraRequests = new SoActiveInfraRequests();
        soActiveInfraRequests.setRequestId(requestID);

        final Optional<SoActiveInfraRequests[]> response =
                Optional.of(new SoActiveInfraRequests[] {soActiveInfraRequests});

        final HttpRestServiceProvider mockServiceProvider = mock(HttpRestServiceProvider.class);

        when(mockServiceProvider.postHttpRequest(eq(filters), eq(searchUrl), eq(SoActiveInfraRequests[].class)))
                .thenReturn(response);

        final DatabaseServiceProvider objUnderTest = new DatabaseServiceProviderImpl(URL_PROVIDER, mockServiceProvider);

        final List<SoInfraRequest> actualList = objUnderTest.getSoInfraRequest(filters, 0, 0, null);
        assertFalse(actualList.isEmpty());
        assertEquals(requestID, actualList.get(0).getRequestId());

    }
}
