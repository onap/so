/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.scripts

import org.apache.commons.collections.map.HashedMap
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.serviceinstancebeans.ModelInfo
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.apache.commons.lang3.StringUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import javax.ws.rs.core.Response
import org.onap.so.bpmn.common.scripts.OofUtils



class DoAllocateCoreNSSI extends AbstractServiceTaskProcessor {

	private static final Logger logger = LoggerFactory.getLogger( DoAllocateCoreNSSI.class);
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
	JsonUtils jsonUtil = new JsonUtils()
	ObjectMapper mapper = new ObjectMapper()

	OofUtils oofUtils = new OofUtils()

	public void preProcessRequest(DelegateExecution execution) {
		
		logger.debug("**** Enter DoAllocateCoreNSSI ::: preProcessRequest ****")

		String msg = ""
		//Get SliceProfile from sliceParams JSON
		String sliceProfile = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "sliceProfile")
		if (isBlank(sliceProfile)) {
			msg = "Slice Profile is null"
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		} else {
			execution.setVariable("sliceProfile", sliceProfile)
		}
		
		logger.debug("**** Exit DoAllocateCoreNSSI ::: preProcessRequest ****")
	}

	/**
	 * Query NSST name from CatalogDB
	 * @param execution
	 */

	void getNSSTName(DelegateExecution execution){
		logger.debug("**** Enter DoAllocateCoreNSSI ::: getNSSTName ****")

		String nsstModelInvariantUuid = execution.getVariable("modelInvariantUuid")

		try{

			String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution, nsstModelInvariantUuid)
			logger.debug("***** JSON Response is: "+json)

			String nsstName = jsonUtil.getJsonValue(json, "serviceResources.modelInfo.modelName") ?: ""
			String networkServiceModelInfo = jsonUtil.getJsonValue(json, "serviceResources.serviceProxy.modelInfo") ?: ""
			
			logger.debug("***** nsstName is: "+ nsstName)
			execution.setVariable("nsstName",nsstName)
		}catch(BpmnError e){
			throw e
		} catch (Exception ex){
			String msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}

		logger.debug("**** Exit DoAllocateCoreNSSI ::: getNSSTName ****")
	}


	void prepareOOFRequest(DelegateExecution execution){

		logger.debug("**** Enter DoAllocateCoreNSSI ::: prepareOOFRequest ****")

		//API Path
		String apiPath =  "/api/oof/selection/nssi/v1"

		logger.debug("API path for DoAllocateCoreNSSI: "+apiPath)

		execution.setVariable("apiPath", apiPath)                               

		//Setting correlator as requestId
		String requestId = execution.getVariable("msoRequestId")
		execution.setVariable("correlator", requestId)                          

		//Setting messageType for all Core slice as cn
		String messageType = "cn"
		execution.setVariable("messageType", messageType)

		//Is there any specific timeout we have to set or else we don't need to send
		//if blank will be set default value in DoHandleOofRequest
		String timeout = "PT30M"                                                  
		execution.setVariable("timeout", timeout)


		Map<String, Object> profileInfo = mapper.readValue(execution.getVariable("sliceProfile"), Map.class)
		String nsstModelUuid = execution.getVariable("modelUuid")
		String nsstModelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String nsstName = execution.getVariable("nsstName")
		String oofRequest = oofUtils.buildSelectNSSIRequest(requestId, messageType, nsstModelUuid, nsstModelInvariantUuid, nsstName, profileInfo)     

		logger.debug("**** OOfRequest for Core Slice: "+oofRequest)
		execution.setVariable("oofRequest", oofRequest)

		logger.debug("**** Exit DoAllocateCoreNSSI ::: prepareOOFRequest ****")

	}

	public void processOOFAsyncResponse(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreNSSI ::: processOOFAsyncResponse ****")

		Response httpResponse=execution.getVariable("WorkflowResponse")
		String OOFResponse = httpResponse.readEntity(String.class)

		logger.debug("NSSI OOFResponse is: " + OOFResponse)

		execution.setVariable("OOFResponse", OOFResponse)
		
		String solutions = jsonUtil.getJsonValue(OOFResponse, "solutions")

		execution.setVariable("solutions", solutions)                  

		logger.debug("**** Exit DoAllocateCoreNSSI ::: processOOFAsyncResponse ****")
	}
}