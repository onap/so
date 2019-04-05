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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import com.google.gson.Gson;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.hamcrest.MockitoHamcrest;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.vim.model.AccessInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.ChangeTypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationComputeResource;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfIdentifierCreationNotification;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201Links;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201LinksSelf;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201VimConnectionInfo;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Sol003LcnControllerTest {

    private static final String CLOUD_OWNER = "myTestCloudOwner";
    private static final String REGION = "myTestRegion";
    private static final String TENANT_ID = "myTestTenantId";

    @LocalServerPort
    private int port;
    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;
    private MockRestServiceServer mockRestServer;

    @MockBean
    private AAIResourcesClient aaiResourcesClient;

    @Autowired
    private Sol003LcnContoller controller;
    private final Gson gson = new Gson();

    @Before
    public void setUp() throws Exception {
        mockRestServer = MockRestServiceServer.bindTo(testRestTemplate).build();
    }

    @Test
    public void lcnNotification_IdentifierCreated_Returns204() throws URISyntaxException, InterruptedException {
        final VnfIdentifierCreationNotification vnfIdentifierCreationNotification =
                new VnfIdentifierCreationNotification();
        final ResponseEntity<Void> response =
                controller.lcnVnfIdentifierCreationNotificationPost(vnfIdentifierCreationNotification);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void lcnNotification_IdentifierDeleted_Returns204() throws URISyntaxException, InterruptedException {
        final VnfIdentifierCreationNotification vnfIdentifierCreationNotification =
                new VnfIdentifierCreationNotification();
        final ResponseEntity<Void> response =
                controller.lcnVnfIdentifierCreationNotificationPost(vnfIdentifierCreationNotification);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void lcnNotification_InstantiateStartingOrProcessing_NoAction()
            throws URISyntaxException, InterruptedException {
        final VnfLcmOperationOccurrenceNotification startingNotification = new VnfLcmOperationOccurrenceNotification();
        startingNotification.setOperation(OperationEnum.INSTANTIATE);
        startingNotification.setOperationState(OperationStateEnum.STARTING);

        ResponseEntity<Void> response = controller.lcnVnfLcmOperationOccurrenceNotificationPost(startingNotification);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verifyZeroInteractions(aaiResourcesClient);

        final VnfLcmOperationOccurrenceNotification processingNotification =
                new VnfLcmOperationOccurrenceNotification();
        processingNotification.setOperation(OperationEnum.INSTANTIATE);
        processingNotification.setOperationState(OperationStateEnum.STARTING);

        response = controller.lcnVnfLcmOperationOccurrenceNotificationPost(processingNotification);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verifyZeroInteractions(aaiResourcesClient);
    }

    @Test
    public void lcnNotification_InstantiateCompleted_AaiUpdated() throws URISyntaxException, InterruptedException {
        final VnfLcmOperationOccurrenceNotification vnfLcmOperationOccurrenceNotification =
                createNotification(OperationEnum.INSTANTIATE);
        addVnfcsToNotification(vnfLcmOperationOccurrenceNotification, ChangeTypeEnum.ADDED);
        final InlineResponse201 vnfInstance = createVnfInstance();

        mockRestServer.expect(requestTo(new URI("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm")))
                .andRespond(withSuccess(gson.toJson(vnfInstance), MediaType.APPLICATION_JSON));

        final GenericVnf genericVnf = createGenericVnf("vnfmType1");
        final List<GenericVnf> genericVnfs = new ArrayList<>();
        genericVnfs.add(genericVnf);
        doReturn(Optional.of(genericVnfs)).when(aaiResourcesClient).get(eq(List.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher(
                        "/network/generic-vnfs?selflink=http%3A%2F%2Fvnfm%3A8080%2Fvnfs%2FmyTestVnfIdOnVnfm")));

        final ResponseEntity<Void> response =
                controller.lcnVnfLcmOperationOccurrenceNotificationPost(vnfLcmOperationOccurrenceNotification);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        final ArgumentCaptor<Object> bodyArgument = ArgumentCaptor.forClass(Object.class);
        final ArgumentCaptor<AAIResourceUri> uriArgument = ArgumentCaptor.forClass(AAIResourceUri.class);

        verify(aaiResourcesClient, timeout(1000).times(2)).update(uriArgument.capture(), bodyArgument.capture());

        assertEquals("/network/generic-vnfs/generic-vnf/myTestVnfId",
                uriArgument.getAllValues().get(0).build().toString());
        final GenericVnf updatedGenericVnf = (GenericVnf) bodyArgument.getAllValues().get(0);
        assertEquals("10.10.10.10", updatedGenericVnf.getIpv4OamAddress());
        assertEquals("Created", updatedGenericVnf.getOrchestrationStatus());

        assertEquals(
                "/cloud-infrastructure/cloud-regions/cloud-region/" + CLOUD_OWNER + "/" + REGION + "/tenants/tenant/"
                        + TENANT_ID + "/vservers/vserver/myVnfc1",
                uriArgument.getAllValues().get(1).build().toString());

        final Vserver vserver = (Vserver) bodyArgument.getAllValues().get(1);
        assertEquals("myVnfc1", vserver.getVserverId());
        final Relationship relationship = vserver.getRelationshipList().getRelationship().get(0);
        assertEquals("generic-vnf", relationship.getRelatedTo());
        assertEquals("tosca.relationships.HostedOn", relationship.getRelationshipLabel());
        assertEquals("/aai/v15/network/generic-vnfs/generic-vnf/myTestVnfId", relationship.getRelatedLink());
        assertEquals("generic-vnf.vnf-id", relationship.getRelationshipData().get(0).getRelationshipKey());
        assertEquals("myTestVnfId", relationship.getRelationshipData().get(0).getRelationshipValue());
    }

    @Test
    public void lcnNotification_TerminateCompleted_AaiUpdated() throws URISyntaxException, InterruptedException {
        final VnfLcmOperationOccurrenceNotification vnfLcmOperationOccurrenceNotification =
                createNotification(OperationEnum.TERMINATE);
        addVnfcsToNotification(vnfLcmOperationOccurrenceNotification, ChangeTypeEnum.REMOVED);

        final InlineResponse201 vnfInstance = createVnfInstance();

        mockRestServer.expect(requestTo(new URI("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm")))
                .andRespond(withSuccess(gson.toJson(vnfInstance), MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo(new URI("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm")))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON));

        final GenericVnf genericVnf = createGenericVnf("vnfmType1");
        genericVnf.setSelflink("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm");
        final List<GenericVnf> genericVnfs = new ArrayList<>();
        genericVnfs.add(genericVnf);
        doReturn(Optional.of(genericVnfs)).when(aaiResourcesClient).get(eq(List.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher(
                        "/network/generic-vnfs?selflink=http%3A%2F%2Fvnfm%3A8080%2Fvnfs%2FmyTestVnfIdOnVnfm")));

        final ResponseEntity<Void> response =
                controller.lcnVnfLcmOperationOccurrenceNotificationPost(vnfLcmOperationOccurrenceNotification);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        final ArgumentCaptor<GenericVnf> genericVnfArgument = ArgumentCaptor.forClass(GenericVnf.class);
        final ArgumentCaptor<AAIResourceUri> updateUriArgument = ArgumentCaptor.forClass(AAIResourceUri.class);
        verify(aaiResourcesClient, timeout(10000000)).update(updateUriArgument.capture(), genericVnfArgument.capture());
        assertEquals("/network/generic-vnfs/generic-vnf/myTestVnfId", updateUriArgument.getValue().build().toString());
        assertEquals("Assigned", genericVnfArgument.getValue().getOrchestrationStatus());

        final ArgumentCaptor<AAIResourceUri> deleteUriArgument = ArgumentCaptor.forClass(AAIResourceUri.class);

        verify(aaiResourcesClient, timeout(10000000)).delete(deleteUriArgument.capture());

        assertEquals(
                "/cloud-infrastructure/cloud-regions/cloud-region/" + CLOUD_OWNER + "/" + REGION + "/tenants/tenant/"
                        + TENANT_ID + "/vservers/vserver/myVnfc1",
                deleteUriArgument.getAllValues().get(0).build().toString());
    }

    private VnfLcmOperationOccurrenceNotification createNotification(final OperationEnum operation) {
        final VnfLcmOperationOccurrenceNotification notification = new VnfLcmOperationOccurrenceNotification();
        notification.setOperation(operation);
        notification.setOperationState(OperationStateEnum.COMPLETED);

        final LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance linkToVnfInstance =
                new LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance()
                        .href("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm");
        final LcnVnfLcmOperationOccurrenceNotificationLinks operationLinks =
                new LcnVnfLcmOperationOccurrenceNotificationLinks().vnfInstance(linkToVnfInstance);
        notification.setLinks(operationLinks);

        return notification;
    }

    private void addVnfcsToNotification(final VnfLcmOperationOccurrenceNotification notification,
            final ChangeTypeEnum changeType) {
        final List<LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs> affectedVnfcs = new ArrayList<>();;
        final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc =
                new LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs();
        vnfc.changeType(changeType);
        final LcnVnfLcmOperationOccurrenceNotificationComputeResource computeResource =
                new LcnVnfLcmOperationOccurrenceNotificationComputeResource();
        computeResource.setResourceId("myVnfc1");
        computeResource.setVimConnectionId(CLOUD_OWNER + "_" + REGION);
        vnfc.setComputeResource(computeResource);
        affectedVnfcs.add(vnfc);
        notification.setAffectedVnfcs(affectedVnfcs);
    }

    private InlineResponse201 createVnfInstance() {
        final InlineResponse201 vnfInstance = new InlineResponse201();
        final InlineResponse201LinksSelf selfLink = new InlineResponse201LinksSelf();
        selfLink.setHref("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm");
        final InlineResponse201Links VnfInstancelinks = new InlineResponse201Links();
        VnfInstancelinks.setSelf(selfLink);
        vnfInstance.setLinks(VnfInstancelinks);

        final Map<String, String> vnfConfigurableProperties = new HashMap<>();
        vnfConfigurableProperties.put("vnfIpAddress", "10.10.10.10");
        vnfInstance.setVnfConfigurableProperties(vnfConfigurableProperties);

        final List<InlineResponse201VimConnectionInfo> vimConnectionInfo = new ArrayList<>();;
        final InlineResponse201VimConnectionInfo vimConnection = new InlineResponse201VimConnectionInfo();
        vimConnection.setVimId(CLOUD_OWNER + "_" + REGION);
        vimConnection.setId(CLOUD_OWNER + "_" + REGION);
        final AccessInfo accessInfo = new AccessInfo();
        accessInfo.setProjectId(TENANT_ID);
        vimConnection.setAccessInfo(accessInfo);
        vimConnectionInfo.add(vimConnection);
        vnfInstance.setVimConnectionInfo(vimConnectionInfo);
        return vnfInstance;
    }

    private GenericVnf createGenericVnf(final String type) {
        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType(type);
        return genericVnf;
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
