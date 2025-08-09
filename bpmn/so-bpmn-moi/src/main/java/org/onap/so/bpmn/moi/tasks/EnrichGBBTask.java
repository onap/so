/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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


package org.onap.so.bpmn.moi.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.common.InjectExecution;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.OofUtils;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.moi.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EnrichGBBTask {

    private static final String EXECUTE_BB_VAR_NAME = "buildingBlock";
    private static final String GBB_INPUT_VAR_NAME = "gBBInput";

    ExceptionUtil exceptionUtil = new ExceptionUtil();
    JsonUtils jsonUtil = new JsonUtils();

    @Autowired
    CatalogDbClient catalogDbClient;

    InjectExecution injectExecution = new InjectExecution();


    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichGBBTask.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    OofUtils oofUtils = new OofUtils(null);

    public void prepareOofRequest(DelegateExecution execution) throws Exception {

        String msoReqId = (String) execution.getVariable("mso-request-id");


        BuildingBlockExecution gBuildingBlockExecution =
                (BuildingBlockExecution) execution.getVariable("gBuildingBlockExecution");

        DelegateExecutionImpl gbbEx = injectExecution.execute(execution,
                (DelegateExecutionImpl) execution.getVariable("gBuildingBlockExecution"));


        GeneralBuildingBlock generalBuildingBlock = (GeneralBuildingBlock) execution.getVariable("gBBInput");

        List<Map<String, Object>> mapUserParams =
                generalBuildingBlock.getRequestContext().getRequestParameters().getUserParams();

        Attributes attributes = null;

        for (Map<String, Object> userParamData : mapUserParams) {
            if (userParamData.get("nssi") != null) {
                Map<String, Object> mapParam = (Map<String, Object>) userParamData.get("nssi");
                LOGGER.info(">>> mapParam: {}", mapParam);
                attributes = mapper.convertValue(mapParam, Attributes.class);
            }
        }
        // Attributes attributes = new ObjectMapper().convertValue(attrMap, Attributes.class);
        Integer latency = attributes.getSliceProfileList().get(0).getRANSliceSubnetProfile().getLatency();
        Integer areaTrafficCapDL =
                attributes.getSliceProfileList().get(0).getRANSliceSubnetProfile().getAreaTrafficCapDL();


        String requestId = generalBuildingBlock.getRequestContext().getMsoRequestId();

        // API Path
        String apiPath = "/api/oof/v1/selection/nsst";
        LOGGER.debug("API path for NSST Selection: {}", apiPath);
        execution.setVariable("NSST_apiPath", apiPath);

        // Setting correlator as requestId
        execution.setVariable("NSST_correlator", requestId);

        // Setting messageType for all Core slice as an
        String messageType = "an";
        execution.setVariable("NSST_messageType", messageType);

        String timeout = "PT30M";
        execution.setVariable("NSST_timeout", timeout);

        Map<String, Object> profileInfo = new HashMap<>();
        profileInfo.put("latency", latency);
        profileInfo.put("areaTrafficCapDL", areaTrafficCapDL);

        String oofRequest = oofUtils.buildSelectNSTRequest(requestId, messageType, profileInfo);
        LOGGER.debug("**** OOfRequest for NSST Selection: {}", oofRequest);
        execution.setVariable("NSST_oofRequest", oofRequest);
    }


    public void processOOFAsyncResponse(DelegateExecution execution) {
        GeneralBuildingBlock generalBuildingBlock = (GeneralBuildingBlock) execution.getVariable("gBBInput");

        LOGGER.debug(">>>> generalBuildingBlock Initial: {}", generalBuildingBlock);

        LOGGER.debug(" **** Enter EnrichGBB ::: processOOFAsyncResponse ****");
        String OOFResponse = (String) execution.getVariable("NSST_asyncCallbackResponse");
        String requestStatus = jsonUtil.getJsonValue(OOFResponse, "requestStatus");
        LOGGER.debug("NSST OOFResponse is: {}", OOFResponse);
        execution.setVariable("OOFResponse", OOFResponse);
        String solutions = "";
        if (requestStatus.equals("completed")) {
            List solutionsList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(OOFResponse, "solutions"));
            if (solutionsList != null && !solutionsList.isEmpty()) {
                solutions = (String) solutionsList.get(0);
            }
        } else {
            String statusMessage = jsonUtil.getJsonValue(OOFResponse, "statusMessage");
            LOGGER.error("received failed status from oof {}", statusMessage);
            LOGGER.debug("received failed status from oof {}", statusMessage);
        }

        LOGGER.debug(">>>>>> solutions: {}", solutions);

        String nsstId = jsonUtil.getJsonValue(solutions, "UUID");
        LOGGER.info(">>> nsstId:{} ", nsstId);

        Service service = catalogDbClient.getServiceByModelUUID(nsstId);

        LOGGER.info("Service from CatalogDB: {}", service);

        LOGGER.debug(">>> Map Incoming Values to GBB");

        ServiceInstance serviceInstance = generalBuildingBlock.getServiceInstance();

        ModelInfoServiceInstance modelInfoServiceInstanceFromGBB = serviceInstance.getModelInfoServiceInstance();

        if (modelInfoServiceInstanceFromGBB == null) {
            String modelInvariantId = service.getModelInvariantUUID();
            String modelVersion = service.getModelVersion();
            String serviceType = service.getServiceType();
            String serviceRole = service.getServiceRole();
            String controllerActor = service.getControllerActor();
            String blueprintName = service.getBlueprintName();
            String blueprintVersion = service.getBlueprintVersion();

            ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
            modelInfoServiceInstance.setModelUuid("ad2233a2-6e3f-42cf-8c60-04e614031383");
            modelInfoServiceInstance.setModelInvariantUuid("38730fb9-bfbb-4a78-88f8-b4f6823197b6");
            modelInfoServiceInstance.setModelVersion("1.0");
            modelInfoServiceInstance.setServiceRole("AN");
            modelInfoServiceInstance.setServiceType("eMBB");
            modelInfoServiceInstance.setBlueprintVersion("1.0.0");
            modelInfoServiceInstance.setControllerActor("CDS");
            modelInfoServiceInstance.setBlueprintName("Hello_World_CBA");
            modelInfoServiceInstance.setServiceType(serviceType);
            modelInfoServiceInstance.setServiceRole(serviceRole);
            modelInfoServiceInstance.setControllerActor(controllerActor);
            modelInfoServiceInstance.setBlueprintName(blueprintName);
            modelInfoServiceInstance.setBlueprintVersion(blueprintVersion);
            modelInfoServiceInstance.setModelInvariantUuid(modelInvariantId);
            modelInfoServiceInstance.setModelUuid(nsstId);
            modelInfoServiceInstance.setModelVersion(modelVersion);

            serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

            serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);


        }

        LOGGER.info(">>> ServiceInstance: {}", serviceInstance);

        generalBuildingBlock.setServiceInstance(serviceInstance);

        LOGGER.debug("generalBuildingBlock: {}", generalBuildingBlock);

        execution.setVariable(GBB_INPUT_VAR_NAME, generalBuildingBlock);
        BuildingBlockExecution gBuildingBlockExecution1 = new DelegateExecutionImpl(execution);
        execution.setVariable("gBuildingBlockExecution", gBuildingBlockExecution1);

        // execution.setVariable("gBuildingBlockExecution", gBBExecution);
        LOGGER.debug(" **** Exit EnrichBB ::: processOOFAsyncResponse ****");
    }
}
