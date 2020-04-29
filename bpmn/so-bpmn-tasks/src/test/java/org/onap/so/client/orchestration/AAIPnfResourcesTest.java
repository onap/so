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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

    private Pnf pnf;
    private ServiceInstance serviceInstance;

    @Mock
    protected AAIObjectMapper aaiObjectMapperMock;

    @Mock
    protected InjectionHelper injectionHelperMock;

    @Mock
    protected AAIResourcesClient aaiResourcesClientMock;

    @InjectMocks
    AAIPnfResources aaiPnfResources = new AAIPnfResources();

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

        aaiPnfResources.createPnfAndConnectServiceInstance(pnf, serviceInstance);

        assertEquals(OrchestrationStatus.INVENTORIED, pnf.getOrchestrationStatus());
        verify(aaiResourcesClientMock, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void updateOrchestrationStatusPnfShouldSetStatusAndUpdatePnfInAAI() {
        org.onap.aai.domain.yang.Pnf pnfYang = new org.onap.aai.domain.yang.Pnf();
        doReturn(pnfYang).when(aaiObjectMapperMock).mapPnf(pnf);

        aaiPnfResources.updateOrchestrationStatusPnf(pnf, OrchestrationStatus.ACTIVE);

        assertEquals(OrchestrationStatus.ACTIVE, pnf.getOrchestrationStatus());
        verify(aaiObjectMapperMock, times(1))
                .mapPnf(argThat(arg -> OrchestrationStatus.ACTIVE.equals(arg.getOrchestrationStatus())));
        verify(aaiResourcesClientMock, times(1)).update(any(AAIResourceUri.class), eq(pnfYang));
    }

}
