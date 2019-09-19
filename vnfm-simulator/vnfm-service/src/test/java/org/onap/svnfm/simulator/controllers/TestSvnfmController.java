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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.controller.SvnfmController;
import org.onap.svnfm.simulator.repository.VnfmCacheRepository;
import org.onap.svnfm.simulator.services.SvnfmService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class TestSvnfmController {

    private static final String VNF_INSTANCES_URL = "/vnf_instances/";
    private static final String VNFD_ID = "vnfdId";
    private static final String VNF_INSTANCE_NAME = "vnfInstanceName";
    private static final String VNF_INSTANCE_DESCRIPTION = "vnfInstanceDescription";
    private static final String VNF_INSTANCE_ID = "vnfInstanceId";
    private static final String VNF_OPERATION_ID = "vnfOperationId";
    private static final String VNF_INSTANTIATE_URL = "/instantiate";
    private static final String VNF_TERMINATE_URL = "/terminate";
    private static final String VNF_LCM_OP_OCC_ID = "vnfLcmOpOccId";
    private static final String VNF_OPERATION_STATUS_URL = "/vnf_lcm_op_occs/";
    private static final String SUBSCRIPTIONS_URL = "/subscriptions";

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

        createVnfRequest.setVnfdId(VNFD_ID);
        createVnfRequest.setVnfInstanceName(VNF_INSTANCE_NAME);
        createVnfRequest.setVnfInstanceDescription(VNF_INSTANCE_DESCRIPTION);

        when(vnfmCacheRepository.createVnf(eq(createVnfRequest), anyString())).thenReturn(new InlineResponse201());

        final String body = (new ObjectMapper()).valueToTree(createVnfRequest).toString();
        this.mockMvc
                .perform(post(Constant.BASE_URL + VNF_INSTANCES_URL).content(body)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void test_getVnf_usingValidVnfInstanceId_vnfInstanceRetrievedSuccessfully() throws Exception {
        final InlineResponse201 inlineResponse201 = new InlineResponse201();
        inlineResponse201.setId(VNF_INSTANCE_ID);
        inlineResponse201.setVnfInstanceName(VNF_INSTANCE_NAME);

        when(vnfmCacheRepository.getVnf(VNF_INSTANCE_ID)).thenReturn(inlineResponse201);

        final ResultActions resultActions = this.mockMvc
                .perform(get(Constant.BASE_URL + VNF_INSTANCES_URL + VNF_INSTANCE_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        final MvcResult result = resultActions.andReturn();
        final String content = result.getResponse().getContentAsString();
        final InlineResponse201 response201 = new ObjectMapper().readValue(content, InlineResponse201.class);
        assertThat(response201.getId()).isEqualTo(VNF_INSTANCE_ID);
        assertThat(response201.getVnfInstanceName()).isEqualTo(VNF_INSTANCE_NAME);
    }

    @Test
    public void test_instantiateVnf_usingValidVnfInstanceId_returnsHttpStatusAccepted() throws Exception {
        when(svnfmService.instantiateVnf(VNF_INSTANCE_ID)).thenReturn(VNF_OPERATION_ID);

        this.mockMvc
                .perform(post(Constant.BASE_URL + VNF_INSTANCES_URL + VNF_INSTANCE_ID + VNF_INSTANTIATE_URL)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void test_deleteVnf_usingValidVnfInstanceId_returnsHttpStatusNoContent() throws Exception {
        this.mockMvc
                .perform(delete(Constant.BASE_URL + VNF_INSTANCES_URL + VNF_INSTANCE_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void test_terminateVnf_usingValidVnfInstanceId_returnsHttpStatusIsAccepted() throws Exception {
        when(svnfmService.terminateVnf(VNF_INSTANCE_ID)).thenReturn(VNF_OPERATION_ID);

        this.mockMvc
                .perform(post(Constant.BASE_URL + VNF_INSTANCES_URL + VNF_INSTANCE_ID + VNF_TERMINATE_URL)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void test_getOperationStatus_usingValidOperationId_operationStatusRetrievedSuccessfully() throws Exception {
        final InlineResponse200 inlineResponse200 = new InlineResponse200();
        inlineResponse200.setId(VNF_LCM_OP_OCC_ID);
        inlineResponse200.setOperation(OperationEnum.INSTANTIATE);

        when(svnfmService.getOperationStatus(VNF_LCM_OP_OCC_ID)).thenReturn(inlineResponse200);

        final ResultActions resultActions = this.mockMvc
                .perform(get(Constant.BASE_URL + VNF_OPERATION_STATUS_URL + VNF_LCM_OP_OCC_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        final MvcResult result = resultActions.andReturn();
        final String content = result.getResponse().getContentAsString();
        final InlineResponse200 response200 = new ObjectMapper().readValue(content, InlineResponse200.class);
        assertThat(response200.getId()).isEqualTo(VNF_LCM_OP_OCC_ID);
        assertThat(response200.getOperation()).isEqualTo(OperationEnum.INSTANTIATE);
    }

    @Test
    public void test_subscribeForNotifications_usingValidSubscriptionRequest_returnsHttpStatusCreated()
            throws Exception {
        final LccnSubscriptionRequest lccnSubscriptionRequest = new LccnSubscriptionRequest();
        final String body = (new ObjectMapper()).valueToTree(lccnSubscriptionRequest).toString();

        this.mockMvc
                .perform(post(Constant.BASE_URL + SUBSCRIPTIONS_URL).content(body)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
