/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 TechMahindra
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

import java.util.Map;
import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

class AbstractCDSProcessor extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AbstractCDSProcessor.class);


	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest (DelegateExecution execution) {
	    def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		msoLogger.debug("STARTED preProcessRequest Process")
		
		def prefix="ACPREST_"
		execution.setVariable("prefix", prefix)
		setSuccessIndicator(execution, false)
		
		def requestId = execution.getVariable("requestId")
		execution.setVariable("ACPREST_requestId", requestId)
		msoLogger.debug("requestId : " + requestId)
		
		def originatorId = execution.getVariable("originatorId")
		execution.setVariable("ACPREST_originatorId", originatorId)
		msoLogger.debug("originatorId : " + originatorId)
		
		def subRequestId = execution.getVariable("subRequestId")
		execution.setVariable("ACPREST_subRequestId", subRequestId)
		msoLogger.debug("subRequestId : " + subRequestId)
		
		def actionName = execution.getVariable("actionName")
		execution.setVariable("ACPREST_actionName", actionName)
		msoLogger.debug("actionName : " + actionName)
		
		def mode = execution.getVariable("mode")
		execution.setVariable("ACPREST_mode", mode)
		msoLogger.debug("mode : " + mode)
		
		def blueprintName = execution.getVariable("blueprintName")
		execution.setVariable("ACPREST_blueprintName", blueprintName)
		msoLogger.debug("blueprintName : " + blueprintName)
		
		def blueprintVersion = execution.getVariable("blueprintVersion")
		execution.setVariable("ACPREST_blueprintVersion", blueprintVersion)
		msoLogger.debug("blueprintVersion : " + blueprintVersion)
		
		def requestObject = execution.getVariable("requestObject")
		execution.setVariable("ACPREST_requestObject", requestObject)
		msoLogger.debug("requestObject : " + requestObject)
		
		msoLogger.debug("Ended preProcessRequest Process")
	}
	
	public void callCDSProcessor (DelegateExecution execution) {
	    def method = getClass().getSimpleName() + '.callCDSProcessor(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		String prefix = execution.getVariable('prefix')
		
        msoLogger.debug("STARTED callCDSProcessor Process")
        
        def requestId = execution.getVariable(prefix +"requestId")
		def originatorId = execution.getVariable(prefix +"originatorId")
		def subRequestId = execution.getVariable(prefix +"subRequestId")
		def actionName = execution.getVariable(prefix +"actionName")
		def mode = execution.getVariable(prefix +"mode")
		def blueprintName = execution.getVariable(prefix +"blueprintName")
		def blueprintVersion = execution.getVariable(prefix +"blueprintVersion")
		def requestObject = execution.getVariable(prefix +"requestObject")

        Map<String,String> parameters  = new HashMap<>();
        parameters.put('requestId', requestId)
        parameters.put('originatorId', originatorId)
        parameters.put('subRequestId', subRequestId)
        parameters.put('actionName', actionName)
        parameters.put('mode', mode)
        parameters.put('blueprintName', blueprintName)
        parameters.put('blueprintVersion', blueprintVersion)
        parameters.put('requestObject', requestObject)
        
        AbstractCDSProcessingBBUtils abstractCDSProcessUtils = new AbstractCDSProcessingBBUtils();
        abstractCDSProcessUtils.sendRequestToCDSClient(parameters)
        
        msoLogger.debug("ENDED callCDSProcessor Process")
	}
	
	public void processCDSResponse (DelegateExecution execution) {
	    def method = getClass().getSimpleName() + '.processCDSResponse(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
	    msoLogger.debug("STARTED processCDSResponse Process")
	    
	    
	    msoLogger.debug("ENDED processCDSResponse Process")
	}
}
