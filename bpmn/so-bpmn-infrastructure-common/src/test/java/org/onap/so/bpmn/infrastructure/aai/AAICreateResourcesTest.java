/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.aai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;

@RunWith(MockitoJUnitRunner.class)
public class AAICreateResourcesTest {

    private AAICreateResources aaiCreateResources;

    private String projectName;
    private String serviceInstanceId;
    private String owningEntityId;
    private String owningEntityName;
    private String platformName;
    private String vnfId;
    private String lineOfBusiness;
    private String globalCustomerId;
    private String serviceType;

    @Spy
    private AAIResourcesClient aaiResourcesClient;

    @Before
    public void before() {


        aaiCreateResources = new AAICreateResources();
        aaiCreateResources.setAaiClient(aaiResourcesClient);

        projectName = "projectName";
        serviceInstanceId = "serviceInstanceId";
        owningEntityId = "owningEntityId";
        owningEntityName = "owningEntityName";
        platformName = "platformName";
        vnfId = "vnfId";
        lineOfBusiness = "lineOfBusiness";
        globalCustomerId = "globalCustomerId";
        serviceType = "serviceType";
    }

    @Test
    public void createAAIProjectTest() {
        doReturn(aaiResourcesClient).when(aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                isA(Optional.class));
        doNothing().when(aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));


        aaiCreateResources.createAAIProject(projectName, serviceInstanceId);

        AAIResourceUri projectURI = AAIUriFactory.createResourceUri(AAIObjectType.PROJECT, projectName);

        verify(aaiResourcesClient, times(1)).createIfNotExists(projectURI, Optional.empty());
        verify(aaiResourcesClient, times(1)).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
    }

    @Test
    public void createAAIOwningEntityTest() {
        doReturn(aaiResourcesClient).when(aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                isA(Optional.class));
        doNothing().when(aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));

        aaiCreateResources.createAAIOwningEntity(owningEntityId, owningEntityName, serviceInstanceId);

        HashMap<String, String> owningEntityMap = new HashMap<>();
        owningEntityMap.put("owning-entity-name", owningEntityName);
        AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntityId);

        verify(aaiResourcesClient, times(1)).createIfNotExists(owningEntityURI, Optional.of(owningEntityMap));
        verify(aaiResourcesClient, times(1)).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
    }

    @Test
    public void existsOwningEntityTest() {
        doReturn(true).when(aaiResourcesClient).exists(isA(AAIResourceUri.class));

        boolean expectedBoolean = aaiCreateResources.existsOwningEntity(owningEntityId);

        AAIResourceUri owningEntityURI = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, owningEntityId);

        verify(aaiResourcesClient, times(1)).exists(owningEntityURI);
        assertTrue(expectedBoolean);
    }

    @Test
    public void connectOwningEntityandServiceInstanceTest() {
        doNothing().when(aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));

        aaiCreateResources.connectOwningEntityandServiceInstance(owningEntityId, serviceInstanceId);

        verify(aaiResourcesClient, times(1)).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
    }

    @Test
    public void createAAIPlatformTest() {
        doReturn(aaiResourcesClient).when(aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                isA(Optional.class));
        doNothing().when(aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));

        aaiCreateResources.createAAIPlatform(platformName, vnfId);

        AAIResourceUri platformURI = AAIUriFactory.createResourceUri(AAIObjectType.PLATFORM, platformName);

        verify(aaiResourcesClient, times(1)).createIfNotExists(platformURI, Optional.empty());
        verify(aaiResourcesClient, times(1)).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
    }

    @Test
    public void createAAILineOfBusinessTest() {
        doReturn(aaiResourcesClient).when(aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                isA(Optional.class));
        doNothing().when(aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));

        aaiCreateResources.createAAILineOfBusiness(lineOfBusiness, vnfId);

        AAIResourceUri lineOfBusinessURI =
                AAIUriFactory.createResourceUri(AAIObjectType.LINE_OF_BUSINESS, lineOfBusiness);

        verify(aaiResourcesClient, times(1)).createIfNotExists(lineOfBusinessURI, Optional.empty());
        verify(aaiResourcesClient, times(1)).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
    }

    @Test
    public void createAAIServiceInstanceTest() {
        doReturn(aaiResourcesClient).when(aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                isA(Optional.class));

        aaiCreateResources.createAAIServiceInstance(globalCustomerId, serviceType, serviceInstanceId);

        AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                globalCustomerId, serviceType, serviceInstanceId);

        verify(aaiResourcesClient, times(1)).createIfNotExists(serviceInstanceURI, Optional.empty());
    }

    @Test
    public void getVnfInstanceTest() {
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper("vnfUriAaiResponse");

        doReturn(aaiResultWrapper).when(aaiResourcesClient).get(isA(AAIResourceUri.class));

        Optional<GenericVnf> actualVnf = aaiCreateResources.getVnfInstance(vnfId);

        AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);

        verify(aaiResourcesClient, times(1)).get(vnfURI);
        assertEquals(actualVnf, aaiResultWrapper.asBean(GenericVnf.class));
    }

    @Test
    public void getVnfInstanceExceptionTest() {
        doThrow(RuntimeException.class).when(aaiResourcesClient).get(isA(AAIResourceUri.class));

        Optional<GenericVnf> actualVnf = aaiCreateResources.getVnfInstance(vnfId);

        AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);

        verify(aaiResourcesClient, times(1)).get(vnfURI);
        assertEquals(actualVnf, Optional.empty());
    }
}
