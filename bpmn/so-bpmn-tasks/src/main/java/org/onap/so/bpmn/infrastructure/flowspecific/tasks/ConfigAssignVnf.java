/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
 * Copyright (C) 2019 Nokia.
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.flowspecific.exceptions.VnfNotFoundException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.ConfigAssignPropertiesForVnf;
import org.onap.so.client.cds.beans.ConfigAssignRequestVnf;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Get vnf related data and config assign
 */
@Component
public class ConfigAssignVnf {

    private static final Logger logger = LoggerFactory.getLogger(ConfigAssignVnf.class);
    private static final String ORIGINATOR_ID = "SO";
    private static final String ACTION_NAME = "config-assign";
    private static final String MODE = "sync";
    private static final ObjectMapper objectMapper`=new ObjectMapper();

    private final ExtractPojosForBB extractPojosForBB;
    private final ExceptionBuilder exceptionBuilder;

    @Autowired
    public ConfigAssignVnf(ExtractPojosForBB extractPojosForBB, ExceptionBuilder exceptionBuilder) {
        this.extractPojosForBB = extractPojosForBB;
        this.exceptionBuilder = exceptionBuilder;
    }

    /**
     * Getting the vnf data, blueprint name, blueprint version etc and setting them in execution object and calling the
     * subprocess.
     */
    public void preProcessAbstractCDSProcessing(BuildingBlockExecution execution) {
        logger.info("Start preProcessAbstractCDSProcessing ");
        try {
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            ConfigAssignPropertiesForVnf configAssignPropertiesForVnf = new ConfigAssignPropertiesForVnf();
            configAssignPropertiesForVnf.setServiceInstanceId(serviceInstance.getServiceInstanceId());
            configAssignPropertiesForVnf
                    .setServiceModelUuid(serviceInstance.getModelInfoServiceInstance().getModelUuid());
            configAssignPropertiesForVnf
                    .setVnfCustomizationUuid(genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid());
            configAssignPropertiesForVnf.setVnfId(genericVnf.getVnfId());
            configAssignPropertiesForVnf.setVnfName(genericVnf.getVnfName());
            setUserParamsInConfigAssignPropertiesForVnf(configAssignPropertiesForVnf,
                    execution.getGeneralBuildingBlock().getRequestContext().getRequestParameters().getUserParams(),
                    genericVnf);
            ConfigAssignRequestVnf configAssignRequestVnf = new ConfigAssignRequestVnf();
            configAssignRequestVnf.setResolutionKey(genericVnf.getVnfName());
            configAssignRequestVnf.setConfigAssignPropertiesForVnf(configAssignPropertiesForVnf);

            String blueprintName = genericVnf.getModelInfoGenericVnf().getBlueprintName();
            String blueprintVersion = genericVnf.getModelInfoGenericVnf().getBlueprintVersion();
            logger.debug(" BlueprintName : " + blueprintName + " BlueprintVersion : " + blueprintVersion);

            AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();

            abstractCDSPropertiesBean.setBlueprintName(blueprintName);
            abstractCDSPropertiesBean.setBlueprintVersion(blueprintVersion);
            abstractCDSPropertiesBean.setRequestObject(configAssignRequestVnf.toString());

            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            abstractCDSPropertiesBean.setOriginatorId(ORIGINATOR_ID);
            abstractCDSPropertiesBean.setRequestId(gBBInput.getRequestContext().getMsoRequestId());
            abstractCDSPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
            abstractCDSPropertiesBean.setActionName(ACTION_NAME);
            abstractCDSPropertiesBean.setMode(MODE);
            execution.setVariable("executionObject", abstractCDSPropertiesBean);
        } catch (Exception ex) {
            logger.error("An exception occurred when creating ConfigAssignPropertiesForVnf for CDS request", ex);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    private void setUserParamsInConfigAssignPropertiesForVnf(ConfigAssignPropertiesForVnf configAssignProperties,
            List<Map<String, Object>> userParamsFromRequest, GenericVnf vnf) throws Exception {
        Service service = getServiceFromRequestUserParams(userParamsFromRequest);
        List<Map<String, String>> instanceParamsList =
                getInstanceParamForVnf(service, vnf.getModelInfoGenericVnf().getModelCustomizationUuid());
        instanceParamsList
                .forEach(instanceParamsMap -> instanceParamsMap.forEach(configAssignProperties::setUserParam));
    }

    private Service getServiceFromRequestUserParams(List<Map<String, Object>> userParams) throws Exception {
        Map<String, Object> serviceMap = userParams.stream().filter(key -> key.containsKey("service")).findFirst()
                .orElseThrow(() -> new Exception("Can not find service in userParams section in generalBuildingBlock"));
        return getServiceObjectFromServiceMap(serviceMap);
    }

    private Service getServiceObjectFromServiceMap(Map<String, Object> serviceMap) throws IOException {
        String serviceFromJson = objectMapper.writeValueAsString(serviceMap.get("service"));
        try {
            return objectMapper.readValue(serviceFromJson, Service.class);
        } catch (IOException e) {
            logger.error(String.format(
                    "An exception occurred while converting json object to Service object. The json is: %s",
                    serviceFromJson), e);
            throw e;
        }
    }

    private List<Map<String, String>> getInstanceParamForVnf(Service service, String genericVnfModelCustomizationUuid)
            throws VnfNotFoundException {
        Optional<Vnfs> foundedVnf = service.getResources().getVnfs().stream()
                .filter(vnfs -> vnfs.getModelInfo().getModelCustomizationId().equals(genericVnfModelCustomizationUuid))
                .findFirst();
        if (foundedVnf.isPresent()) {
            return foundedVnf.get().getInstanceParams();
        } else {
            throw new VnfNotFoundException(genericVnfModelCustomizationUuid);
        }
    }
}
