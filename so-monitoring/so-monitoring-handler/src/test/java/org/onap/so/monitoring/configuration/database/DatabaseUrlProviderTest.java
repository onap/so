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
package org.onap.so.monitoring.configuration.database;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * @author waqas.ikram@ericsson.com
 */
public class DatabaseUrlProviderTest {

    private static final int MAX_RESULT = 1;
    private static final String URL = "http://localhost:8081/infraActiveRequests/";
    private final DatabaseUrlProvider objUnderTest = new DatabaseUrlProvider(URL);

    @Test
    public void test_maxResultNull() {
        final long from = System.currentTimeMillis();
        final long to = System.currentTimeMillis();
        final String actualUrl = objUnderTest.getSearchUrl(from, to, null);
        assertEquals(URL + "v1/getInfraActiveRequests?from=" + from + "&to=" + to, actualUrl);
    }

    @Test
    public void test_maxResultNotNull() {
        final long from = System.currentTimeMillis();
        final long to = System.currentTimeMillis();
        final String actualUrl = objUnderTest.getSearchUrl(from, to, MAX_RESULT);
        assertEquals(URL + "v1/getInfraActiveRequests?from=" + from + "&to=" + to + "&maxResult=" + MAX_RESULT,
                actualUrl);
    }
}
