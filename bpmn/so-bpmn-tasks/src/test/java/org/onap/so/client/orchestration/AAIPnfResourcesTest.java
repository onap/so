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
import java.util.Optional;
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
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIPnfResourcesTest extends TestDataSetup {

    private static final String PNF_NAME = "pnfTest";

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
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(OrchestrationStatus.INVENTORIED.toString());
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        testedObject.checkIfPnfExistsInAaiAndCanBeUsed(PNF_NAME);
    }

    @Test
    public void existingPnfInAaiWithNullStatusCanBeUsed() throws Exception {
        // given
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(null);
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        testedObject.checkIfPnfExistsInAaiAndCanBeUsed(PNF_NAME);
    }

    @Test
    public void existingPnfInAaiWithEmptyStatusCanBeUsed() throws Exception {
        // given
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(Strings.EMPTY);
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        testedObject.checkIfPnfExistsInAaiAndCanBeUsed(PNF_NAME);
    }

    @Test
    public void existingPnfInAaiCanNotBeUsed() {
        // given
        org.onap.aai.domain.yang.Pnf pnfFromAai = createPnf(OrchestrationStatus.ACTIVE.toString());
        when(injectionHelperMock.getAaiClient().get(org.onap.aai.domain.yang.Pnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.PNF, PNF_NAME))).thenReturn(Optional.of(pnfFromAai));
        // when
        try {
            testedObject.checkIfPnfExistsInAaiAndCanBeUsed(PNF_NAME);
        } catch (Exception e) {
            // then
            assertThat(e.getMessage()).isEqualTo(String.format(
                    "pnf with name %s already exists with orchestration status Active, only status Inventoried allows to use existing pnf",
                    PNF_NAME));
        }
    }

    private org.onap.aai.domain.yang.Pnf createPnf(String orchestrationStatus) {
        org.onap.aai.domain.yang.Pnf pnfFromAai = new org.onap.aai.domain.yang.Pnf();
        pnfFromAai.setPnfName(PNF_NAME);
        pnfFromAai.setOrchestrationStatus(orchestrationStatus);
        return pnfFromAai;
    }
}
