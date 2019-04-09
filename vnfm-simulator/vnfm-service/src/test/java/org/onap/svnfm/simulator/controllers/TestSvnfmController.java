/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.svnfm.simulator.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.svnfm.simulator.controller.SvnfmController;
import org.onap.svnfm.simulator.repository.VnfmCacheRepository;
import org.onap.svnfm.simulator.services.SvnfmService;
import org.onap.vnfm.v1.model.CreateVnfRequest;
import org.onap.vnfm.v1.model.InlineResponse201;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class TestSvnfmController {

    @InjectMocks
    private SvnfmController svnfmController;

    private MockMvc mockMvc;

    @Mock
    private SvnfmService svnfmService;

    @Mock
    private VnfmCacheRepository vnfmCacheRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(svnfmController).build();
    }

    @Test
    public void createVnfInstanceTest() throws Exception {
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();

        createVnfRequest.setVnfdId("123456798");
        createVnfRequest.setVnfInstanceName("createVnfInstanceTest");
        createVnfRequest.setVnfInstanceDescription("createVnfInstanceTest");

        when(vnfmCacheRepository.createVnf(createVnfRequest)).thenReturn(new InlineResponse201());

        svnfmService.createVnf(createVnfRequest);

        final String body = (new ObjectMapper()).valueToTree(createVnfRequest).toString();
        this.mockMvc
                .perform(post("/svnfm/vnf_instances").content(body).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}


