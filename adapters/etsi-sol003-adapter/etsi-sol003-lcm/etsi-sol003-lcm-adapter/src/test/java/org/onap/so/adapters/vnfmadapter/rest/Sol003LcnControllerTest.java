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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.hamcrest.MockitoHamcrest;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.adapters.vnfmadapter.extclients.aai.AaiHelper;
import org.onap.so.adapters.vnfmadapter.extclients.aai.OamIpAddressSource;
import org.onap.so.adapters.vnfmadapter.extclients.aai.OamIpAddressSource.OamIpAddressType;
import org.onap.so.adapters.vnfmadapter.extclients.vim.model.AccessInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201Links;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201LinksSelf;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201VimConnectionInfo;
import org.onap.so.adapters.vnfmadapter.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs;
import org.onap.so.adapters.vnfmadapter.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.ChangeTypeEnum;
import org.onap.so.adapters.vnfmadapter.lcn.model.LcnVnfLcmOperationOccurrenceNotificationComputeResource;
import org.onap.so.adapters.vnfmadapter.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinks;
import org.onap.so.adapters.vnfmadapter.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance;
import org.onap.so.adapters.vnfmadapter.lcn.model.VnfIdentifierCreationNotification;
import org.onap.so.adapters.vnfmadapter.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vnfmadapter.lcn.model.VnfLcmOperationOccurrenceNotification.OperationEnum;
import org.onap.so.adapters.vnfmadapter.lcn.model.VnfLcmOperationOccurrenceNotification.OperationStateEnum;
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
import com.google.gson.Gson;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
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

    @Inject
    private AaiHelper aaiHelper;

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
        addRelationshipFromGenericVnfToVnfm(genericVnf, "vnfm1");
        final List<GenericVnf> listOfGenericVnfs = new ArrayList<>();
        listOfGenericVnfs.add(genericVnf);
        final GenericVnfs genericVnfs = new GenericVnfs();
        genericVnfs.getGenericVnf().addAll(listOfGenericVnfs);
        doReturn(Optional.of(genericVnfs)).when(aaiResourcesClient).get(eq(GenericVnfs.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher(
                        "/network/generic-vnfs?selflink=http%3A%2F%2Fvnfm%3A8080%2Fvnfs%2FmyTestVnfIdOnVnfm")));
        EsrVnfm vnfm = new EsrVnfm();
        vnfm.setVnfmId("vnfm1");
        final EsrSystemInfoList esrSystemInfoList = new EsrSystemInfoList();
        vnfm.setEsrSystemInfoList(esrSystemInfoList);
        doReturn(Optional.of(vnfm)).when(aaiResourcesClient).get(eq(EsrVnfm.class), MockitoHamcrest
                .argThat(new AaiResourceUriMatcher("/external-system/esr-vnfm-list/esr-vnfm/vnfm1?depth=1")));

        final ResponseEntity<Void> response =
                controller.lcnVnfLcmOperationOccurrenceNotificationPost(vnfLcmOperationOccurrenceNotification);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        final ArgumentCaptor<Object> bodyArgument1 = ArgumentCaptor.forClass(Object.class);
        final ArgumentCaptor<AAIResourceUri> uriArgument1 = ArgumentCaptor.forClass(AAIResourceUri.class);

        verify(aaiResourcesClient, timeout(1000)).update(uriArgument1.capture(), bodyArgument1.capture());

        assertEquals("/network/generic-vnfs/generic-vnf/myTestVnfId",
                uriArgument1.getAllValues().get(0).build().toString());
        final GenericVnf updatedGenericVnf = (GenericVnf) bodyArgument1.getAllValues().get(0);
        assertEquals("10.10.10.10", updatedGenericVnf.getIpv4OamAddress());
        assertEquals("Created", updatedGenericVnf.getOrchestrationStatus());

        final ArgumentCaptor<Object> bodyArgument2 = ArgumentCaptor.forClass(Object.class);
        final ArgumentCaptor<AAIResourceUri> uriArgument2 = ArgumentCaptor.forClass(AAIResourceUri.class);
        verify(aaiResourcesClient, timeout(1000)).create(uriArgument2.capture(), bodyArgument2.capture());

        assertEquals(
                "/cloud-infrastructure/cloud-regions/cloud-region/" + CLOUD_OWNER + "/" + REGION + "/tenants/tenant/"
                        + TENANT_ID + "/vservers/vserver/myVnfc1",
                uriArgument2.getAllValues().get(0).build().toString());

        final Vserver vserver = (Vserver) bodyArgument2.getAllValues().get(0);
        assertEquals("myVnfc1", vserver.getVserverId());

        final ArgumentCaptor<AAIResourceUri> uriArgument1Connect = ArgumentCaptor.forClass(AAIResourceUri.class);
        final ArgumentCaptor<AAIResourceUri> uriArgument2Connect = ArgumentCaptor.forClass(AAIResourceUri.class);
        verify(aaiResourcesClient, timeout(1000)).connect(uriArgument1Connect.capture(), uriArgument2Connect.capture());
        assertEquals(
                "/cloud-infrastructure/cloud-regions/cloud-region/" + CLOUD_OWNER + "/" + REGION + "/tenants/tenant/"
                        + TENANT_ID + "/vservers/vserver/myVnfc1",
                uriArgument1Connect.getAllValues().get(0).build().toString());
        assertEquals("/network/generic-vnfs/generic-vnf/myTestVnfId",
                uriArgument2Connect.getAllValues().get(0).build().toString());
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
                .andRespond(withStatus(HttpStatus.NO_CONTENT).contentType(MediaType.APPLICATION_JSON));

        final GenericVnf genericVnf = createGenericVnf("vnfmType1");
        addRelationshipFromGenericVnfToVnfm(genericVnf, "vnfm1");
        genericVnf.setSelflink("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm");
        final List<GenericVnf> listOfGenericVnfs = new ArrayList<>();
        listOfGenericVnfs.add(genericVnf);
        final GenericVnfs genericVnfs = new GenericVnfs();
        genericVnfs.getGenericVnf().addAll(listOfGenericVnfs);
        addRelationshipFromGenericVnfToVserver(genericVnf, "myVnfc1");

        doReturn(Optional.of(genericVnfs)).when(aaiResourcesClient).get(eq(GenericVnfs.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher(
                        "/network/generic-vnfs?selflink=http%3A%2F%2Fvnfm%3A8080%2Fvnfs%2FmyTestVnfIdOnVnfm")));
        EsrVnfm vnfm = new EsrVnfm();
        vnfm.setVnfmId("vnfm1");
        final EsrSystemInfoList esrSystemInfoList = new EsrSystemInfoList();
        vnfm.setEsrSystemInfoList(esrSystemInfoList);
        doReturn(Optional.of(vnfm)).when(aaiResourcesClient).get(eq(EsrVnfm.class), MockitoHamcrest
                .argThat(new AaiResourceUriMatcher("/external-system/esr-vnfm-list/esr-vnfm/vnfm1?depth=1")));

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
        vnfInstance.setId("myTestVnfIdOnVnfm");
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

        final OamIpAddressSource oamIpAddressSource =
                new OamIpAddressSource(OamIpAddressType.CONFIGURABLE_PROPERTY, "vnfIpAddress");
        aaiHelper.setOamIpAddressSource("myTestVnfIdOnVnfm", oamIpAddressSource);
        return vnfInstance;
    }

    private GenericVnf createGenericVnf(final String type) {
        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType(type);
        return genericVnf;
    }

    private void addRelationshipFromGenericVnfToVnfm(final GenericVnf genericVnf, final String vnfmId) {
        final Relationship relationshipToVnfm = new Relationship();
        relationshipToVnfm.setRelatedLink("/aai/v15/external-system/esr-vnfm-list/esr-vnfm/" + vnfmId);
        relationshipToVnfm.setRelatedTo("esr-vnfm");
        final RelationshipData relationshipData = new RelationshipData();
        relationshipData.setRelationshipKey("esr-vnfm.vnfm-id");
        relationshipData.setRelationshipValue(vnfmId);
        relationshipToVnfm.getRelationshipData().add(relationshipData);

        if (genericVnf.getRelationshipList() == null) {
            final RelationshipList relationshipList = new RelationshipList();
            genericVnf.setRelationshipList(relationshipList);
        }
        genericVnf.getRelationshipList().getRelationship().add(relationshipToVnfm);
    }

    private void addRelationshipFromGenericVnfToVserver(final GenericVnf genericVnf, final String vserverId) {
        final Relationship relationshipToVserver = new Relationship();
        relationshipToVserver.setRelatedTo("vserver");
        final RelationshipData relationshipData1 = new RelationshipData();
        relationshipData1.setRelationshipKey("vserver.vserver-id");
        relationshipData1.setRelationshipValue(vserverId);
        relationshipToVserver.getRelationshipData().add(relationshipData1);
        final RelationshipData relationshipData2 = new RelationshipData();
        relationshipData2.setRelationshipKey("cloud-region.cloud-owner");
        relationshipData2.setRelationshipValue(CLOUD_OWNER);
        relationshipToVserver.getRelationshipData().add(relationshipData2);
        final RelationshipData relationshipData3 = new RelationshipData();
        relationshipData3.setRelationshipKey("cloud-region.cloud-region-id");
        relationshipData3.setRelationshipValue(REGION);
        relationshipToVserver.getRelationshipData().add(relationshipData3);
        final RelationshipData relationshipData4 = new RelationshipData();
        relationshipData4.setRelationshipKey("tenant.tenant-id");
        relationshipData4.setRelationshipValue(TENANT_ID);
        relationshipToVserver.getRelationshipData().add(relationshipData4);

        if (genericVnf.getRelationshipList() == null) {
            final RelationshipList relationshipList = new RelationshipList();
            genericVnf.setRelationshipList(relationshipList);
        }
        genericVnf.getRelationshipList().getRelationship().add(relationshipToVserver);
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
