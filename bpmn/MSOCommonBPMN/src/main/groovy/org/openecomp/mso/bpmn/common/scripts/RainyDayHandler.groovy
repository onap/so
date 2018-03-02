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
package org.openecomp.mso.bpmn.common.scripts;

import static org.apache.commons.lang3.StringUtils.*;



import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.client.policy.PolicyClient
import org.openecomp.mso.client.policy.PolicyClientImpl
import org.openecomp.mso.client.policy.entities.DictionaryData
import org.openecomp.mso.client.policy.entities.PolicyDecision
import org.openecomp.mso.client.policy.entities.Treatments
import org.openecomp.mso.client.policy.PolicyRestClient


import com.att.ecomp.mso.bpmn.core.domain.*

import groovy.json.*

/**
 * This groovy class supports the <class>RainyDayHandler.bpmn</class> process.
 *
 * @author
 *
 * Inputs:
 * @param - msoRequestId
 * @param - isDebugLogEnabled
 * @param - serviceType
 * @param - vnfType
 * @param - currentActivity
 * @param - workStep
 * @param - failedActivity
 * @param - errorCode
 * @param - errorText 
 * @param - vnfName
 *
 * Outputs:
 * @param - WorkflowException
 * @param - handlingCode
 *
 */
public class RainyDayHandler extends AbstractServiceTaskProcessor {

	String Prefix="RDH_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	
	JsonUtils jsonUtils = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest of RainyDayHandler *****",  isDebugLogEnabled)

		try {
			execution.setVariable("prefix", Prefix)
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			utils.log("DEBUG", "msoRequestId is: " + requestId, isDebugLogEnabled)		
			def serviceType = execution.getVariable("serviceType")
			utils.log("DEBUG", "serviceType is: " + serviceType, isDebugLogEnabled)
			def vnfType = execution.getVariable("vnfType")
			utils.log("DEBUG", "vnftype is: " + vnfType, isDebugLogEnabled)
			def currentActivity = execution.getVariable("currentActivity")
			utils.log("DEBUG", "currentActivity is: " + currentActivity, isDebugLogEnabled)
			def workStep = execution.getVariable("workStep")
			utils.log("DEBUG", "workStep is: " + workStep, isDebugLogEnabled)
			def failedActivity = execution.getVariable("failedActivity")
			utils.log("DEBUG", "failedActivity is: " + failedActivity, isDebugLogEnabled)
			def errorCode = execution.getVariable("errorCode")
			utils.log("DEBUG", "errorCode is: " + errorCode, isDebugLogEnabled)
			def errorText = execution.getVariable("errorText")
			utils.log("DEBUG", "errorText is: " + errorText, isDebugLogEnabled)
			String defaultPolicyDisposition = (String) execution.getVariable('URN_policy_default_disposition')
			utils.log("DEBUG", "defaultPolicyDisposition is: " + defaultPolicyDisposition, isDebugLogEnabled)
			execution.setVariable('defaultPolicyDisposition', defaultPolicyDisposition)
			
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest of RainyDayHandler *****",  isDebugLogEnabled)
	}

	public void queryPolicy (DelegateExecution execution) {
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** queryPolicy of RainyDayHandler *****",  isDebugLogEnabled)

		try {

			// check for input
			String serviceType = execution.getVariable("serviceType")
			String vnfType = execution.getVariable("vnfType")
			
			utils.log("DEBUG", "serviceType: " + serviceType, isDebugLogEnabled)
			utils.log("DEBUG", "vnfType: " + vnfType, isDebugLogEnabled)
			
			def errorCode = execution.getVariable("errorCode")
			def bbId = execution.getVariable("currentActivity")
			def workStep = execution.getVariable("workStep")
			
			utils.log("DEBUG", "Before querying policy", isDebugLogEnabled)
			
			String decision = 'DENY'
			String disposition = "Abort"
			String defaultAllowedTreatments = "rollback, skip, manual, abort"
			
			String defaultPolicyDisposition = (String) execution.getVariable('defaultPolicyDisposition')
			if (defaultPolicyDisposition != null) {
				utils.log("DEBUG", "Setting disposition to the configured default instead of querying Policy: " + defaultPolicyDisposition, isDebugLogEnabled)
				disposition = defaultPolicyDisposition
				utils.log("DEBUG", "Setting default allowed treatments: " + defaultAllowedTreatments, isDebugLogEnabled)
				execution.setVariable("validResponses", defaultAllowedTreatments)
			}
			else {
			
				PolicyDecision decisionObject = null
			
				try {			
					PolicyClient policyClient = new PolicyClientImpl()
					utils.log("DEBUG", "Created policy client", isDebugLogEnabled)
					decisionObject = policyClient.getDecision(serviceType, vnfType, bbId, workStep, errorCode)
					utils.log("DEBUG", "Obtained decision object", isDebugLogEnabled)
					DictionaryData dictClient = policyClient.getAllowedTreatments(bbId, workStep)					
					Treatments treatments = dictClient.getTreatments()
					String validResponses = treatments.getString()
					if (validResponses != null) {
						validResponses = validResponses.toLowerCase()
					}
					utils.log("DEBUG", "Obtained validResponses: " + validResponses, isDebugLogEnabled)
					execution.setVariable("validResponses", validResponses)
				
				} catch(Exception e) {
					msg = "Exception in queryPolicy " + e.getMessage()
					utils.log("DEBUG", msg, isDebugLogEnabled)				
				}
			
						
				if (decisionObject != null) {
					decision = decisionObject.getDecision()
					disposition = decisionObject.getDetails()
					utils.log("DEBUG", "Obtained disposition from policy engine: " + disposition, isDebugLogEnabled)
				}
				else {
					disposition = "Abort"
				}
				if (disposition == null) {
					disposition = "Abort"
				}
			}			
			execution.setVariable("handlingCode", disposition)			
			
			utils.log("DEBUG", "Disposition: "+ disposition, isDebugLogEnabled)

		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN exception: " + e.errorMessage, isDebugLogEnabled)
			throw e;
		} catch (Exception ex){
			msg = "Exception in queryPolicy " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			//exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit queryPolicy of RainyDayHandler *****",  isDebugLogEnabled)
	}



	
}
