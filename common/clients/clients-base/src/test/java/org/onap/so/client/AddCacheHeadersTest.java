/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.so.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class AddCacheHeadersTest {

    @Mock
    ClientRequestContext request;

    @Mock
    ClientResponseContext response;

    private final AddCacheHeaders addCacheHeaders = new AddCacheHeaders(new CachePropertiesImpl());

    @Test
    @SneakyThrows
    public void thatCacheHeaderIsAddedForGet() {
        MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
        when(request.getMethod()).thenReturn("GET");
        when(response.getHeaders()).thenReturn(headers);

        addCacheHeaders.filter(request, response);
        assertTrue(headers.containsKey("Cache-Control"));
        assertEquals(Collections.singletonList("public, max-age=" + (new CachePropertiesImpl().getMaxAge() / 1000)),
                headers.get("Cache-Control"));
    }

    @Test
    @SneakyThrows
    public void thatCacheHeaderIsNotOverwrittenForGet() {
        MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
        List<String> expected = Collections.singletonList("foo");
        headers.put("Cache-Control", expected);
        when(request.getMethod()).thenReturn("GET");
        when(response.getHeaders()).thenReturn(headers);

        addCacheHeaders.filter(request, response);
        assertTrue(headers.containsKey("Cache-Control"));
        assertEquals(expected, headers.get("Cache-Control"));
    }

    @Test
    @SneakyThrows
    public void thatCacheHeaderIsNotAddedForOtherMethods() {
        MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
        when(request.getMethod()).thenReturn("PUT");

        addCacheHeaders.filter(request, response);
        assertFalse(headers.containsKey("Cache-Control"));
    }

    class CachePropertiesImpl implements CacheProperties {

        @Override
        public String getCacheName() {
            return "test-cache";
        }

        @Override
        public Long getMaxAge() {
            return 5 * 60 * 1000L;
        }

    }
}
