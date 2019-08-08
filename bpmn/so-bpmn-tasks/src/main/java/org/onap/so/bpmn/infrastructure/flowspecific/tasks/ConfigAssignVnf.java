/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
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
import org.onap.so.bpmn.common.BuildingBlockExecution;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 
 * Get vnf related data and config assign
 *
 */
@Component
public class ConfigAssignVnf {

    private static final Logger logger = LoggerFactory.getLogger(ConfigAssignVnf.class);
    private static final String ORIGINATOR_ID = "SO";
    private static final String ACTION_NAME = "config-assign";
    private static final String MODE = "sync";
    private static final String SERVICE = "service";

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    /**
     * Getting the vnf data, blueprint name, blueprint version etc and setting them in execution object and calling the
     * subprocess.
     * 
     * @param execution
     */
    public void preProcessAbstractCDSProcessing(BuildingBlockExecution execution) {
        logger.info("Start preProcessAbstractCDSProcessing ");
        List<Map<String, String>> userParameters = new ArrayList<>();
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);

            Optional<Map<String, Object>> requestDetails =
                    execution.getGeneralBuildingBlock()
                            .getRequestContext()
                            .getRequestParameters()
                            .getUserParams().stream()
                            .filter(x -> x.containsKey(SERVICE))
                            .findFirst();

            if (requestDetails.isPresent()) {
                ObjectMapper mapper = new ObjectMapper();
                String input = mapper.writeValueAsString(requestDetails.get().get(SERVICE));
                Service service = mapper.readValue(input, Service.class);

                service.getResources()
                        .getVnfs()
                        .stream()
                        .map(Vnfs::getInstanceParams)
                        .forEach(userParameters::addAll);
            }

            ConfigAssignPropertiesForVnf configAssignPropertiesForVnf = new ConfigAssignPropertiesForVnf();
            configAssignPropertiesForVnf.setServiceInstanceId(serviceInstance.getServiceInstanceId());
            configAssignPropertiesForVnf
                    .setServiceModelUuid(serviceInstance.getModelInfoServiceInstance().getModelUuid());
            configAssignPropertiesForVnf
                    .setVnfCustomizationUuid(vnf.getModelInfoGenericVnf().getModelCustomizationUuid());
            configAssignPropertiesForVnf.setVnfId(vnf.getVnfId());
            configAssignPropertiesForVnf.setVnfName(vnf.getVnfName());

            userParameters.stream()
                          .flatMap(params -> params.entrySet().stream())
                          .forEach(entry -> configAssignPropertiesForVnf.setUserParam(entry.getKey(), entry.getValue()));

            ConfigAssignRequestVnf configAssignRequestVnf = new ConfigAssignRequestVnf();
            configAssignRequestVnf.setResolutionKey(vnf.getVnfName());
            configAssignRequestVnf.setConfigAssignPropertiesForVnf(configAssignPropertiesForVnf);

            String blueprintName = vnf.getModelInfoGenericVnf().getBlueprintName();
            String blueprintVersion = vnf.getModelInfoGenericVnf().getBlueprintVersion();
            logger.debug(" BlueprintName :  {}  BlueprintVersion :  {} " , blueprintName,  blueprintVersion);

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
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
