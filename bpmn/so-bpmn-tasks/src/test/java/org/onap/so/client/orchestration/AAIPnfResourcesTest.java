/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import joptsimple.internal.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIPnfResourcesTest extends TestDataSetup {

    public static final String TEST_VERSION = "testVersion";
    private static final String PNF_NAME = "pnfTest";
    public static final String TEST_CUSTOMIZATION_UUID = "testCustomizationUuid";
    public static final String TEST_INVARIANT_UUID = "testInvariantUuid";
    public static final String TEST_ROLE = "testRole";

    private Pnf pnf;
    private ServiceInstance serviceInstance;

    @Mock
    protected AAIObjectMapper aaiObjectMapperMock;

    @Mock
    protected InjectionHelper injectionHelperMock;

    @Mock
    protected AAIResourcesClient aaiResourcesClientMock;

    @InjectMocks
    AAIPnfResources testedObject = new AAIPnfResources();

    @Before
    public void setUp() {
        pnf = buildPnf();
        pnf.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        serviceInstance = buildServiceInstance();

        doReturn(aaiResourcesClientMock).when(injectionHelperMock).getAaiClient();
    }

    @Test
    public void createPnfAndConnectServiceInstanceShouldSetInventoriedStatusAndCallConnectMethod() {
        org.onap.aai.domain.yang.Pnf pnfYang = new org.onap.aai.domain.yang.Pnf();

        doReturn(pnfYang).when(aaiObjectMapperMock).mapPnf(pnf);
        doReturn(aaiResourcesClientMock).when(aaiResourcesClientMock).createIfNotExists(any(AAIResourceUri.class),
                eq(Optional.of(pnfYang)));

        testedObject.createPnfAndConnectServiceInstance(pnf, serviceInstance);

        assertEquals(OrchestrationStatus.INVENTORIED, pnf.getOrchestrationStatus());
        verify(aaiResourcesClientMock, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void updateOrchestrationStatusPnfShouldSetStatusAndUpdatePnfInAAI() {
        org.onap.aai.domain.yang.Pnf pnfYang = new org.onap.aai.domain.yang.Pnf();
        doReturn(pnfYang).when(aaiObjectMapperMock).mapPnf(pnf);

        testedObject.updateOrchestrationStatusPnf(pnf, OrchestrationStatus.ACTIVE);

        assertEquals(OrchestrationStatus.ACTIVE, pnf.getOrchestrationStatus());
        verify(aaiObjectMapperMock, times(1))
                .mapPnf(argThat(arg -> OrchestrationStatus.ACTIVE.equals(arg.getOrchestrationStatus())));
        verify(aaiResourcesClientMock, times(1)).update(any(AAIResourceUri.class), eq(pnfYang));
    }

    @Test
    public void existingPnfInAaiWithInventoriedStatusCanBeUsed() throws Exception {
        // given
        Pnf pnfTest = cretePacWithDefaultsName();
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(OrchestrationStatus.INVENTORIED.toString());
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        testedObject.checkIfPnfExistsInAaiAndCanBeUsed(pnfTest);
        verify(aaiResourcesClientMock, times(1)).update(any(), any());
    }

    @Test
    public void existingPnfInAaiWithNullStatusCanBeUsed() throws Exception {
        // given
        Pnf pnfTest = cretePacWithDefaultsName();
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(null);
        pnfTest.setRole("test");
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        testedObject.checkIfPnfExistsInAaiAndCanBeUsed(pnfTest);
        verify(aaiResourcesClientMock, times(1)).update(any(), eq(pnfFromAai));
    }

    @Test
    public void existingPnfInAaiIsUpdated() throws Exception {
        // given
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(null);
        Pnf pnfTest = getPnfWithTestValues();
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        testedObject.checkIfPnfExistsInAaiAndCanBeUsed(pnfTest);
        verify(aaiResourcesClientMock, times(1)).update(any(), eq(pnfFromAai));
        verifyPnfFromAai(pnfFromAai);
    }

    private void verifyPnfFromAai(org.onap.aai.domain.yang.Pnf pnf) {
        assertEquals(OrchestrationStatus.INVENTORIED.toString(), pnf.getOrchestrationStatus());
        assertEquals(TEST_ROLE, pnf.getNfRole());
        assertEquals(TEST_CUSTOMIZATION_UUID, pnf.getModelCustomizationId());
        assertEquals(TEST_INVARIANT_UUID, pnf.getModelInvariantId());
        assertEquals(TEST_VERSION, pnf.getModelVersionId());
    }

    private Pnf getPnfWithTestValues() {
        Pnf pnfTest = cretePacWithDefaultsName();
        ModelInfoPnf modelInfoPnf = getModelInfoPnf();
        pnfTest.setModelInfoPnf(modelInfoPnf);
        pnfTest.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        pnfTest.setRole(TEST_ROLE);
        return pnfTest;
    }

    private ModelInfoPnf getModelInfoPnf() {
        ModelInfoPnf modelInfoPnf = new ModelInfoPnf();
        modelInfoPnf.setModelCustomizationUuid(TEST_CUSTOMIZATION_UUID);
        modelInfoPnf.setModelInvariantUuid(TEST_INVARIANT_UUID);
        modelInfoPnf.setModelUuid(TEST_VERSION);
        return modelInfoPnf;
    }

    @Test
    public void existingPnfInAaiWithEmptyStatusCanBeUsed() throws Exception {
        // given
        Pnf pnfTest = cretePacWithDefaultsName();
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(Strings.EMPTY);
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        testedObject.checkIfPnfExistsInAaiAndCanBeUsed(pnfTest);
    }

    @Test
    public void existingPnfInAaiCanNotBeUsed() {
        // given
        Pnf pnfTest = cretePacWithDefaultsName();
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(OrchestrationStatus.ACTIVE.toString());
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        try {
            testedObject.checkIfPnfExistsInAaiAndCanBeUsed(pnfTest);
        } catch (Exception e) {
            // then
            assertThat(e.getMessage()).isEqualTo(String.format(
                    "pnf with name %s already exists with orchestration status Active, existing pnf can be used only "
                            + "if status is not set or set as Inventoried",
                    PNF_NAME));
        }
        verify(aaiResourcesClientMock, times(0)).update(any(), any());
    }

    @Test
    public void existingPnfInAaiIsRelatedToService() throws IOException {
        // given
        final String relatedTo = "service-instance";
        final String serviceInstanceId = "service-instance-id";
        final String path = "src/test/resources/__files/BuildingBlocks/aaiPnf.json";
        Pnf pnfTest = cretePacWithDefaultsName();
        org.onap.aai.domain.yang.Pnf pnfFromAai =
                new ObjectMapper().readValue(new File(path), org.onap.aai.domain.yang.Pnf.class);
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        try {
            testedObject.checkIfPnfExistsInAaiAndCanBeUsed(pnfTest);
        } catch (Exception e) {
            // then
            assertThat(e.getMessage()).isEqualTo(String.format(
                    "Pnf with name %s exist with orchestration status %s and is related to %s service with certain service-instance-id: %s",
                    PNF_NAME, OrchestrationStatus.ACTIVE, relatedTo, serviceInstanceId));
        }
        verify(aaiResourcesClientMock, times(0)).update(any(), any());
    }

    private org.onap.aai.domain.yang.Pnf createPnf(String orchestrationStatus) {
        org.onap.aai.domain.yang.Pnf pnfFromAai = new org.onap.aai.domain.yang.Pnf();
        pnfFromAai.setPnfName(PNF_NAME);
        pnfFromAai.setOrchestrationStatus(orchestrationStatus);
        return pnfFromAai;
    }

    private Pnf cretePacWithDefaultsName() {
        Pnf pnfTest = new Pnf();
        pnfTest.setPnfName(PNF_NAME);
        return pnfTest;
    }
}
