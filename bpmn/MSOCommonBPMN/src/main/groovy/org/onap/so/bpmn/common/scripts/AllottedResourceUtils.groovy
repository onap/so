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

import javax.ws.rs.NotFoundException

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse;



class AllottedResourceUtils {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AllottedResourceUtils.class);


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

		msoLogger.trace("getAROrchStatus ")
		String msg = ""
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		String arType = execution.getVariable("allottedResourceType")
		String arRole = execution.getVariable("allottedResourceRole")
		String siXml = execution.getVariable("CSI_service")
		String ar = null
		String orchStatus = null
		XmlParser xmlParser = new XmlParser()
		msoLogger.debug("getAROrchStatus siXml:" + siXml)
		try {
			if (!isBlank(siXml)) {
				def groovy.util.Node siNode = xmlParser.parseText(siXml)
				def groovy.util.Node relationshipList = utils.getChildNode(siNode, 'relationship-list')
				if (relationshipList != null) {
					def groovy.util.NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
					for (groovy.util.Node relationship in relationships) {
						def groovy.util.Node relatedTo = utils.getChildNode(relationship, 'related-to')
						if ((relatedTo != null) && (relatedTo.text().equals('allotted-resource'))) {
							msoLogger.debug("getARORchStatus AR found")
							def groovy.util.Node relatedLink = utils.getChildNode(relationship, 'related-link')
							if (relatedLink != null){
								ar = getARbyLink(execution, relatedLink.text(), arRole)
								if (!isBlank(ar))
								{
									orchStatus = execution.getVariable("aaiAROrchStatus")
									break
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			msoLogger.debug(" Error encountered in getAROrchStatus" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in getAROrchStatus" + e.getMessage())
		}
		msoLogger.trace(" Exit getAROrchStatus - OrchStatus:" + orchStatus)
		return orchStatus
	}

	// get Allotted Resource by AllottedResourceId
	// used on Delete - called from doDeleteAR
	// setsVariable aaiARGetResponse
	public String getARbyId (DelegateExecution execution, String allottedResourceId) {
		msoLogger.trace("getARbyId ")
		AAIResourceUri arLink = getARLinkbyId(execution, allottedResourceId)
		String ar = null
		if (!isBlank(arLink))
		{
			ar = getARbyLink(execution, arLink, "")
		}
		msoLogger.trace(" Exit GetARbyId - AR:" + ar)
		return ar;
	}

	public String getPSIFmARLink(DelegateExecution execution, String arLink)
	{
		// Path: /aai/{version}/business/customers/customer/{cust}/service-subscriptions/service-subscription/{subs}/service-instances/service-instance/{psiid}/allotted-resources/allotted-resource/{arid}
		msoLogger.trace(" getPSIFmARLink - path:" + arLink)
		String[] split = arLink.split("/service-instance/")
		String[] splitB =  split[1].split("/allotted-resources/")
		String siId = splitB[0]
		msoLogger.trace(" Exit getARLinkbyId - parentServiceInstanceId:" + siId )
		return siId
	}

	// get Allotted Resource Link by AllottedResourceId using Nodes Query
	// used on Delete - called from getARbyId
	public String getARLinkbyId (DelegateExecution execution, String allottedResourceId) {
		msoLogger.trace("getARLinkbyId ")
		String arLink = null
		try {
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.ALLOTTED_RESOURCE, allottedResourceId)
			AaiUtil aaiUtil = new AaiUtil()
			arLink = aaiUtil.createAaiUri(uri)
		} catch (NotFoundException e) {
			msoLogger.debug("GET AR received a Not Found (404) Response")
		} catch(Exception e){
			msoLogger.debug(" Error encountered within GetAaiAR" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in GetARbyId" + e.getMessage())
		}
		msoLogger.debug(" ***** Exit GetARLinkbyId ***** Link:" + arLink)
		return arLink
	}

	// get Allotted resource using Link
	// used on Create called from getARORchStatus
	// used on Delete called from getARbyId
	// setsVariable aaiARPath - used for Patch in create
	public String getARbyLink (DelegateExecution execution, String link, String role) {
		msoLogger.trace("getARbyLink ")
		String ar = null
		String arUrl = null
		try {
			AaiUtil aaiUriUtil = new AaiUtil(taskProcessor)
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			String arEndpoint = ""

			if(!isBlank(link)) {
				msoLogger.debug("Incoming AR Resource Link is: " + link)
				String[] split = link.split("/aai/")
				arEndpoint = "/aai/" + split[1]
			}

			arUrl = "${aai_endpoint}" + arEndpoint

			msoLogger.debug("GET AR Aai Path is: \n" + arUrl)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, arUrl)
			int responseCode = response.getStatusCode()
			msoLogger.debug("  GET AR response code is: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			msoLogger.debug("GET AR:" + aaiResponse)
			if(responseCode == 200 || responseCode == 202){
				msoLogger.debug("GET AR Received a Good Response Code")
				if(utils.nodeExists(aaiResponse, "allotted-resource")){
					if (!isBlank(role))
					{
						if (utils.nodeExists(aaiResponse, "role") && role.equals(utils.getNodeText(aaiResponse, "role"))) {
							ar = aaiResponse
						}else{
							msoLogger.debug("AAI AR does not match input role:" + role)
						}
					}
					else
					{
						ar = aaiResponse
					}
				}
				else
				{
					msoLogger.debug("GET AR Does NOT Contain Data" )
				}
			}else if(responseCode == 404){
				msoLogger.debug("GET AR received a Not Found (404) Response")
			}
			else{
				msoLogger.debug("  GET AR received a Bad Response: \n" + aaiResponse)
				buildAAIErrorResponse(execution, aaiResponse, "Error retrieving AR from AAI")
			}
		}catch(Exception e){
			msoLogger.debug(" Error encountered within GetAaiAR" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in GetAaiAR" + e.getMessage())
		}
		if (!isBlank(ar))
		{
			execution.setVariable("aaiARGetResponse", ar)
			execution.setVariable("aaiARPath", arUrl)

			String resourceVersion = null
			if (utils.nodeExists(ar, "resource-version")) {
				resourceVersion = utils.getNodeText(ar, "resource-version")
				execution.setVariable("aaiARResourceVersion", resourceVersion)
			}

			String orchStatus = null
			if (utils.nodeExists(ar, "orchestration-status")) {
				orchStatus= utils.getNodeText(ar, "orchestration-status")
			}
			else
			{
				orchStatus = " "
			}
			execution.setVariable("aaiAROrchStatus", orchStatus)
		}
		msoLogger.trace(" Exit GetARbyLink - AR:" + ar)
		return ar
	}

	public void updateAROrchStatus(DelegateExecution execution, String status, String aaiARPath){
		msoLogger.trace("updaAROrchStatus ")
		try{

			String updateReq =	"""
					{
					"orchestration-status": "${status}"
					}
					"""

			msoLogger.debug('AAI AR URI: ' + aaiARPath)

			AaiUtil aaiUriUtil = new AaiUtil(taskProcessor)
			APIResponse apiResponse = aaiUriUtil.executeAAIPatchCall(execution, aaiARPath, updateReq)
			def aaiResponse = apiResponse.getResponseBodyAsString()
			def responseCode = apiResponse.getStatusCode()

			msoLogger.debug("AAI Response Code: " + responseCode)
			msoLogger.debug("AAI Response: " + aaiResponse)
			if(responseCode == 200){
				msoLogger.debug("UpdateAR Good REST Response is: " + "\n" + aaiResponse)
			}else{
				msoLogger.debug("UpdateAROrchStatus Bad REST Response!")
				buildAAIErrorResponse(execution, aaiResponse, "Error updating AR OrchStatus in AAI")
			}

		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException ")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception in updateAR.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e.getMessage());
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, 'Internal Error in updateAROrchStatus.' + e.getMessage())
		}
		msoLogger.trace("Exit updateAROrchStatus ")
	}

	//Sets Variable "wasDeleted"
	public void deleteAR(DelegateExecution execution, String aaiARPath){
		msoLogger.trace(" deleteAR - aaiARPath:" + aaiARPath)
		try {
			AaiUtil aaiUriUtil = new AaiUtil(taskProcessor)
			APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, aaiARPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("deleteARResponseCode", responseCode)

			msoLogger.debug("  Delete AR response code:" + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			execution.setVariable("aaiARDeleteResponse", aaiResponse)

			msoLogger.debug("Delete AR Response:" + aaiResponse)

			//Process Response
			if(responseCode == 204){
				msoLogger.debug("  Delete AR Received a Good Response")
				execution.setVariable("wasDeleted", "true")
			}else if(responseCode == 404){
				msoLogger.debug("  Delete AR Received a Not Found (404) Response")
			}else if(responseCode == 412){
				msoLogger.debug("Delete AR Received a Resource Version Mismatch Error: \n" + aaiResponse)
				exceptionUtil.buildAndThrowWorkflowException(execution, 412, "DeleteAR Received a resource-version Mismatch Error Response from AAI")
			}else{
				msoLogger.debug("Delete AR Received a BAD REST Response: \n" + aaiResponse)
				buildAAIErrorResponse(execution, aaiResponse, "Error deleting AR in AAI")
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug(" Error encountered in deleteAR!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Delete AR")
		}
		msoLogger.trace("Exit deleteAR ")
	}

	public void buildAAIErrorResponse(DelegateExecution execution, String response, String errorMessage){
		msoLogger.trace("BuildAAIErrorResponse")

		if((response != null) && (response.contains("Fault") || response.contains("RESTFault"))){
			WorkflowException workflowException = exceptionUtil.MapAAIExceptionToWorkflowException(response, execution)
			execution.setVariable("WorkflowException", workflowException)
		}else{
			exceptionUtil.buildWorkflowException(execution, 500, errorMessage)
		}

		msoLogger.trace("Exit BuildAAIErrorResponse Process")
		throw new BpmnError("MSOWorkflowException")
	}

}
