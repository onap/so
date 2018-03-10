package org.openecomp.mso.bpmn.common.scripts

import org.apache.commons.lang3.StringEscapeUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse


import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution

import groovy.util.XmlParser
import groovy.util.Node
import static org.apache.commons.lang3.StringUtils.*

class AllottedResourceUtils {

	private AbstractServiceTaskProcessor taskProcessor
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	MsoUtils utils

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
	public String getAROrchStatus (Execution execution) {

		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** getAROrchStatus *****", isDebugEnabled)
		String msg = ""
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		String arType = execution.getVariable("allottedResourceType")
		String arRole = execution.getVariable("allottedResourceRole")
		String siXml = execution.getVariable("CSI_service")
		String ar = null
		String orchStatus = null
		XmlParser xmlParser = new XmlParser()
		utils.log("DEBUG","getAROrchStatus siXml:" + siXml, isDebugEnabled)
		try {
			if (!isBlank(siXml)) {
				def groovy.util.Node siNode = xmlParser.parseText(siXml)
				def groovy.util.Node relationshipList = utils.getChildNode(siNode, 'relationship-list')
				if (relationshipList != null) {
					def groovy.util.NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
					for (groovy.util.Node relationship in relationships) {
						def groovy.util.Node relatedTo = utils.getChildNode(relationship, 'related-to')
						if ((relatedTo != null) && (relatedTo.text().equals('allotted-resource'))) {
							utils.log("DEBUG","getARORchStatus AR found", isDebugEnabled)
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
			utils.log("DEBUG", " Error encountered in getAROrchStatus" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in getAROrchStatus" + e.getMessage())
		}
		utils.log("DEBUG"," *****Exit getAROrchStatus **** OrchStatus:" + orchStatus, isDebugEnabled)
		return orchStatus
	}

	// get Allotted Resource by AllottedResourceId
	// used on Delete - called from doDeleteAR
	// setsVariable aaiARGetResponse
	public String getARbyId (Execution execution, String allottedResourceId) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ***** getARbyId ***** ", isDebugEnabled)
		String arLink = getARLinkbyId(execution, allottedResourceId)
		String ar = null
		if (!isBlank(arLink))
		{
			ar = getARbyLink(execution, arLink, "")
		}
		utils.log("DEBUG", " ***** Exit GetARbyId ***** AR:" + ar, isDebugEnabled)
		return ar
	}
	
	public String getPSIFmARLink(Execution execution, String arLink)
	{
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		// Path: /aai/{version}/business/customers/customer/{cust}/service-subscriptions/service-subscription/{subs}/service-instances/service-instance/{psiid}/allotted-resources/allotted-resource/{arid}
		utils.log("DEBUG", " ***** getPSIFmARLink ***** path:" + arLink, isDebugEnabled)
		String[] split = arLink.split("/service-instance/")
		String[] splitB =  split[1].split("/allotted-resources/")
		String siId = splitB[0]
		utils.log("DEBUG", " ***** Exit getARLinkbyId ***** parentServiceInstanceId:" + siId , isDebugEnabled)
		return siId
	}

	// get Allotted Resource Link by AllottedResourceId using Nodes Query
	// used on Delete - called from getARbyId
	public String getARLinkbyId (Execution execution, String allottedResourceId) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ***** getARLinkbyId ***** ", isDebugEnabled)
		String arLink = null
		try {
			AaiUtil aaiUriUtil = new AaiUtil(taskProcessor)
			String aaiNQUri = aaiUriUtil.getSearchNodesQueryEndpoint(execution)
			String aaiEndpoint = execution.getVariable("URN_aai_endpoint")
			String aaiUrl = "${aaiNQUri}?search-node-type=allotted-resource&filter=id:EQUALS:${allottedResourceId}"

			utils.log("DEBUG", "getARLinkbyId url: \n" + aaiUrl, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, aaiUrl)
			int responseCode = response.getStatusCode()
			utils.log("DEBUG", "  GET AR response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			utils.log("DEBUG", "GET AR:" + aaiResponse, isDebugEnabled)
			if(responseCode == 200 || responseCode == 202){
				utils.log("DEBUG", "GET AR Received a Good Response Code", isDebugEnabled)
				if(utils.nodeExists(aaiResponse, "result-data")){
					utils.log("DEBUG", "Query for AllottedResource Url Response Does Contain Data" , isDebugEnabled)
					arLink = utils.getNodeText1(aaiResponse, "resource-link")
				}else{
					utils.log("DEBUG", "GET AR Response Does NOT Contain Data" , isDebugEnabled)
				}
			}else if(responseCode == 404){
				utils.log("DEBUG", "GET AR received a Not Found (404) Response", isDebugEnabled)
			}
			else{
				utils.log("DEBUG", "  GET AR received a Bad Response: \n" + aaiResponse, isDebugEnabled)
				buildAAIErrorResponse(execution, aaiResponse, "Error retrieving AR from AAI")
			}
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered within GetAaiAR" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in GetARbyId" + e.getMessage())
		}
		utils.log("DEBUG", " ***** Exit GetARLinkbyId ***** Link:" + arLink, isDebugEnabled)
		return arLink
	}

	// get Allotted resource using Link
	// used on Create called from getARORchStatus
	// used on Delete called from getARbyId
	// setsVariable aaiARPath - used for Patch in create
	public String getARbyLink (Execution execution, String link, String role) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ***** getARbyLink ***** ", isDebugEnabled)
		String ar = null
		String arUrl = null
		try {
			AaiUtil aaiUriUtil = new AaiUtil(taskProcessor)
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			String arEndpoint = ""

			if(!isBlank(link)) {
				utils.log("DEBUG", "Incoming AR Resource Link is: " + link, isDebugEnabled)
				String[] split = link.split("/aai/")
				arEndpoint = "/aai/" + split[1]
			}

			arUrl = "${aai_endpoint}" + arEndpoint
		
			utils.log("DEBUG", "GET AR Aai Path is: \n" + arUrl, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, arUrl)
			int responseCode = response.getStatusCode()
			utils.log("DEBUG", "  GET AR response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			utils.log("DEBUG", "GET AR:" + aaiResponse, isDebugEnabled)
			if(responseCode == 200 || responseCode == 202){
				utils.log("DEBUG", "GET AR Received a Good Response Code", isDebugEnabled)
				if(utils.nodeExists(aaiResponse, "allotted-resource")){
					if (!isBlank(role))
					{
						if (utils.nodeExists(aaiResponse, "role") && role.equals(utils.getNodeText1(aaiResponse, "role"))) {
							ar = aaiResponse
						}else{
							utils.log("DEBUG", "AAI AR does not match input role:" + role, isDebugEnabled)
						}
					}
					else
					{
						ar = aaiResponse
					}
				}
				else
				{
					utils.log("DEBUG", "GET AR Does NOT Contain Data" , isDebugEnabled)
				}
			}else if(responseCode == 404){
				utils.log("DEBUG", "GET AR received a Not Found (404) Response", isDebugEnabled)
			}
			else{
				utils.log("DEBUG", "  GET AR received a Bad Response: \n" + aaiResponse, isDebugEnabled)
				buildAAIErrorResponse(execution, aaiResponse, "Error retrieving AR from AAI")
			}
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered within GetAaiAR" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error in GetAaiAR" + e.getMessage())
		}
		if (!isBlank(ar))
		{
			execution.setVariable("aaiARGetResponse", ar)
			execution.setVariable("aaiARPath", arUrl)
			
			String resourceVersion = null
			if (utils.nodeExists(ar, "resource-version")) {
				resourceVersion = utils.getNodeText1(ar, "resource-version")
				execution.setVariable("aaiARResourceVersion", resourceVersion)
			}
			
			String orchStatus = null
			if (utils.nodeExists(ar, "orchestration-status")) {
				orchStatus= utils.getNodeText1(ar, "orchestration-status")
			}
			else
			{
				orchStatus = " "
			}
			execution.setVariable("aaiAROrchStatus", orchStatus)
		}
		utils.log("DEBUG", " ***** Exit GetARbyLink ***** AR:" + ar, isDebugEnabled)
		return ar
	}

	public void updateAROrchStatus(Execution execution, String status, String aaiARPath){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** updaAROrchStatus *** ", isDebugEnabled)
		try{

			String updateReq =	"""
					{
					"orchestration-status": "${status}"
					}
					"""

			utils.log("DEBUG", 'AAI AR URI: ' + aaiARPath, isDebugEnabled)

			AaiUtil aaiUriUtil = new AaiUtil(taskProcessor)
			APIResponse apiResponse = aaiUriUtil.executeAAIPatchCall(execution, aaiARPath, updateReq)
			def aaiResponse = StringEscapeUtils.unescapeXml(apiResponse.getResponseBodyAsString())
			def responseCode = apiResponse.getStatusCode()

			utils.logAudit("AAI Response Code: " + responseCode)
			utils.logAudit("AAI Response: " + aaiResponse)
			if(responseCode == 200){
				utils.log("DEBUG", "UpdateAR Good REST Response is: " + "\n" + aaiResponse, isDebugEnabled)
			}else{
				utils.log("DEBUG", "UpdateAROrchStatus Bad REST Response!", isDebugEnabled)
				buildAAIErrorResponse(execution, aaiResponse, "Error updating AR OrchStatus in AAI")
			}

		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException ", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", "Exception in updateAR. Exception is:\n" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, 'Internal Error in updateAROrchStatus.' + e.getMessage())
		}
		utils.log("DEBUG", " *** Exit updateAROrchStatus *** ", isDebugEnabled)
	}
	
	//Sets Variable "wasDeleted"
	public void deleteAR(Execution execution, String aaiARPath){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** deleteAR *** aaiARPath:" + aaiARPath, isDebugEnabled)
		try {
			AaiUtil aaiUriUtil = new AaiUtil(taskProcessor)
			APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, aaiARPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("deleteARResponseCode", responseCode)
			
			utils.log("DEBUG", "  Delete AR response code:" + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("aaiARDeleteResponse", aaiResponse)

			utils.log("DEBUG", "Delete AR Response:" + aaiResponse)
			//Process Response
			if(responseCode == 204){
				utils.log("DEBUG", "  Delete AR Received a Good Response", isDebugEnabled)
				execution.setVariable("wasDeleted", "true")
			}else if(responseCode == 404){
				utils.log("DEBUG", "  Delete AR Received a Not Found (404) Response", isDebugEnabled)
			}else if(responseCode == 412){
				utils.log("DEBUG", "Delete AR Received a Resource Version Mismatch Error: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 412, "DeleteAR Received a resource-version Mismatch Error Response from AAI")
			}else{
				utils.log("DEBUG", "Delete AR Received a BAD REST Response: \n" + aaiResponse, isDebugEnabled)
				buildAAIErrorResponse(execution, aaiResponse, "Error deleting AR in AAI")
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered in deleteAR!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Delete AR")
		}
		utils.log("DEBUG", " *** Exit deleteAR *** ", isDebugEnabled)
	}

	public void buildAAIErrorResponse(Execution execution, String response, String errorMessage){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** BuildAAIErrorResponse*** ", isDebugEnabled)

		if((response != null) && (response.contains("Fault") || response.contains("RESTFault"))){
			WorkflowException workflowException = exceptionUtil.MapAAIExceptionToWorkflowException(response, execution)
			execution.setVariable("WorkflowException", workflowException)
		}else{
			exceptionUtil.buildWorkflowException(execution, 500, errorMessage)
		}

		utils.log("DEBUG", " *** Exit BuildAAIErrorResponse Process*** ", isDebugEnabled)
		throw new BpmnError("MSOWorkflowException")
	}

}