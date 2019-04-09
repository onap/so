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

package org.onap.so.adapters.vnfmadapter.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import java.util.Optional;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.hamcrest.MockitoHamcrest;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantRequest.OperationEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources.TypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201VimConnections;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Sol003GrantControllerTest {

    private static final String CLOUD_OWNER = "myTestCloudOwner";
    private static final String REGION = "myTestRegion";
    private static final String TENANT_ID = "myTestTenantId";
    private static final String SEPARATOR = "_";
    private static final String vimConnectionId = CLOUD_OWNER + SEPARATOR + REGION;

    @LocalServerPort
    private int port;
    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;
    private MockRestServiceServer mockRestServer;

    @MockBean
    private AAIResourcesClient aaiResourcesClient;

    @Autowired
    private Sol003GrantController controller;
    private final Gson gson = new Gson();

    @Before
    public void setUp() throws Exception {
        mockRestServer = MockRestServiceServer.bindTo(testRestTemplate).build();
        setUpVimInMockAai();
    }

    @Test
    public void grantRequest_ValidRequestInstantiate_GrantApproved() {
        GrantRequest grantRequest = createGrantRequest("INSTANTIATE");
        setUpGenericVnfWithVnfmRelationshipInMockAai("vnfmType", "vnfm1");
        final ResponseEntity<InlineResponse201> response = controller.grantsPost(grantRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getAddResources().size());

        assertEquals(vimConnectionId, response.getBody().getAddResources().get(0).getVimConnectionId());
        assertEquals("myTestVnfId", response.getBody().getVnfInstanceId());
        assertEquals("123456", response.getBody().getVnfLcmOpOccId());

        InlineResponse201VimConnections vimConnections = response.getBody().getVimConnections().get(0);
        assertEquals(vimConnectionId, vimConnections.getVimId());
        assertEquals("OPENSTACK", vimConnections.getVimType());
        assertNotNull(vimConnections.getAccessInfo());
        assertNotNull(vimConnections.getInterfaceInfo());
        assertEquals("INSTANTIATE", grantRequest.getOperation().toString());
    }

    @Test
    public void getGrant_notSupported_returns501() {
        final ResponseEntity<InlineResponse201> response2 = controller.grantsGrantIdGet("myTestGrantId");
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response2.getStatusCode());
    }

    @Test
    public void grantRequest_ValidRequestTerminate_GrantApproved() {
        GrantRequest grantRequest = createGrantRequest("TERMINATE");
        setUpGenericVnfWithVnfmRelationshipInMockAai("vnfmType", "vnfm1");
        final ResponseEntity<InlineResponse201> response = controller.grantsPost(grantRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getAddResources().size());
        assertEquals(vimConnectionId, response.getBody().getAddResources().get(0).getVimConnectionId());
        assertEquals("myTestVnfId", response.getBody().getVnfInstanceId());
        assertEquals("123456", response.getBody().getVnfLcmOpOccId());

        InlineResponse201VimConnections vimConnections = response.getBody().getVimConnections().get(0);
        assertEquals(vimConnectionId, vimConnections.getVimId());
        assertEquals("OPENSTACK", vimConnections.getVimType());
        assertNotNull(vimConnections.getAccessInfo());
        assertNotNull(vimConnections.getInterfaceInfo());
        assertEquals("TERMINATE", grantRequest.getOperation().toString());

    }

    private GrantRequest createGrantRequest(String operation) {
        GrantRequest grantRequest = new GrantRequest();
        grantRequest.setVnfInstanceId("myTestVnfId");
        grantRequest.setVnfLcmOpOccId("123456");
        if (operation == "INSTANTIATE") {
            grantRequest.setOperation(OperationEnum.INSTANTIATE);
            GrantsAddResources resource = new GrantsAddResources();
            resource.setId("123");
            resource.setType(TypeEnum.COMPUTE);
            grantRequest.addAddResourcesItem(resource);
        } else if (operation == "TERMINATE") {
            grantRequest.setOperation(OperationEnum.TERMINATE);
            GrantsAddResources resource = new GrantsAddResources();
            resource.setId("123");
            resource.setType(TypeEnum.COMPUTE);
            grantRequest.addRemoveResourcesItem(resource);
        }

        return grantRequest;
    }

    private void setUpVimInMockAai() {
        final EsrSystemInfo esrSystemInfo = new EsrSystemInfo();
        esrSystemInfo.setServiceUrl("http://myVim:8080");
        esrSystemInfo.setType("OPENSTACK");
        esrSystemInfo.setSystemType("VIM");
        esrSystemInfo.setCloudDomain("myDomain");
        esrSystemInfo.setUserName("myUser");
        esrSystemInfo.setPassword("myPassword");

        final EsrSystemInfoList esrSystemInfoList = new EsrSystemInfoList();
        esrSystemInfoList.getEsrSystemInfo().add(esrSystemInfo);

        doReturn(Optional.of(esrSystemInfoList)).when(aaiResourcesClient).get(eq(EsrSystemInfoList.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher("/cloud-infrastructure/cloud-regions/cloud-region/"
                        + CLOUD_OWNER + "/" + REGION + "/esr-system-info-list")));
    }

    private GenericVnf createGenericVnf(final String type) {
        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType(type);
        return genericVnf;
    }

    private void setUpGenericVnfWithVnfmRelationshipInMockAai(final String type, final String vnfmId) {
        final GenericVnf genericVnf = createGenericVnf(type);

        final Relationship relationshipToVnfm = new Relationship();
        relationshipToVnfm.setRelatedTo("tenant");
        final RelationshipData relationshipData1 = new RelationshipData();
        final RelationshipData relationshipData2 = new RelationshipData();
        final RelationshipData relationshipData3 = new RelationshipData();

        relationshipData1.setRelationshipKey("cloud-region.cloud-owner");
        relationshipData1.setRelationshipValue(CLOUD_OWNER);
        relationshipData2.setRelationshipKey("cloud-region.cloud-region-id");
        relationshipData2.setRelationshipValue(REGION);
        relationshipData3.setRelationshipKey("tenant.tenant-id");
        relationshipData3.setRelationshipValue(TENANT_ID);

        relationshipToVnfm.getRelationshipData().add(relationshipData1);
        relationshipToVnfm.getRelationshipData().add(relationshipData2);
        relationshipToVnfm.getRelationshipData().add(relationshipData3);

        final RelationshipList relationshipList = new RelationshipList();
        relationshipList.getRelationship().add(relationshipToVnfm);
        genericVnf.setRelationshipList(relationshipList);

        doReturn(Optional.of(genericVnf)).when(aaiResourcesClient).get(eq(GenericVnf.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher("/network/generic-vnfs/generic-vnf/myTestVnfId")));
    }

    private class AaiResourceUriMatcher extends BaseMatcher<AAIResourceUri> {

        final String uriAsString;

        public AaiResourceUriMatcher(final String uriAsString) {
            this.uriAsString = uriAsString;
        }

        @Override
        public boolean matches(final Object item) {
            if (item instanceof AAIResourceUri) {
                if (uriAsString.endsWith("...")) {
                    return ((AAIResourceUri) item).build().toString()
                            .startsWith(uriAsString.substring(0, uriAsString.indexOf("...")));
                }
                return ((AAIResourceUri) item).build().toString().equals(uriAsString);
            }
            return false;
        }

        @Override
        public void describeTo(final Description description) {}

    }

}
