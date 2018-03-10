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
package org.openecomp.mso.bpmn.common.scripts

import org.openecomp.mso.bpmn.core.json.DecomposeJsonUtil

import static org.apache.commons.lang3.StringUtils.*


import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.json.JsonUtils

import com.att.ecomp.mso.bpmn.core.domain.*

import groovy.json.*

/**
 * This groovy class supports the <class>DecomposeService.bpmn</class> process.
 *
 * @author
 *
 * Inputs:
 * @param - msoRequestId
 * @param - isDebugLogEnabled
 * @param - serviceInstanceId
 * @param - serviceModelInfo
 * @param - requestParameters (may be null)
 *
 * Outputs:
 * @param - rollbackData (null)
 * @param - rolledBack (null)
 * @param - WorkflowException
 * @param - serviceDecomposition
 *
 */
public class DecomposeService extends AbstractServiceTaskProcessor {

	String Prefix="DDS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtils()
	JsonUtils jsonUtils = new JsonUtils()

	public void preProcessRequest (Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest of DecomposeService *****",  isDebugEnabled)
		setBasicDBAuthHeader(execution, isDebugEnabled)

		try {
			execution.setVariable("prefix", Prefix)
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String serviceModelInfo = execution.getVariable("serviceModelInfo")
			execution.setVariable("DDS_serviceModelInvariantId", jsonUtils.getJsonValue(serviceModelInfo, "modelInvariantUuid"))
			execution.setVariable("DDS_serviceModelUuid", jsonUtils.getJsonValue(serviceModelInfo, "modelUuid"))
			execution.setVariable("DDS_modelVersion", jsonUtils.getJsonValue(serviceModelInfo, "modelVersion"))
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest of DecomposeService *****",  isDebugEnabled)
	}

	public void queryCatalogDb (Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** queryCatalogDB of DecomposeService *****",  isDebugEnabled)

		try {

			// check for input
			String serviceModelInvariantId = execution.getVariable("DDS_serviceModelInvariantId")
			String serviceModelUuid = execution.getVariable("DDS_serviceModelUuid")
			String modelVersion = execution.getVariable("DDS_modelVersion")

			utils.log("DEBUG", "serviceModelInvariantId: " + serviceModelInvariantId, isDebugEnabled)
			utils.log("DEBUG", "modelVersion: " + modelVersion, isDebugEnabled)

			JSONObject catalogDbResponse = null
            if(serviceModelUuid != null && serviceModelUuid.length() > 0)
                catalogDbResponse = catalogDbUtils.getServiceResourcesByServiceModelUuid(execution, serviceModelUuid, "v2")
            else if (modelVersion != null && modelVersion.length() > 0)
				catalogDbResponse = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidAndServiceModelVersion(execution, serviceModelInvariantId, modelVersion, "v2")
			else
				catalogDbResponse = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuid(execution, serviceModelInvariantId, "v2")
			String catalogDbResponseString = catalogDbResponse.toString()

			execution.setVariable("DDS_catalogDbResponse", catalogDbResponseString)
			utils.log("DEBUG", "catalog DB response string: "+ catalogDbResponseString, isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in queryCatalogDb " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit queryCatalogDb of DecomposeService *****",  isDebugEnabled)
	}



	public void actuallyDecomposeService (Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** actuallyDecomposeService of DecomposeService *****",  isDebugEnabled)

		try {

			// check for input
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String serviceModelInvariantId = execution.getVariable("DDS_serviceModelInvariantId")

			utils.log("DEBUG", "serviceModelInvariantId: " + serviceModelInvariantId, isDebugEnabled)

			utils.log("DEBUG", "getting service decomposition", isDebugEnabled)

			String catalogDbResponse = execution.getVariable("DDS_catalogDbResponse")
			ServiceDecomposition serviceDecomposition = DecomposeJsonUtil.jsonToServiceDecomposition(catalogDbResponse, serviceInstanceId)

			execution.setVariable("serviceDecomposition", serviceDecomposition)
			execution.setVariable("serviceDecompositionString", serviceDecomposition.toJsonString())

			utils.log("DEBUG", "service decomposition: "+ serviceDecomposition.toJsonString(), isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in actuallyDecomposeService " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit actuallyDecomposeService of DecomposeService *****",  isDebugEnabled)
	}

}
