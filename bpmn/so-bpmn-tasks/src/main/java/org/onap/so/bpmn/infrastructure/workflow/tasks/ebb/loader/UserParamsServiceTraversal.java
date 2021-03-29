/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.Pnfs;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CREATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.FABRIC_CONFIGURATION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.USER_PARAM_SERVICE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.WORKFLOW_ACTION_ERROR_MESSAGE;

@Component
public class UserParamsServiceTraversal {

    private static final Logger logger = LoggerFactory.getLogger(UserParamsServiceTraversal.class);

    private final CatalogDbClient catalogDbClient;
    private final ExceptionBuilder exceptionBuilder;
    private boolean foundVfModuleOrVG;
    private String vnfCustomizationUUID;
    private String vfModuleCustomizationUUID;

    UserParamsServiceTraversal(CatalogDbClient catalogDbClient, ExceptionBuilder exceptionBuilder) {
        this.catalogDbClient = catalogDbClient;
        this.exceptionBuilder = exceptionBuilder;
    }

    public List<Resource> getResourceListFromUserParams(DelegateExecution execution,
            List<Map<String, Object>> userParams, String serviceModelVersionId, String requestAction)
            throws IOException {
        if (userParams != null) {
            for (Map<String, Object> params : userParams) {
                if (params.containsKey(USER_PARAM_SERVICE)) {
                    ObjectMapper obj = new ObjectMapper();
                    String input = obj.writeValueAsString(params.get(USER_PARAM_SERVICE));
                    Service validate = obj.readValue(input, Service.class);
                    return getResourceList(execution, serviceModelVersionId, requestAction, validate);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Resource> getResourceList(DelegateExecution execution, String serviceModelVersionId,
            String requestAction, Service validate) {
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(new Resource(WorkflowType.SERVICE, validate.getModelInfo().getModelVersionId(), false));
        if (validate.getResources().getVnfs() != null) {
            setResourceListForVnfs(execution, resourceList, validate);
        }
        if (validate.getResources().getPnfs() != null) {
            setResourceListForPnfs(resourceList, validate);
        }
        if (validate.getResources().getNetworks() != null) {
            setResourceListForNetworks(execution, serviceModelVersionId, requestAction, resourceList, validate);
        }
        return resourceList;
    }

    private void setResourceListForVnfs(DelegateExecution execution, List<Resource> resourceList, Service validate) {
        for (Vnfs vnf : validate.getResources().getVnfs()) {
            setVnfCustomizationUUID(vnf);
            resourceList.add(new Resource(WorkflowType.VNF, vnf.getModelInfo().getModelCustomizationId(), false));
            setResourceListForVfModules(execution, resourceList, validate, vnf);
        }
    }

    private void setResourceListForVfModules(DelegateExecution execution, List<Resource> resourceList, Service validate,
            Vnfs vnf) {
        if (vnf.getVfModules() != null) {
            for (VfModules vfModule : vnf.getVfModules()) {
                setVfModuleCustomizationUUID(vfModule);
                VfModuleCustomization vfModuleCustomization =
                        catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(vfModuleCustomizationUUID);
                if (vfModuleCustomization != null && vfModuleCustomization.getVfModule() != null) {
                    setVolumeGroupWorkFlowTypeToResourceList(resourceList, vfModuleCustomization);
                    setVfModuleAndConfigurationWorkFlowTypeToResourceList(resourceList, validate, vnf, vfModule,
                            vfModuleCustomization);
                    if (!foundVfModuleOrVG) {
                        buildAndThrowException(execution,
                                "Could not determine if vfModule was a vfModule or volume group. Heat template and Heat env are null");
                    }
                }
            }
        }
    }

    private void setVolumeGroupWorkFlowTypeToResourceList(List<Resource> resourceList,
            VfModuleCustomization vfModuleCustomization) {
        if (vfModuleCustomization.getVfModule().getVolumeHeatTemplate() != null
                && vfModuleCustomization.getVolumeHeatEnv() != null) {
            foundVfModuleOrVG = true;
            resourceList.add(
                    new Resource(WorkflowType.VOLUMEGROUP, vfModuleCustomization.getModelCustomizationUUID(), false));
        }
    }

    private void setVfModuleAndConfigurationWorkFlowTypeToResourceList(List<Resource> resourceList, Service validate,
            Vnfs vnf, VfModules vfModule, VfModuleCustomization vfModuleCustomization) {
        if ((vfModuleCustomization.getVfModule().getModuleHeatTemplate() != null
                && vfModuleCustomization.getHeatEnvironment() != null)
                || (vfModuleCustomization.getVfModule().getModelName() != null
                        && vfModuleCustomization.getVfModule().getModelName().contains("helm"))) {
            foundVfModuleOrVG = true;
            Resource resource = setVfModuleWorkFlowTypeToResourceList(resourceList, vfModuleCustomization);
            setConfigurationWorkFlowTypeToResourceList(resourceList, validate, vnf, vfModule, resource);
        }
    }

    private Resource setVfModuleWorkFlowTypeToResourceList(List<Resource> resourceList,
            VfModuleCustomization vfModuleCustomization) {
        Resource resource =
                new Resource(WorkflowType.VFMODULE, vfModuleCustomization.getModelCustomizationUUID(), false);
        resource.setBaseVfModule(vfModuleCustomization.getVfModule().getIsBase() != null
                && vfModuleCustomization.getVfModule().getIsBase());
        resourceList.add(resource);
        return resource;
    }

    private void setConfigurationWorkFlowTypeToResourceList(List<Resource> resourceList, Service validate, Vnfs vnf,
            VfModules vfModule, Resource resource) {
        if (!vnfCustomizationUUID.isEmpty() && !vfModuleCustomizationUUID.isEmpty()) {
            List<CvnfcConfigurationCustomization> configs =
                    traverseCatalogDbForConfiguration(validate.getModelInfo().getModelVersionId());
            for (CvnfcConfigurationCustomization config : configs) {
                Resource configResource = new Resource(WorkflowType.CONFIGURATION,
                        config.getConfigurationResource().getModelUUID(), false);
                resource.setVnfCustomizationId(vnf.getModelInfo().getModelCustomizationId());
                resource.setVfModuleCustomizationId(vfModule.getModelInfo().getModelCustomizationId());
                resourceList.add(configResource);
            }
        }
    }

    private void setVfModuleCustomizationUUID(VfModules vfModule) {
        if (vfModule.getModelInfo() != null && vfModule.getModelInfo().getModelCustomizationUuid() != null) {
            vfModuleCustomizationUUID = vfModule.getModelInfo().getModelCustomizationUuid();
        } else {
            vfModuleCustomizationUUID = "";
        }
    }

    private void setVnfCustomizationUUID(Vnfs vnf) {
        if (vnf.getModelInfo() != null && vnf.getModelInfo().getModelCustomizationUuid() != null) {
            vnfCustomizationUUID = vnf.getModelInfo().getModelCustomizationUuid();
        } else {
            vnfCustomizationUUID = "";
        }
    }

    private void setResourceListForPnfs(List<Resource> resourceList, Service validate) {
        for (Pnfs pnf : validate.getResources().getPnfs()) {
            resourceList.add(new Resource(WorkflowType.PNF, pnf.getModelInfo().getModelCustomizationId(), false));
        }
    }

    private void setResourceListForNetworks(DelegateExecution execution, String serviceModelVersionId,
            String requestAction, List<Resource> resourceList, Service validate) {
        for (Networks network : validate.getResources().getNetworks()) {
            resourceList
                    .add(new Resource(WorkflowType.NETWORK, network.getModelInfo().getModelCustomizationId(), false));
        }
        if (requestAction.equals(CREATE_INSTANCE)) {
            String networkColCustId = queryCatalogDbForNetworkCollection(execution, serviceModelVersionId);
            if (networkColCustId != null) {
                resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION, networkColCustId, false));
            }
        }
    }


    private List<CvnfcConfigurationCustomization> traverseCatalogDbForConfiguration(String serviceModelUUID) {
        try {
            List<CvnfcCustomization> cvnfcCustomizations = catalogDbClient.getCvnfcCustomization(serviceModelUUID,
                    vnfCustomizationUUID, vfModuleCustomizationUUID);
            return getCvnfcConfigurationCustomizations(cvnfcCustomizations);
        } catch (Exception ex) {
            logger.error("Error in finding configurations", ex);
            return Collections.emptyList();
        }
    }

    private List<CvnfcConfigurationCustomization> getCvnfcConfigurationCustomizations(
            List<CvnfcCustomization> cvnfcCustomizations) {
        List<CvnfcConfigurationCustomization> configurations = new ArrayList<>();
        for (CvnfcCustomization cvnfc : cvnfcCustomizations) {
            for (CvnfcConfigurationCustomization customization : cvnfc.getCvnfcConfigurationCustomization()) {
                if (customization.getConfigurationResource().getToscaNodeType().contains(FABRIC_CONFIGURATION)) {
                    configurations.add(customization);
                }
            }
        }
        logger.debug("found {} fabric configuration(s)", configurations.size());
        return configurations;
    }

    private String queryCatalogDbForNetworkCollection(DelegateExecution execution, String serviceModelVersionId) {
        org.onap.so.db.catalog.beans.Service service = catalogDbClient.getServiceByID(serviceModelVersionId);
        if (service != null) {
            CollectionResourceCustomization networkCollection = this.findCatalogNetworkCollection(execution, service);
            if (networkCollection != null) {
                return networkCollection.getModelCustomizationUUID();
            }
        }
        return null;
    }

    private CollectionResourceCustomization findCatalogNetworkCollection(DelegateExecution execution,
            org.onap.so.db.catalog.beans.Service service) {
        CollectionResourceCustomization networkCollection = null;
        int count = 0;
        for (CollectionResourceCustomization collectionCustom : service.getCollectionResourceCustomizations()) {
            if (catalogDbClient.getNetworkCollectionResourceCustomizationByID(
                    collectionCustom.getModelCustomizationUUID()) != null) {
                networkCollection = collectionCustom;
                count++;
            }
        }
        if (count > 1) {
            buildAndThrowException(execution,
                    "Found multiple Network Collections in the Service model, only one per Service is supported.");
        }
        return networkCollection;
    }

    private void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }
}
