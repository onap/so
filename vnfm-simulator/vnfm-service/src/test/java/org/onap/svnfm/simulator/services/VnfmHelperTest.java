/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

package org.onap.svnfm.simulator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201.InstantiationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201LinksSelf;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.model.VnfInstance;

public class VnfmHelperTest {

    private static final String VNF_INSTANCE_ID = "vnfInstanceTestId";
    private static final String VNFD_ID = "vnfdTestId";
    private static final String VNF_INSTANCE_NAME = "vnfInsNameTest";
    private static final String VNF_INSTANCE_DESCRIPTION = "vnfInstTestDescr";
    private static final String APPLICATION_CONFIG_BASE_URL = "appConfUrl";
    private VnfmHelper testedObject;
    private ApplicationConfig applicationConfigMock;

    @Before
    public void setup() {
        applicationConfigMock = mock(ApplicationConfig.class);
        testedObject = new VnfmHelper(applicationConfigMock);
    }

    @Test
    public void createVnfInstance() {
        // when
        VnfInstance result = testedObject.createVnfInstance(createVnfRequest(), VNF_INSTANCE_ID);
        // then
        assertThat(result.getId()).isEqualTo(VNF_INSTANCE_ID);
        assertThat(result.getVnfdId()).isEqualTo(VNFD_ID);
        assertThat(result.getVnfInstanceName()).isEqualTo(VNF_INSTANCE_NAME);
        assertThat(result.getVnfInstanceDescription()).isEqualTo(VNF_INSTANCE_DESCRIPTION);
        assertThat(result.getVnfProvider()).isEqualTo(Constant.VNF_PROVIDER);
        assertThat(result.getVnfProductName()).isEqualTo(Constant.VNF_PROVIDER_NAME);
    }

    @Test
    public void getInlineResponse201() throws Exception {
        // given
        when(applicationConfigMock.getBaseUrl()).thenReturn(APPLICATION_CONFIG_BASE_URL);
        // when
        InlineResponse201 result = testedObject.getInlineResponse201(prepareVnfInstance());
        // then
        assertThat(result.getVnfdVersion()).isEqualTo(Constant.VNFD_VERSION);
        assertThat(result.getVnfSoftwareVersion()).isEqualTo(Constant.VNF_SOFTWARE_VERSION);
        assertThat(result.getInstantiationState()).isEqualByComparingTo(InstantiationStateEnum.NOT_INSTANTIATED);
        verifyAdditionalPropertyInlineResponse201(result);
    }

    private CreateVnfRequest createVnfRequest() {
        CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(VNFD_ID);
        createVnfRequest.setVnfInstanceName(VNF_INSTANCE_NAME);
        createVnfRequest.setVnfInstanceDescription(VNF_INSTANCE_DESCRIPTION);
        return createVnfRequest;
    }

    private VnfInstance prepareVnfInstance() {
        VnfInstance vnfInstance = new VnfInstance();
        vnfInstance.setId(VNF_INSTANCE_ID);
        return vnfInstance;
    }

    private void verifyAdditionalPropertyInlineResponse201(InlineResponse201 result) {
        InlineResponse201LinksSelf expectedVnfInstancesLinksSelf = new InlineResponse201LinksSelf();
        expectedVnfInstancesLinksSelf
                .setHref(APPLICATION_CONFIG_BASE_URL + "/vnflcm/v1/vnf_instances/" + VNF_INSTANCE_ID);
        assertThat(result.getLinks().getSelf()).isEqualTo(expectedVnfInstancesLinksSelf);

        InlineResponse201LinksSelf expectedVnfInstancesLinksSelfInstantiate = new InlineResponse201LinksSelf();
        expectedVnfInstancesLinksSelfInstantiate.setHref(
                (APPLICATION_CONFIG_BASE_URL + "/vnflcm/v1/vnf_instances/" + VNF_INSTANCE_ID + "/instantiate"));
        assertThat(result.getLinks().getInstantiate()).isEqualTo(expectedVnfInstancesLinksSelfInstantiate);
    }

}
