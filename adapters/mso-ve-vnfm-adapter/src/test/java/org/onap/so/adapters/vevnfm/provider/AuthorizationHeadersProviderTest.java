/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.provider;

import static org.junit.Assert.*;
import static org.onap.so.configuration.rest.BasicHttpHeadersProvider.AUTHORIZATION_HEADER;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class AuthorizationHeadersProviderTest {

    private static final String AUTHORIZATION_EXAMPLE = "authorization";
    private static final String BLANK_EXAMPLE = "\t\n";
    private static final String EMPTY = "";

    private final AuthorizationHeadersProvider provider = new AuthorizationHeadersProvider();

    @Test
    public void testSuccessValidAuthorizationAndRemoval() {
        final HttpHeaders headers = provider.getHttpHeaders();
        final int size = headers.size();

        provider.addAuthorization(AUTHORIZATION_EXAMPLE);
        assertEquals(size + 1, headers.size());
        assertTrue(headers.containsKey(AUTHORIZATION_HEADER));

        provider.removeAuthorization();
        assertEquals(size, headers.size());
        assertFalse(headers.containsKey(AUTHORIZATION_HEADER));
    }

    @Test
    public void testBlankAuthorization() {
        final HttpHeaders headers = provider.getHttpHeaders();
        final int size = headers.size();

        provider.addAuthorization(BLANK_EXAMPLE);
        assertEquals(size, headers.size());
    }

    @Test
    public void testEmptyAuthorization() {
        final HttpHeaders headers = provider.getHttpHeaders();
        final int size = headers.size();

        provider.addAuthorization(EMPTY);
        assertEquals(size, headers.size());
    }

    @Test
    public void testNullAuthorization() {
        final HttpHeaders headers = provider.getHttpHeaders();
        final int size = headers.size();

        provider.addAuthorization(null);
        assertEquals(size, headers.size());
    }

    @Test
    public void testRemoveAuthorization() {
        final HttpHeaders headers = provider.getHttpHeaders();
        final int size = headers.size();

        provider.removeAuthorization();
        provider.removeAuthorization();
        assertEquals(size, headers.size());
    }
}
