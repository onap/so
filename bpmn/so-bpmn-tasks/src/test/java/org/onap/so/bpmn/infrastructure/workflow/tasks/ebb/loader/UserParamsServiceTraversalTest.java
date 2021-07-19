/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

public class UserParamsServiceTraversalTest extends BaseTaskTest {

    private static final String MACRO_ASSIGN_JSON = "Macro/ServiceMacroAssign.json";
    private static final String MACRO_CREATE_JSON = "Macro/ServiceMacroAssignVnfAndPnf.json";
    private static final String MACRO_ASSIGN_PNF_JSON = "Macro/ServiceMacroAssignPnf.json";
    private static final String NETWORK_COLLECTION_JSON = "Macro/CreateNetworkCollection.json";
    private static final String MACRO_CREATE_WITHOUT_RESOURCES_JSON = "Macro/ServiceMacroCreateWithoutResources.json";
    private static final String serviceInstanceId = "123";
    private DelegateExecution execution;
    private CatalogDbClient mockCatalogDbClient;
    private UserParamsServiceTraversal userParamsServiceTraversal;
    private String requestAction;

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        mockCatalogDbClient = mock(CatalogDbClient.class);
        userParamsServiceTraversal = new UserParamsServiceTraversal(mockCatalogDbClient, mock(ExceptionBuilder.class));
        requestAction = "assignInstance";
    }

    @Test
    public void getResourceListFromUserParams() throws Exception {
        initExecution(requestAction, readBpmnRequestFromFile(MACRO_CREATE_WITHOUT_RESOURCES_JSON), false);
        Mockito.doReturn(getVfModuleCustomization()).when(mockCatalogDbClient)
                .getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        Mockito.doReturn(getCvnfcCustomizations()).when(mockCatalogDbClient).getCvnfcCustomization(anyString(),
                anyString(), anyString());

        List<Resource> resourceListFromUserParams = userParamsServiceTraversal.getResourceListFromUserParams(execution,
                getUserParams(), serviceInstanceId, requestAction);
        List<WorkflowType> expected = List.of(WorkflowType.SERVICE);
        List<WorkflowType> result =
                resourceListFromUserParams.stream().map(Resource::getResourceType).collect(Collectors.toList());

        assertEquals(1, resourceListFromUserParams.size());
        assertThat(expected, is(result));
    }

    @Test
    public void getResourceListFromUserParamsForVnfs() throws Exception {
        initExecution(requestAction, readBpmnRequestFromFile(MACRO_ASSIGN_JSON), false);
        Mockito.doReturn(getVfModuleCustomization()).when(mockCatalogDbClient)
                .getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        Mockito.doReturn(getCvnfcCustomizations()).when(mockCatalogDbClient).getCvnfcCustomization(anyString(),
                anyString(), anyString());

        List<Resource> resourceListFromUserParams = userParamsServiceTraversal.getResourceListFromUserParams(execution,
                getUserParams(), serviceInstanceId, requestAction);
        List<WorkflowType> expected = List.of(WorkflowType.SERVICE, WorkflowType.VNF, WorkflowType.VOLUMEGROUP,
                WorkflowType.VFMODULE, WorkflowType.CONFIGURATION);
        List<WorkflowType> result =
                resourceListFromUserParams.stream().map(Resource::getResourceType).collect(Collectors.toList());

        assertEquals(5, resourceListFromUserParams.size());
        assertThat(expected, is(result));
    }

    @Test
    public void getResourceListFromUserParamsForVnfsWithPriorities() throws Exception {
        initExecution(requestAction, readBpmnRequestFromFile(MACRO_CREATE_JSON), false);
        Mockito.doReturn(getVfModuleCustomization()).when(mockCatalogDbClient)
                .getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        Mockito.doReturn(getCvnfcCustomizations()).when(mockCatalogDbClient).getCvnfcCustomization(anyString(),
                anyString(), anyString());

        List<Resource> resourceListFromUserParams = userParamsServiceTraversal.getResourceListFromUserParams(execution,
                getUserParams(), serviceInstanceId, requestAction);
        List<WorkflowType> expected = List.of(WorkflowType.SERVICE, WorkflowType.VNF, WorkflowType.VOLUMEGROUP,
                WorkflowType.VFMODULE, WorkflowType.CONFIGURATION, WorkflowType.PNF);
        List<WorkflowType> result =
                resourceListFromUserParams.stream().map(Resource::getResourceType).collect(Collectors.toList());

        assertEquals(6, resourceListFromUserParams.size());
        assertThat(expected, is(result));
        assertEquals(2, resourceListFromUserParams.get(1).getChildren().get(1).getProcessingPriority());
    }

    @Test
    public void getResourceListFromUserParamsForPnfs() throws Exception {
        initExecution(requestAction, readBpmnRequestFromFile(MACRO_ASSIGN_PNF_JSON), false);
        Mockito.doReturn(getVfModuleCustomization()).when(mockCatalogDbClient)
                .getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        Mockito.doReturn(getCvnfcCustomizations()).when(mockCatalogDbClient).getCvnfcCustomization(anyString(),
                anyString(), anyString());

        List<Resource> resourceListFromUserParams = userParamsServiceTraversal.getResourceListFromUserParams(execution,
                getUserParams(), serviceInstanceId, requestAction);
        List<WorkflowType> expected = List.of(WorkflowType.SERVICE, WorkflowType.PNF);
        List<WorkflowType> result =
                resourceListFromUserParams.stream().map(Resource::getResourceType).collect(Collectors.toList());

        assertEquals(2, resourceListFromUserParams.size());
        assertThat(expected, is(result));
    }

    @Test
    public void getResourceListFromUserParamsForNetworks() throws Exception {
        requestAction = "createInstance";
        initExecution(requestAction, readBpmnRequestFromFile(NETWORK_COLLECTION_JSON), false);
        Mockito.doReturn(getVfModuleCustomization()).when(mockCatalogDbClient)
                .getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        Mockito.doReturn(getCvnfcCustomizations()).when(mockCatalogDbClient).getCvnfcCustomization(anyString(),
                anyString(), anyString());
        Mockito.doReturn(getService()).when(mockCatalogDbClient).getServiceByID(anyString());
        Mockito.doReturn(new NetworkCollectionResourceCustomization()).when(mockCatalogDbClient)
                .getNetworkCollectionResourceCustomizationByID(anyString());

        List<Resource> resourceListFromUserParams = userParamsServiceTraversal.getResourceListFromUserParams(execution,
                getUserParams(), serviceInstanceId, requestAction);
        List<WorkflowType> expected = List.of(WorkflowType.SERVICE, WorkflowType.NETWORK, WorkflowType.NETWORK,
                WorkflowType.NETWORKCOLLECTION);
        List<WorkflowType> result =
                resourceListFromUserParams.stream().map(Resource::getResourceType).collect(Collectors.toList());

        assertEquals(4, resourceListFromUserParams.size());
        assertThat(expected, is(result));
    }

    @Test
    public void getResourceListFromUserParamsBuildAndThrowExceptionWhenVfModuleAreEmpty() throws Exception {
        initExecution(requestAction, readBpmnRequestFromFile(MACRO_ASSIGN_JSON), false);
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVfModule(null);
        Mockito.doReturn(vfModuleCustomization).when(mockCatalogDbClient)
                .getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");

        List<Resource> resourceListFromUserParams = userParamsServiceTraversal.getResourceListFromUserParams(execution,
                getUserParams(), serviceInstanceId, requestAction);
        List<WorkflowType> expected = List.of(WorkflowType.SERVICE, WorkflowType.VNF);
        List<WorkflowType> result =
                resourceListFromUserParams.stream().map(Resource::getResourceType).collect(Collectors.toList());

        assertEquals(2, resourceListFromUserParams.size());
        assertThat(expected, is(result));
    }

    private List<Map<String, Object>> getUserParams() throws IOException {
        String bpmnRequest = (String) execution.getVariable(BBConstants.G_BPMN_REQUEST);
        ServiceInstancesRequest sIRequest = new ObjectMapper().readValue(bpmnRequest, ServiceInstancesRequest.class);
        return sIRequest.getRequestDetails().getRequestParameters().getUserParams();
    }

    @Test
    public void getResourceListFromUserParamsWhenUserParamsAreNull() throws Exception {
        List<Resource> expectedResourceList = new ArrayList<>();
        List<Resource> resultResourceList = userParamsServiceTraversal.getResourceListFromUserParams(execution, null,
                serviceInstanceId, requestAction);

        assertEquals(expectedResourceList, resultResourceList);
    }

    private String readBpmnRequestFromFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/" + fileName)));
    }

    private void initExecution(String gAction, String bpmnRequest, boolean isAlaCarte) {
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", isAlaCarte);
        execution.setVariable("apiVersion", "7");
    }

    private Service getService() {
        Service service = new Service();
        List<CollectionResourceCustomization> collectionResourceCustomizations = new ArrayList<>();
        CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
        collectionResourceCustomization.setModelCustomizationUUID("123");
        collectionResourceCustomizations.add(collectionResourceCustomization);
        service.setCollectionResourceCustomizations(collectionResourceCustomizations);
        return service;
    }

    private VfModuleCustomization getVfModuleCustomization() {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        VfModule vfModule = new VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModule.setModelName("helm");
        vfModule.setModuleHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        return vfModuleCustomization;
    }

    private List<CvnfcCustomization> getCvnfcCustomizations() {
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setToscaNodeType("FabricConfiguration");

        CvnfcConfigurationCustomization cvnfcConfigurationCustomization = new CvnfcConfigurationCustomization();
        cvnfcConfigurationCustomization.setConfigurationResource(configurationResource);
        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();

        List<CvnfcConfigurationCustomization> cvnfcConfigurationCustomizations = new ArrayList<>();
        cvnfcConfigurationCustomizations.add(cvnfcConfigurationCustomization);
        cvnfcCustomization.setCvnfcConfigurationCustomization(cvnfcConfigurationCustomizations);

        List<CvnfcCustomization> cvnfcCustomizations = new ArrayList<>();
        cvnfcCustomizations.add(cvnfcCustomization);
        return cvnfcCustomizations;
    }
}
