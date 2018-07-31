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

package org.onap.so.bpmn.common.scripts

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.DecomposeJsonUtil;
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MsoLogger

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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DecomposeService.class);


	String Prefix="DDS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtils()
	JsonUtils jsonUtils = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("preProcessRequest of DecomposeService ")
		setBasicDBAuthHeader(execution, execution.getVariable('isDebugLogEnabled'))

		try {
			execution.setVariable("prefix", Prefix)
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String serviceModelInfo = execution.getVariable("serviceModelInfo")
			String invariantId
			if(jsonUtils.jsonElementExist(serviceModelInfo, "modelInvariantUuid")){
				invariantId = jsonUtils.getJsonValue(serviceModelInfo, "modelInvariantUuid")
			}else if(jsonUtils.jsonElementExist(serviceModelInfo, "modelInvariantId")){
				invariantId = jsonUtils.getJsonValue(serviceModelInfo, "modelInvariantId")
			}
			execution.setVariable("DDS_serviceModelInvariantId", invariantId)
			execution.setVariable("DDS_serviceModelUuid", jsonUtils.getJsonValue(serviceModelInfo, "modelUuid"))
			execution.setVariable("DDS_modelVersion", jsonUtils.getJsonValue(serviceModelInfo, "modelVersion"))
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessRequest of DecomposeService ")
	}

	public void queryCatalogDb (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("queryCatalogDB of DecomposeService ")

		try {

			// check for input
			String serviceModelInvariantId = execution.getVariable("DDS_serviceModelInvariantId")
			String serviceModelUuid = execution.getVariable("DDS_serviceModelUuid")
			String modelVersion = execution.getVariable("DDS_modelVersion")

			msoLogger.debug("serviceModelInvariantId: " + serviceModelInvariantId)
			msoLogger.debug("modelVersion: " + modelVersion)

			JSONObject catalogDbResponse = null
            if(serviceModelUuid != null && serviceModelUuid.length() > 0)
                catalogDbResponse = catalogDbUtils.getServiceResourcesByServiceModelUuid(execution, serviceModelUuid, "v2")
            else if (modelVersion != null && modelVersion.length() > 0)
				catalogDbResponse = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidAndServiceModelVersion(execution, serviceModelInvariantId, modelVersion, "v2")
			else
				catalogDbResponse = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuid(execution, serviceModelInvariantId, "v2")

			if (catalogDbResponse == null || catalogDbResponse.toString().equalsIgnoreCase("null")) {
				msg = "No data found in Catalog DB"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}

			String catalogDbResponseString = catalogDbResponse.toString()

			execution.setVariable("DDS_catalogDbResponse", catalogDbResponseString)
			msoLogger.debug("catalog DB response string: "+ catalogDbResponseString)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in queryCatalogDb " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit queryCatalogDb of DecomposeService ")
	}



	public void actuallyDecomposeService (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("actuallyDecomposeService of DecomposeService ")

		try {

			// check for input
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String serviceModelInvariantId = execution.getVariable("DDS_serviceModelInvariantId")

			msoLogger.debug("serviceModelInvariantId: " + serviceModelInvariantId)

			msoLogger.debug("getting service decomposition")

			String catalogDbResponse = execution.getVariable("DDS_catalogDbResponse")
			ServiceDecomposition serviceDecomposition = DecomposeJsonUtil.jsonToServiceDecomposition(catalogDbResponse, serviceInstanceId)

			execution.setVariable("serviceDecomposition", serviceDecomposition)
			execution.setVariable("serviceDecompositionString", serviceDecomposition.toJsonString())

			msoLogger.debug("service decomposition: "+ serviceDecomposition.toJsonString())

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in actuallyDecomposeService " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit actuallyDecomposeService of DecomposeService ")
	}

}
