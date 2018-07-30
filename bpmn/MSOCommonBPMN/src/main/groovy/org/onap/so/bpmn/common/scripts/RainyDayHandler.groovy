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

package org.onap.so.bpmn.common.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.policy.PolicyClient
import org.onap.so.client.policy.PolicyClientImpl
import org.onap.so.client.policy.entities.DictionaryData
import org.onap.so.client.policy.entities.PolicyDecision
import org.onap.so.client.policy.entities.Treatments
import org.onap.so.logger.MsoLogger


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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, RainyDayHandler.class);


	String Prefix="RDH_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	
	JsonUtils jsonUtils = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("preProcessRequest of RainyDayHandler ")

		try {
			execution.setVariable("prefix", Prefix)
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			msoLogger.debug("msoRequestId is: " + requestId)		
			def serviceType = execution.getVariable("serviceType")
			msoLogger.debug("serviceType is: " + serviceType)
			def vnfType = execution.getVariable("vnfType")
			msoLogger.debug("vnftype is: " + vnfType)
			def currentActivity = execution.getVariable("currentActivity")
			msoLogger.debug("currentActivity is: " + currentActivity)
			def workStep = execution.getVariable("workStep")
			msoLogger.debug("workStep is: " + workStep)
			def failedActivity = execution.getVariable("failedActivity")
			msoLogger.debug("failedActivity is: " + failedActivity)
			def errorCode = execution.getVariable("errorCode")
			msoLogger.debug("errorCode is: " + errorCode)
			def errorText = execution.getVariable("errorText")
			msoLogger.debug("errorText is: " + errorText)
			String defaultPolicyDisposition = (String) UrnPropertiesReader.getVariable("policy.default.disposition",execution)
			msoLogger.debug("defaultPolicyDisposition is: " + defaultPolicyDisposition)
			execution.setVariable('defaultPolicyDisposition', defaultPolicyDisposition)
			
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessRequest of RainyDayHandler ")
	}

	public void queryPolicy (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("queryPolicy of RainyDayHandler ")

		try {

			// check for input
			String serviceType = execution.getVariable("serviceType")
			String vnfType = execution.getVariable("vnfType")
			
			msoLogger.debug("serviceType: " + serviceType)
			msoLogger.debug("vnfType: " + vnfType)
			
			def errorCode = execution.getVariable("errorCode")
			def bbId = execution.getVariable("currentActivity")
			def workStep = execution.getVariable("workStep")
			
			msoLogger.debug("Before querying policy")
			
			String decision = 'DENY'
			String disposition = "Abort"
			String defaultAllowedTreatments = "rollback, skip, manual, abort"
			
			String defaultPolicyDisposition = (String) execution.getVariable('defaultPolicyDisposition')
			if (defaultPolicyDisposition != null && !defaultPolicyDisposition.isEmpty()) {
				msoLogger.debug("Setting disposition to the configured default instead of querying Policy: " + defaultPolicyDisposition)
				disposition = defaultPolicyDisposition
				msoLogger.debug("Setting default allowed treatments: " + defaultAllowedTreatments)
				execution.setVariable("validResponses", defaultAllowedTreatments)
			}
			else {
			
				PolicyDecision decisionObject = null
			
				try {			
					PolicyClient policyClient = new PolicyClientImpl()
					msoLogger.debug("Created policy client")
					decisionObject = policyClient.getDecision(serviceType, vnfType, bbId, workStep, errorCode)
					msoLogger.debug("Obtained decision object")
					DictionaryData dictClient = policyClient.getAllowedTreatments(bbId, workStep)					
					Treatments treatments = dictClient.getTreatments()
					String validResponses = treatments.getString()
					if (validResponses != null) {
						validResponses = validResponses.toLowerCase()
					}
					msoLogger.debug("Obtained validResponses: " + validResponses)
					execution.setVariable("validResponses", validResponses)
				
				} catch(Exception e) {
					msg = "Exception in queryPolicy " + e.getMessage()
					msoLogger.debug(msg)				
				}
			
						
				if (decisionObject != null) {
					decision = decisionObject.getDecision()
					disposition = decisionObject.getDetails()
					msoLogger.debug("Obtained disposition from policy engine: " + disposition)
				}
				else {
					disposition = "Abort"
				}
				if (disposition == null) {
					disposition = "Abort"
				}
			}			
			execution.setVariable("handlingCode", disposition)			
			
			msoLogger.debug("Disposition: "+ disposition)

		} catch (BpmnError e) {
			msoLogger.debug("BPMN exception: " + e.errorMessage)
			throw e;
		} catch (Exception ex){
			msg = "Exception in queryPolicy " + ex.getMessage()
			msoLogger.debug(msg)
			//exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit queryPolicy of RainyDayHandler ")
	}



	
}
