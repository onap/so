/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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
package org.onap.so.bpmn;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.AssignNetworkBBUtils;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionExtractResourcesAAI;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.namingservice.NamingRequestObject;
import org.onap.so.client.orchestration.AAICollectionResources;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAIInstanceGroupResources;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.AAIPnfResources;
import org.onap.so.client.orchestration.AAIServiceInstanceResources;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.orchestration.AAIVolumeGroupResources;
import org.onap.so.client.orchestration.AAIVpnBindingResources;
import org.onap.so.client.orchestration.NamingServiceResources;
import org.onap.so.client.orchestration.NetworkAdapterResources;
import org.onap.so.client.orchestration.SDNCNetworkResources;
import org.onap.so.client.orchestration.SDNCServiceInstanceResources;
import org.onap.so.client.orchestration.SDNCVfModuleResources;
import org.onap.so.client.orchestration.SDNCVnfResources;
import org.onap.so.client.orchestration.VnfAdapterVfModuleResources;
import org.onap.so.client.orchestration.VnfAdapterVolumeGroupResources;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.client.RequestsDbClient;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class BaseTaskTest extends TestDataSetup {
    @Mock
    protected AAIVolumeGroupResources aaiVolumeGroupResources;

    @Mock
    protected AAIServiceInstanceResources aaiServiceInstanceResources;

    @Mock
    protected AAIVnfResources aaiVnfResources;

    @Mock
    protected AAIPnfResources aaiPnfResources;

    @Mock
    protected AAIVfModuleResources aaiVfModuleResources;

    @Mock
    protected AAIVpnBindingResources aaiVpnBindingResources;

    @Mock
    protected AAINetworkResources aaiNetworkResources;

    @Mock
    protected AAICollectionResources aaiCollectionResources;

    @Mock
    protected NetworkAdapterResources networkAdapterResources;

    @Mock
    protected VnfAdapterVolumeGroupResources vnfAdapterVolumeGroupResources;

    @Mock
    protected VnfAdapterVfModuleResources vnfAdapterVfModuleResources;

    @Mock
    protected SDNCVnfResources sdncVnfResources;

    @Mock
    protected SDNCNetworkResources sdncNetworkResources;

    @Mock
    protected SDNCVfModuleResources sdncVfModuleResources;

    @Mock
    protected SDNCServiceInstanceResources sdncServiceInstanceResources;

    @Mock
    protected AssignNetworkBBUtils assignNetworkBBUtils;

    @Mock
    protected NetworkAdapterObjectMapper networkAdapterObjectMapper;

    @Mock
    protected AAIInstanceGroupResources aaiInstanceGroupResources;

    @Mock
    protected NamingServiceResources namingServiceResources;

    @Mock
    protected ApplicationControllerAction appCClient;

    @Mock
    protected CatalogDbClient catalogDbClient;

    @Mock
    protected RequestsDbClient requestsDbClient;

    @Mock
    protected BBInputSetupUtils bbSetupUtils;

    @Mock
    protected BBInputSetup bbInputSetup;

    @Mock
    protected AAIConfigurationResources aaiConfigurationResources;

    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected ExtractPojosForBB extractPojosForBB;

    @Mock
    protected ExceptionBuilder exceptionUtil;

    @Mock
    protected WorkflowActionExtractResourcesAAI workflowActionUtils;

    @Mock
    protected Environment env;

    @Mock
    protected NamingRequestObject namingRequestObject;

}
