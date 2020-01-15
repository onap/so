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

import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vevnfm.configuration.StartupConfiguration;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles(StartupConfiguration.TEST_PROFILE)
public class SubscribeSenderTest {

    private static final String SLASH = "/";
    private static final String MINIMAL_JSON_CONTENT = "{}";

    private static final Gson GSON;

    static {
        final GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        GSON = builder.create();
    }

    @Value("${vnfm.subscription}")
    private String vnfmSubscription;

    @Autowired
    private SubscribeSender sender;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockRestServer;

    @Before
    public void init() {
        mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void test() throws Exception {
        // given
        final String endpoint = "lh";
        final LccnSubscriptionRequest request = new LccnSubscriptionRequest();

        mockRestServer.expect(once(), requestTo(SLASH + endpoint + vnfmSubscription))
                .andExpect(header(CONTENT_TYPE, CoreMatchers.containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(method(HttpMethod.POST)).andExpect(content().json(GSON.toJson(request)))
                .andRespond(withStatus(HttpStatus.CREATED).body(MINIMAL_JSON_CONTENT));

        // when
        final boolean done = sender.send(endpoint, request);

        // then
        assertTrue(done);
        mockRestServer.verify();
    }
}
