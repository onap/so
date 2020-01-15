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

package org.onap.so.adapters.vevnfm.subscription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubscribeSenderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SubscribeSender sender;

    @Test
    public void testSuccess() {
        final ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED).body("{}");
        assertTrue(testingSend(response));
    }

    @Test
    public void testFailure() {
        final ResponseEntity<String> response = new ResponseEntity(HttpStatus.BAD_REQUEST);
        assertFalse(testingSend(response));
    }

    private boolean testingSend(final ResponseEntity<String> response) {
        // given
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        final LccnSubscriptionRequest request = new LccnSubscriptionRequest();
        final Map vnfm = new LinkedHashMap<String, String>();
        vnfm.put(SubscribeRunner.URL, "lh");

        // when
        final boolean done = sender.send(vnfm, request);

        // then
        verify(restTemplate, times(1))
                .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        return done;
    }
}
