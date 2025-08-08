/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.orchestration;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.aaiclient.client.aai.AAIValidatorImpl;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIVnfResourcesTest extends TestDataSetup {

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";

    private GenericVnf genericVnf;

    private ServiceInstance serviceInstance;

    private CloudRegion cloudRegion;

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    @Mock
    protected AAIValidatorImpl MOCK_aaiValidatorImpl;

    @InjectMocks
    AAIVnfResources aaiVnfResources = new AAIVnfResources();

    @Before
    public void before() {
        serviceInstance = buildServiceInstance();
        genericVnf = buildGenericVnf();
        cloudRegion = buildCloudRegion();
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }

    @Test
    public void createVnfandConnectServiceInstanceTest() {
        doReturn(new org.onap.aai.domain.yang.GenericVnf()).when(MOCK_aaiObjectMapper).mapVnf(genericVnf);
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                any(Optional.class));
        doNothing().when(MOCK_aaiResourcesClient).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
        genericVnf.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

        aaiVnfResources.createVnfandConnectServiceInstance(genericVnf, serviceInstance);

        assertEquals(OrchestrationStatus.INVENTORIED, genericVnf.getOrchestrationStatus());
        verify(MOCK_aaiObjectMapper, times(1)).mapVnf(genericVnf);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void createPlatformandConnectVnfTest() {
        Platform platform = new Platform();
        platform.setPlatformName("a123");
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                any(Optional.class));
        aaiVnfResources.createPlatformandConnectVnf(platform, genericVnf);
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), isA(AAIResourceUri.class));
    }

    @Test
    public void createLineOfBusinessandConnectVnfTest() {
        LineOfBusiness lob = new LineOfBusiness();
        lob.setLineOfBusinessName("a123");
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                any(Optional.class));
        aaiVnfResources.createLineOfBusinessandConnectVnf(lob, genericVnf);
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), isA(AAIResourceUri.class));
    }

    @Test
    public void deleteVnfTest() {
        doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));

        aaiVnfResources.deleteVnf(genericVnf);

        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void updateOrchestrationStatusVnfTest() {
        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.Vnf.class));

        aaiVnfResources.updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());

        assertEquals(OrchestrationStatus.ACTIVE, genericVnf.getOrchestrationStatus());
    }

    @Test
    public void updateObjectVnfTest() {
        doReturn(new org.onap.aai.domain.yang.GenericVnf()).when(MOCK_aaiObjectMapper).mapVnf(genericVnf);
        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.GenericVnf.class));

        aaiVnfResources.updateObjectVnf(genericVnf);

        verify(MOCK_aaiObjectMapper, times(1)).mapVnf(genericVnf);
        verify(MOCK_aaiResourcesClient, times(1)).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.GenericVnf.class));
    }

    @Test
    public void getGenericVnfTest() {
        Optional<org.onap.aai.domain.yang.GenericVnf> vnf = Optional.of(new org.onap.aai.domain.yang.GenericVnf());
        vnf.get().setVnfId("vnfId");
        doReturn(vnf).when(MOCK_aaiResourcesClient).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
        aaiVnfResources.getGenericVnf("vnfId");
        verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
    }

    @Test
    public void checkInMaintFlagTest() {
        Optional<org.onap.aai.domain.yang.GenericVnf> vnf = Optional.of(new org.onap.aai.domain.yang.GenericVnf());
        vnf.get().setVnfId("vnfId");
        vnf.get().setInMaint(true);
        doReturn(vnf).when(MOCK_aaiResourcesClient).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
        boolean inMaintFlag = aaiVnfResources.checkInMaintFlag("vnfId");
        verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
        assertEquals(inMaintFlag, true);
    }

    @Test
    public void connectVnfToTenantTest() {
        aaiVnfResources.connectVnfToTenant(genericVnf, cloudRegion);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId())
                        .tenant(cloudRegion.getTenantId()))),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(genericVnf.getVnfId()))));
    }

    @Test
    public void connectVnfToCloudRegionTest() {
        aaiVnfResources.connectVnfToCloudRegion(genericVnf, cloudRegion);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(genericVnf.getVnfId()))),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId()))));
    }


    @Test
    public void checkVnfClosedLoopDisabledFlagTest() {
        Optional<org.onap.aai.domain.yang.GenericVnf> vnf = Optional.of(new org.onap.aai.domain.yang.GenericVnf());
        vnf.get().setVnfId("vnfId");
        vnf.get().setIsClosedLoopDisabled(true);
        doReturn(vnf).when(MOCK_aaiResourcesClient).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
        boolean isCheckVnfClosedLoopDisabledFlag = aaiVnfResources.checkVnfClosedLoopDisabledFlag("vnfId");
        verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
        assertEquals(isCheckVnfClosedLoopDisabledFlag, true);
    }

    @Test
    public void checkVnfPserversLockedFlagTest() throws IOException {

        Optional<org.onap.aai.domain.yang.GenericVnf> vnf = Optional.of(new org.onap.aai.domain.yang.GenericVnf());
        vnf.get().setVnfId("vnfId");
        doReturn(vnf).when(MOCK_aaiResourcesClient).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
        doReturn(true).when(MOCK_aaiValidatorImpl).isPhysicalServerLocked("vnfId");
        boolean isVnfPserversLockedFlag = aaiVnfResources.checkVnfPserversLockedFlag("vnfId");
        verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.GenericVnf.class),
                isA(AAIResourceUri.class));
        verify(MOCK_aaiValidatorImpl, times(1)).setClient(isA(AAIRestClientImpl.class));
        verify(MOCK_aaiValidatorImpl, times(1)).isPhysicalServerLocked(isA(String.class));
        assertTrue(isVnfPserversLockedFlag);
    }


    @Test
    public void checkNameInUseTrueTest() {
        AAIPluralResourceUri vnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs())
                .queryParam("vnf-name", "vnfName");
        doReturn(true).when(MOCK_aaiResourcesClient).exists(eq(vnfUri));
        boolean nameInUse = aaiVnfResources.checkNameInUse("vnfName");
        assertTrue(nameInUse);
    }

    @Test
    public void checkNameInUseFalseTest() {
        AAIPluralResourceUri vnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs())
                .queryParam("vnf-name", "vnfName");
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(vnfUri));
        boolean nameInUse = aaiVnfResources.checkNameInUse("vnfName");
        assertFalse(nameInUse);
    }

    @Test
    public void queryVnfWrapperByIdTest() throws Exception {
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("vnfId")).depth(Depth.ALL);
        final String aaiResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiGenericVnf.json")));
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("vnfId");
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse);
        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(eq(uri));
        AAIResultWrapper actualResult = aaiVnfResources.queryVnfWrapperById(genericVnf);
        assertEquals(actualResult, aaiResultWrapper);

    }

    @Test
    public void getVserverTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiVserverQueryResponse.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        Optional<org.onap.aai.domain.yang.Vserver> oVserver = Optional.empty();
        AAIResourceUri vserverUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion("ModelInvariantUUID", "serviceModelVersionId").tenant("abc").vserver("abc"));

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
        oVserver = aaiVnfResources.getVserver(vserverUri);

        verify(MOCK_aaiResourcesClient, times(1)).get(any(AAIResourceUri.class));

        if (oVserver.isPresent()) {
            org.onap.aai.domain.yang.Vserver vserver = oVserver.get();
            assertThat(aaiResultWrapper.asBean(org.onap.aai.domain.yang.Vserver.class).get(), sameBeanAs(vserver));
        }
    }
}
