/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.UriBuilder
import org.apache.commons.lang.StringUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.PreconditionFailedException
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank

class AllottedResourceUtils {
    private static final Logger logger = LoggerFactory.getLogger( AllottedResourceUtils.class);


	private AbstractServiceTaskProcessor taskProcessor
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	MsoUtils utils;

	public AllottedResourceUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
		this.utils = taskProcessor.utils
	}

	/*Used on Create - called from DoCreate
	* Using Consuming ServiceInstanceId get related Allotted Resources Orchestration status from AAI
	* 1) get related AR links for CSI 2) get AR from AR links
	* return: null -> AR Not found
	* return: " " -> AR found with empty orchStatus
	* return: orchStatus - > AR found with this orchStatus
	* setsVariable aaiARGetResponse
	*/
	public String getAROrchStatus (DelegateExecution execution) {

		logger.trace("getAROrchStatus ")
		String msg = ""
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		String arType = execution.getVariable("allottedResourceType")
		String arRole = execution.getVariable("allottedResourceRole")
		String siXml = execution.getVariable("CSI_service")
		String orchStatus = null
		XmlParser xmlParser = new XmlParser()
		logger.debug("getAROrchStatus siXml:" + siXml)
		try {
			if (!isBlank(siXml)) {
				def groovy.util.Node siNode = xmlParser.parseText(siXml)
				def groovy.util.Node relationshipList = utils.getChildNode(siNode, 'relationship-list')
				if (relationshipList != null) {
					def groovy.util.NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
					for (groovy.util.Node relationship in relationships) {
						def groovy.util.Node relatedTo = utils.getChildNode(relationship, 'related-to')
						if ((relatedTo != null) && (relatedTo.text().equals('allotted-resource'))) {
							logger.debug("getARORchStatus AR found")
							def groovy.util.Node relatedLink = utils.getChildNode(relationship, 'related-link')
							if (relatedLink != null){
								Optional<AllottedResource> ar = getARbyLink(execution, relatedLink.text(), arRole)
								if (ar.isPresent()){
									orchStatus = execution.getVariable("aaiAROrchStatus")
									break
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			logger.debug(" Error encountered in getAROrchStatus" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in getAROrchStatus" + e.getMessage())
		}
		logger.trace(" Exit getAROrchStatus - OrchStatus:" + orchStatus)
		return orchStatus
	}

	// get Allotted Resource by AllottedResourceId
	// used on Delete - called from doDeleteAR
	// setsVariable aaiARGetResponse
	public boolean ifExistsAR(DelegateExecution execution, String allottedResourceId) {
		logger.trace("ifExistsAR ")
		try {
			AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(Types.ALLOTTED_RESOURCE.getFragment(allottedResourceId))
            AAIResultWrapper wrapper = getAAIClient().get(resourceUri)
            Optional<AllottedResource> allottedResource = wrapper.asBean(AllottedResource.class)
            if(allottedResource.isPresent()) {
                setExecutionVariables(execution , allottedResource.get(),resourceUri)
                return true
            }else {
                return false
            }
		}catch(Exception e){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in ifExistsAR" + e.getMessage())
		}
	}

	public String getPSIFmARLink(DelegateExecution execution, String arLink)
	{
		// Path: /aai/{version}/business/customers/customer/{cust}/service-subscriptions/service-subscription/{subs}/service-instances/service-instance/{psiid}/allotted-resources/allotted-resource/{arid}
		logger.trace(" getPSIFmARLink - path:" + arLink)
		String[] split = arLink.split("/service-instance/")
		String[] splitB =  split[1].split("/allotted-resources/")
		String siId = splitB[0]
		logger.trace(" Exit getPSIFmARLink - parentServiceInstanceId:" + siId )
		return siId
	}

	// get Allotted resource using Link
	// used on Create called from getARORchStatus
	// used on Delete called from ifExistsAR
	// setsVariable aaiARPath - used for Patch in create

	public Optional<AllottedResource> getARbyLink (DelegateExecution execution, String link, String role) {
		logger.trace("getARbyLink ")
		Optional<AllottedResource> allottedResource = Optional.empty()
		try {
			logger.debug("GET AR Aai Path is: \n" + link)
			AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.ALLOTTED_RESOURCE, UriBuilder.fromPath(link).build())
			allottedResource = getAAIClient().get(AllottedResource.class,uri);
			if(allottedResource.isPresent()) {
				if (!isBlank(role)) {
					if (role == allottedResource.get().getRole()) {
						setExecutionVariables(execution,allottedResource.get(),uri)
					} else {
						logger.debug("AAI AR does not match input role:" + role)
					}
				} else {
					setExecutionVariables(execution,allottedResource.get(),uri)
				}
			}else{
				logger.debug("GET AR received a Not Found (404) Response")
			}
		}catch(Exception e){
			logger.debug(" Error encountered within GetAaiAR" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in GetAaiAR" + e.getMessage())
		}
		return allottedResource
	}

	public void setExecutionVariables(DelegateExecution execution, AllottedResource ar, AAIResourceUri arUrl) {
		execution.setVariable("aaiARGetResponse", ar)
		execution.setVariable("aaiARPath", arUrl.build().toString())
		execution.setVariable("aaiARResourceVersion", ar.getResourceVersion())
		if (StringUtils.isNotEmpty(ar.getOrchestrationStatus())) {
			execution.setVariable("aaiAROrchStatus", ar.getOrchestrationStatus())
		}
		else
		{
			execution.setVariable("aaiAROrchStatus", " ")
		}
	}

	public void updateAROrchStatus(DelegateExecution execution, String status, String aaiARPath){
		logger.trace("updaAROrchStatus ")
		try{

			AllottedResource allottedResource = new AllottedResource();
			allottedResource.setOrchestrationStatus(status)
			logger.debug('AAI AR URI: ' + aaiARPath)

			AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.ALLOTTED_RESOURCE, UriBuilder.fromPath(aaiARPath).build())
			getAAIClient().update(uri,allottedResource)
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception in updateAR.", "BPMN", ErrorCode.UnknownError.getValue(), e.getMessage());
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, 'Internal Error in updateAROrchStatus.' + e.getMessage())
		}
		logger.trace("Exit updateAROrchStatus ")
	}

	//Sets Variable "wasDeleted"
	public void deleteAR(DelegateExecution execution, String aaiARPath){
		logger.trace(" deleteAR - aaiARPath:" + aaiARPath)
		try {

			AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.ALLOTTED_RESOURCE, UriBuilder.fromPath(aaiARPath).build())
			getAAIClient().delete(uri);
		}catch(NotFoundException ex){
			logger.debug("  Delete AR Received a Not Found (404) Response")
		}catch(PreconditionFailedException ex){
			logger.debug("Delete AR Received a Resource Version Mismatch Error: \n")
			exceptionUtil.buildAndThrowWorkflowException(execution, 412, "DeleteAR Received a resource-version Mismatch Error Response from AAI")
		}catch(Exception e){
			logger.debug(" Error encountered in deleteAR!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Delete AR")
		}
		logger.debug("  Delete AR Received a Good Response")
		execution.setVariable("wasDeleted", "true")
		logger.trace("Exit deleteAR ")
	}

	public void buildAAIErrorResponse(DelegateExecution execution, String response, String errorMessage){
		logger.trace("BuildAAIErrorResponse")

		if((response != null) && (response.contains("Fault") || response.contains("RESTFault"))){
			WorkflowException workflowException = exceptionUtil.MapAAIExceptionToWorkflowException(response, execution)
			execution.setVariable("WorkflowException", workflowException)
		}else{
			exceptionUtil.buildWorkflowException(execution, 500, errorMessage)
		}

		logger.trace("Exit BuildAAIErrorResponse Process")
		throw new BpmnError("MSOWorkflowException")
	}

	public  AAIResourcesClient getAAIClient(){
		return new AAIResourcesClient()
	}

}
