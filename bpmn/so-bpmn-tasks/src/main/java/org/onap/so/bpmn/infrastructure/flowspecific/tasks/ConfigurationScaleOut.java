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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.appc.payload.beans.ConfigScaleOutPayload;
import org.onap.so.bpmn.appc.payload.beans.RequestParametersConfigScaleOut;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@Component
public class ConfigurationScaleOut {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ConfigurationScaleOut.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private CatalogDbClient catalogDbClient;
	@Autowired
	private ApplicationControllerAction appCClient;
	private static final String ACTION = "action";
	private static final String MSO_REQUEST_ID = "msoRequestId";
	private static final String VNF_ID = "vnfId";
	private static final String VNF_NAME = "vnfName";
	private static final String VFMODULE_ID = "vfModuleId";
	private static final String CONTROLLER_TYPE = "controllerType";
	private static final String PAYLOAD = "payload";
	
	public void setParamsForConfigurationScaleOut(BuildingBlockExecution execution) {
		
		GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
		
		try {
			List<Map<String, String>> jsonPathForCfgParams = gBBInput.getRequestContext().getConfigurationParameters();
			String key = null;
			String paramValue = null;
			ObjectMapper mapper = new ObjectMapper();
			String configScaleOutPayloadString = null;
			ControllerSelectionReference controllerSelectionReference;
			ConfigScaleOutPayload configPayload = new ConfigScaleOutPayload();
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			String vnfId = vnf.getVnfId();
			String vnfName = vnf.getVnfName();	
			String vnfType = vnf.getVnfType();
			String actionCategory = Action.ConfigScaleOut.toString();
			controllerSelectionReference = catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(vnfType, actionCategory);
			String controllerName = controllerSelectionReference.getControllerName();
			
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			String sdncVfModuleQueryResponse = execution.getVariable("SDNCQueryResponse_" + vfModule.getVfModuleId());
			
			Map<String, String> paramsMap = new HashMap<>();
			RequestParametersConfigScaleOut requestParameters = new RequestParametersConfigScaleOut();
			String configScaleOutParam = null;
			if (jsonPathForCfgParams != null) {
				for (Map<String, String> param : jsonPathForCfgParams) {
					for (Map.Entry<String,String> entry : param.entrySet()) {
						key = entry.getKey();
						paramValue = entry.getValue();
						configScaleOutParam = JsonPath.parse(sdncVfModuleQueryResponse).read(paramValue);
						if(configScaleOutParam != null){
							paramsMap.put(key, configScaleOutParam);
						}
					}
				}
			}
			requestParameters.setVfModuleId(vfModule.getVfModuleId());
			requestParameters.setVnfHostIpAddress(vnf.getIpv4OamAddress());
			configPayload.setConfigurationParameters(paramsMap);
			configPayload.setRequestParameters(requestParameters);
			configScaleOutPayloadString = mapper.writeValueAsString(configPayload);
			configScaleOutPayloadString = configScaleOutPayloadString.replaceAll("\"", "\\\\\"");
			
			execution.setVariable(ACTION, actionCategory);
			execution.setVariable(MSO_REQUEST_ID, gBBInput.getRequestContext().getMsoRequestId());
			execution.setVariable(VNF_ID, vnfId);
			execution.setVariable(VNF_NAME, vnfName);
			execution.setVariable(VFMODULE_ID, vfModule.getVfModuleId());
			execution.setVariable(CONTROLLER_TYPE, controllerName);
			execution.setVariable(PAYLOAD, configScaleOutPayloadString);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void callAppcClient(BuildingBlockExecution execution) {
		try{
			Action commandAction = Action.valueOf(execution.getVariable(ACTION));
			String msoRequestId = execution.getVariable(MSO_REQUEST_ID);
			String vnfId = execution.getVariable(VNF_ID);
			Optional<String> payloadString = null;
			if(execution.getVariable(PAYLOAD) != null){
				String pay = execution.getVariable(PAYLOAD);
				payloadString =  Optional.of(pay);
			}
			String controllerType = execution.getVariable(CONTROLLER_TYPE);
			HashMap<String, String> payloadInfo = new HashMap<>();
			payloadInfo.put(VNF_NAME, execution.getVariable(VNF_NAME));
			payloadInfo.put(VFMODULE_ID,execution.getVariable(VFMODULE_ID));
			//PayloadInfo contains extra information that adds on to payload before making request to appc
			appCClient.runAppCCommand(commandAction, msoRequestId, vnfId, payloadString, payloadInfo, controllerType);
		}catch(Exception ex){
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
