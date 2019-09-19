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

package org.onap.svnfm.simulator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoResourceHandle;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoVnfcResourceInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.model.Vnfds.Vnfc;
import org.onap.svnfm.simulator.model.Vnfds.Vnfd;

public class TerminateOperationProgressorTest {

    private static final String VNFD_ID = "vnfdId";
    private static final String VNFC_TYPE = "COMPUTE";
    private static final String VDU_ID = "vduId";
    private static final String VNF_INSTANCE_ID = "vnfInstanceId";

    @Mock
    private SvnfmService svnfmService;

    private TerminateOperationProgressor testedObject;

    @Before
    public void setup() {
        svnfmService = mock(SvnfmService.class);
        VnfOperation vnfOperation = new VnfOperation();
        vnfOperation.setVnfInstanceId(VNF_INSTANCE_ID);
        vnfOperation.setOperation(OperationEnum.OPERATE);
        testedObject = new TerminateOperationProgressor(vnfOperation, svnfmService, null, new ApplicationConfig(),
                createVnfds(), createSubscriptionService());
    }

    @Test
    public void test_getAddResources_usingValidVnfdId_returnsEmptyList() {
        List<GrantsAddResources> results = testedObject.getAddResources(VNFD_ID);
        assertThat(results.isEmpty());
    }

    @Test
    public void test_getRemoveResources_usingValidVnfdId_retrievesGrantsAddResourcesSuccessfully() {
        // given
        InlineResponse201 inlineResponse201 = createInlineResponse201();
        InlineResponse201InstantiatedVnfInfo inlineResponse201InstantiatedVnfInfo =
                setupInlineResponseInstantiatedVnfInfo();
        inlineResponse201.setInstantiatedVnfInfo(inlineResponse201InstantiatedVnfInfo);

        when(svnfmService.getVnf(VNF_INSTANCE_ID)).thenReturn(inlineResponse201);

        // when
        List<GrantsAddResources> result = testedObject.getRemoveResources(VNFD_ID);

        // then
        assertThat(result).hasSize(1);
        GrantsAddResources grantsAddResourcesResult = result.get(0);
        assertThat(grantsAddResourcesResult.getType()).hasToString(VNFC_TYPE);
        assertThat(grantsAddResourcesResult.getVduId()).isEqualTo(VDU_ID);
    }

    @Test
    public void test_getVnfcChangeType_isEnumRemoved() {
        assertThat(testedObject.getVnfcChangeType().getValue()).isEqualTo("REMOVED");
    }

    private Vnfds createVnfds() {
        Vnfd vnfd = new Vnfd();
        vnfd.setVnfdId(VNFD_ID);
        List<Vnfc> vnfcList = new ArrayList<>();
        vnfcList.add(createVnfc());
        vnfd.setVnfcList(vnfcList);
        List<Vnfd> vnfdList = new ArrayList<>();
        vnfdList.add(vnfd);

        Vnfds vnfds = new Vnfds();
        vnfds.setVnfdList(vnfdList);
        return vnfds;
    }

    private Vnfc createVnfc() {
        Vnfc vnfc = new Vnfc();
        String vnfcId = "vnfcId";
        vnfc.setVnfcId(vnfcId);
        vnfc.setType(VNFC_TYPE);
        String resourceTemplateId = "resTempId";
        vnfc.setResourceTemplateId(resourceTemplateId);
        vnfc.setVduId(VDU_ID);
        String resourceDefinitionId = "resourceDefinitionId";
        vnfc.setGrantResourceId(resourceDefinitionId);
        return vnfc;
    }

    private SubscriptionService createSubscriptionService() {
        SubscriptionService subscriptionService = new SubscriptionService();
        LccnSubscriptionRequest lccnSubscriptionRequest = new LccnSubscriptionRequest();
        String callbackUri = "/lcn/uriest";
        lccnSubscriptionRequest.setCallbackUri(callbackUri);
        subscriptionService.registerSubscription(lccnSubscriptionRequest);
        return subscriptionService;
    }

    private InlineResponse201 createInlineResponse201() {
        InlineResponse201 inlineResponse201 = new InlineResponse201();
        inlineResponse201.setVnfdId(VNFD_ID);
        return inlineResponse201;
    }

    private InlineResponse201InstantiatedVnfInfo setupInlineResponseInstantiatedVnfInfo() {
        InlineResponse201InstantiatedVnfInfo inlineResponse201InstantiatedVnfInfo =
                new InlineResponse201InstantiatedVnfInfo();
        List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> resultList = new ArrayList<>();
        InlineResponse201InstantiatedVnfInfoVnfcResourceInfo resourceInfo =
                new InlineResponse201InstantiatedVnfInfoVnfcResourceInfo();
        String resourceInfoId = "resourceInfoId";
        resourceInfo.setId(resourceInfoId);
        resourceInfo.setVduId((VDU_ID));
        InlineResponse201InstantiatedVnfInfoResourceHandle resourceHandle =
                new InlineResponse201InstantiatedVnfInfoResourceHandle();
        resourceInfo.setComputeResource(resourceHandle);
        resultList.add(resourceInfo);
        inlineResponse201InstantiatedVnfInfo.setVnfcResourceInfo(resultList);
        String flavourId = "flavourId";
        inlineResponse201InstantiatedVnfInfo.setFlavourId(flavourId);
        return inlineResponse201InstantiatedVnfInfo;
    }
}
