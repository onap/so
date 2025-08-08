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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
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
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.mapper.AAIObjectMapper;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIInstanceGroupResourcesTest extends TestDataSetup {

    @InjectMocks
    private AAIInstanceGroupResources aaiInstanceGroupResources = new AAIInstanceGroupResources();

    private InstanceGroup instanceGroup;
    private GenericVnf vnf;
    private ServiceInstance serviceInstance;

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    @Before
    public void before() {
        instanceGroup = buildInstanceGroup();
        vnf = buildGenericVnf();
        serviceInstance = buildServiceInstance();
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }

    @Test
    public void createInstanceGroupTest() {
        doReturn(new org.onap.aai.domain.yang.InstanceGroup()).when(MOCK_aaiObjectMapper)
                .mapInstanceGroup(instanceGroup);
        aaiInstanceGroupResources.createInstanceGroup(instanceGroup);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(
                eq(AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))),
                isA(Optional.class));
    }

    @Test
    public void deleteInstanceGroupTest() {
        aaiInstanceGroupResources.deleteInstanceGroup(instanceGroup);
        verify(MOCK_aaiResourcesClient, times(1)).delete(eq(
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))));
    }

    @Test
    public void connectInstanceGroupTest() {
        aaiInstanceGroupResources.connectInstanceGroupToVnf(instanceGroup, vnf);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()))));
    }

    @Test
    public void connectInstanceGroupWithEdgeTest() {
        aaiInstanceGroupResources.connectInstanceGroupToVnf(instanceGroup, vnf, AAIEdgeLabel.BELONGS_TO);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()))),
                eq(AAIEdgeLabel.BELONGS_TO));
    }

    @Test
    public void existsTest() {
        aaiInstanceGroupResources.exists(instanceGroup);
        verify(MOCK_aaiResourcesClient, times(1)).exists(eq(
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))));
    }

    @Test
    public void createInstanceGroupandConnectServiceInstanceTest() {
        doReturn(new org.onap.aai.domain.yang.InstanceGroup()).when(MOCK_aaiObjectMapper)
                .mapInstanceGroup(instanceGroup);
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                any(Optional.class));
        doNothing().when(MOCK_aaiResourcesClient).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));

        aaiInstanceGroupResources.createInstanceGroupandConnectServiceInstance(instanceGroup, serviceInstance);

        verify(MOCK_aaiObjectMapper, times(1)).mapInstanceGroup(instanceGroup);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void checkInstanceGroupNameInUseTrueTest() {
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroups())
                .queryParam("instance-group-name", "instanceGroupName");
        doReturn(true).when(MOCK_aaiResourcesClient).exists(eq(uri));
        boolean nameInUse = aaiInstanceGroupResources.checkInstanceGroupNameInUse("instanceGroupName");
        assertTrue(nameInUse);
    }

    @Test
    public void checkInstanceGroupNameInUseFalseTest() {
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroups())
                .queryParam("instance-group-name", "instanceGroupName");
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(uri));
        boolean nameInUse = aaiInstanceGroupResources.checkInstanceGroupNameInUse("instanceGroupName");
        assertFalse(nameInUse);
    }

}
