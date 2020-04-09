/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.so.adapters.vevnfm.configuration.StartupConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles(StartupConfiguration.TEST_PROFILE)
public class NotificationControllerTest {

    private static final String MINIMAL_JSON_CONTENT = "{}";
    private static final int ZERO = 0;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RestTemplate restTemplate;

    private String notification;
    private MockMvc mvc;
    private MockRestServiceServer mockRestServer;

    @Before
    public void init() {
        notification = configProperties.getVnfmNotification();
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void testReceiveNotification() throws Exception {
        // given
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(notification)
                .contentType(MediaType.APPLICATION_JSON).content(MINIMAL_JSON_CONTENT);

        mockRestServer.expect(anything()).andRespond(withSuccess());

        // when
        final MvcResult mvcResult = mvc.perform(request).andReturn();

        // then
        final MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(ZERO, response.getContentLength());
        mockRestServer.verify();
    }
}
