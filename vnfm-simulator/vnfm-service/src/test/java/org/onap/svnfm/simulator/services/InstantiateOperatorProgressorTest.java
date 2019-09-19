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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201AddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201VimConnections;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoVnfcResourceInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.model.Vnfds.Vnfc;
import org.onap.svnfm.simulator.model.Vnfds.Vnfd;

public class InstantiateOperatorProgressorTest {

    private static final String VNF_ID = "vnfTestId";
    private static final String CALLBACK_URI = "/lcn/uritest";
    private static final String VNFC_TYPE = "COMPUTE";
    private static final String RESOURCE_TEMPLATE_ID = "resTempIdTest";
    private static final String VDU_ID = "vduIdTest";
    private static final String VNF_INSTANCE_ID = "vnfInstanceId";
    private static final String VNFC_ID = "vnfcIdTest";
    private static final String RESOURCE_DEFINITION_ID = "resDefTestId";
    private static final String VIM_CONNECTION_ID = "vimConnTestId";

    private SvnfmService svnfmServiceMock;

    private InstantiateOperationProgressor testedObject;

    @Before
    public void setup() {
        svnfmServiceMock = mock(SvnfmService.class);
        VnfOperation vnfOperation = new VnfOperation();
        vnfOperation.setVnfInstanceId(VNF_INSTANCE_ID);
        vnfOperation.setOperation(OperationEnum.OPERATE);
        testedObject = new InstantiateOperationProgressor(vnfOperation, svnfmServiceMock, null, new ApplicationConfig(),
                createVnfds(), createSubscriptionService());
    }

    @Test
    public void getAddResources_vnfIdFound() {
        List<GrantsAddResources> result = testedObject.getAddResources(VNF_ID);
        assertThat(result).hasSize(1);
        GrantsAddResources grantsAddResourceResult = result.get(0);
        assertThat(grantsAddResourceResult.getType()).hasToString(VNFC_TYPE);
        assertThat(grantsAddResourceResult.getResourceTemplateId()).isEqualTo(RESOURCE_TEMPLATE_ID);
        assertThat(grantsAddResourceResult.getVduId()).isEqualTo(VDU_ID);
    }

    @Test
    public void getAddResources_vnfIdNotFound() {
        List<GrantsAddResources> result = testedObject.getAddResources("otherVnfId");
        assertThat(result).isEmpty();
    }

    @Test
    public void handleGrantResponse_VnfdObjectsAvailable() {
        when(svnfmServiceMock.getVnf(VNF_INSTANCE_ID)).thenReturn(createInlineResponse201());

        InlineResponse201VimConnections inlineResponse201VimConnections = new InlineResponse201VimConnections();
        List<InlineResponse201VimConnections> listOfVimConnection = new ArrayList<>();
        listOfVimConnection.add(inlineResponse201VimConnections);

        InlineResponse201AddResources inlineResponse201AddResources = new InlineResponse201AddResources();
        inlineResponse201AddResources.setResourceDefinitionId(RESOURCE_DEFINITION_ID);
        inlineResponse201AddResources.setVimConnectionId(VIM_CONNECTION_ID);
        List<InlineResponse201AddResources> listOfResources = new ArrayList<>();
        listOfResources.add(inlineResponse201AddResources);

        InlineResponse201 inlineResponse201 = new InlineResponse201();
        inlineResponse201.setVimConnections(listOfVimConnection);
        inlineResponse201.setAddResources(listOfResources);
        List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> resultList =
                testedObject.handleGrantResponse(inlineResponse201);

        assertThat(resultList).hasSize(1);
        InlineResponse201InstantiatedVnfInfoVnfcResourceInfo resultObject = resultList.get(0);
        assertThat(resultObject.getId()).isEqualTo(VNFC_ID);
        assertThat(resultObject.getVduId()).isEqualTo(VDU_ID);
        assertThat(resultObject.getComputeResource().getVimConnectionId()).isEqualTo(VIM_CONNECTION_ID);
    }

    @Test
    public void getVnfcChangeType_enumAdded() {
        assertThat(testedObject.getVnfcChangeType().getValue()).isEqualTo("ADDED");
    }

    @Test
    public void getRemoveResourcesShouldReturnEmptyList() {
        assertThat(testedObject.getRemoveResources("anyVnfId")).isEmpty();
    }

    @Test
    public void test_buildGrantRequest_usingValidVnfInstanceId_grantRequestRetrievedSuccessfully() {
        when(svnfmServiceMock.getVnf(Mockito.eq(VNF_INSTANCE_ID))).thenReturn(createInlineResponse201());
        GrantRequest grantRequest = testedObject.buildGrantRequest();
        assertThat(grantRequest.getVnfdId().equals(VNF_ID));
    }

    private Vnfds createVnfds() {
        Vnfd vnfd = new Vnfd();
        vnfd.setVnfdId(VNF_ID);
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
        vnfc.setVnfcId(VNFC_ID);
        vnfc.setType(VNFC_TYPE);
        vnfc.setResourceTemplateId(RESOURCE_TEMPLATE_ID);
        vnfc.setVduId(VDU_ID);
        vnfc.setGrantResourceId(RESOURCE_DEFINITION_ID);
        return vnfc;
    }

    private SubscriptionService createSubscriptionService() {
        SubscriptionService subscriptionService = new SubscriptionService();
        LccnSubscriptionRequest lccnSubscriptionRequest = new LccnSubscriptionRequest();
        lccnSubscriptionRequest.setCallbackUri(CALLBACK_URI);
        subscriptionService.registerSubscription(lccnSubscriptionRequest);
        return subscriptionService;
    }

    private org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201 createInlineResponse201() {
        org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201 inlineResponse201 =
                new org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201();
        inlineResponse201.setVnfdId(VNF_ID);
        return inlineResponse201;
    }
}
