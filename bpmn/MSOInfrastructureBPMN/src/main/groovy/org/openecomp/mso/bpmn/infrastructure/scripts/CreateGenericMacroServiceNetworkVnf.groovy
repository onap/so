/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>CreateGenericMacroServiceNetworkVnf.bpmn</class> process.
 *
 */
public class CreateGenericMacroServiceNetworkVnf extends AbstractServiceTaskProcessor {

	String Prefix="CREVAS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtils()

	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable("createGenericMacroServiceNetworkVnfRequest", "")
		execution.setVariable("globalSubscriberId", "")
		execution.setVariable("serviceInstanceName", "")
		execution.setVariable("msoRequestId", "")
		execution.setVariable("CREVAS_NetworksCreatedCount", 0)
		execution.setVariable("CREVAS_VnfsCreatedCount", 0)
		execution.setVariable("productFamilyId", "")
		
		
		//TODO
		execution.setVariable("sdncVersion", "1702")
	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest CreateGenericMacroServiceNetworkVnf Request ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// check for incoming json message/input
			String createGenericMacroServiceNetworkVnfRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(createGenericMacroServiceNetworkVnfRequest)
			execution.setVariable("createGenericMacroServiceNetworkVnfRequest", createGenericMacroServiceNetworkVnfRequest);
			println 'createGenericMacroServiceNetworkVnfRequest - ' + createGenericMacroServiceNetworkVnfRequest

			// extract requestId
			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			if ((serviceInstanceId == null) || (serviceInstanceId.isEmpty())) {
				serviceInstanceId = UUID.randomUUID().toString()
				utils.log("DEBUG", " Generated new Service Instance: " + serviceInstanceId , isDebugEnabled)
			} else {
				utils.log("DEBUG", "Using provided Service Instance ID: " + serviceInstanceId , isDebugEnabled)
			}

			serviceInstanceId = UriUtils.encode(serviceInstanceId,"UTF-8")
			execution.setVariable("serviceInstanceId", serviceInstanceId)
			
			String requestAction = execution.getVariable("requestAction")
			execution.setVariable("requestAction", requestAction)

			String source = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.requestInfo.source")
			if ((source == null) || (source.isEmpty())) {
				execution.setVariable("source", "VID")
			} else {
				execution.setVariable("source", source)
			}

			// extract globalSubscriberId
			String globalSubscriberId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.subscriberInfo.globalSubscriberId")

			// verify element global-customer-id is sent from JSON input, throw exception if missing
			if ((globalSubscriberId == null) || (globalSubscriberId.isEmpty())) {
				String dataErrorMessage = " Element 'globalSubscriberId' is missing. "
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
				execution.setVariable("globalCustomerId", globalSubscriberId)
			}
			
			// extract subscriptionServiceType
			String subscriptionServiceType = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.requestParameters.subscriptionServiceType")
			execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			utils.log("DEBUG", "Incoming subscriptionServiceType is: " + subscriptionServiceType, isDebugEnabled)

			String suppressRollback = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.requestInfo.suppressRollback")
			execution.setVariable("disableRollback", suppressRollback)
			utils.log("DEBUG", "Incoming Suppress/Disable Rollback is: " + suppressRollback, isDebugEnabled)
			
			String productFamilyId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.requestInfo.productFamilyId")
			execution.setVariable("productFamilyId", productFamilyId)
			utils.log("DEBUG", "Incoming productFamilyId is: " + productFamilyId, isDebugEnabled)
			
			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>CREATE</action>
					<source>${source}</source>
				   </request-info>"""

			execution.setVariable("CREVAS_requestInfo", requestInfo)
			
			utils.log("DEBUG", " ***** Completed preProcessRequest CreateGenericMacroServiceNetworkVnf Request ***** ", isDebugEnabled)

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside sendSyncResponse of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String requestId = execution.getVariable("mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse ="""{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse, isDebugEnabled)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected from method sendSyncResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	// *******************************
	//     
	// *******************************
	public void prepareCreateServiceInstance(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareCreateServiceInstance of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

			String createGenericMacroServiceNetworkVnfRequest = execution.getVariable("createGenericMacroServiceNetworkVnfRequest")
			String serviceModelInfo = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.modelInfo")
			execution.setVariable("serviceModelInfo", serviceModelInfo)

			String serviceInputParams = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.requestParameters")
			execution.setVariable("serviceInputParams", serviceInputParams)
			
			String serviceInstanceName = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.requestInfo.instanceName")
			execution.setVariable("serviceInstanceName", serviceInstanceName)

			utils.log("DEBUG", " ***** Completed prepareCreateServiceInstance of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected Error from method prepareCreateService() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	
	public void postProcessServiceInstanceCreate (Execution execution){
		def method = getClass().getSimpleName() + '.postProcessServiceInstanceCreate(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		String source = execution.getVariable("source")
		String requestId = execution.getVariable("mso-request-id")
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		String serviceInstanceName = execution.getVariable("serviceInstanceName")
		
		try {

			String payload = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.openecomp.mso/requestsdb">
			<soapenv:Header/>
			<soapenv:Body>
			<req:updateInfraRequest>
				<requestId>${requestId}</requestId>
				<lastModifiedBy>BPEL</lastModifiedBy>
				<serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
				<serviceInstanceName>${serviceInstanceName}</serviceInstanceName>
			</req:updateInfraRequest>
			</soapenv:Body>
			</soapenv:Envelope>
			"""
			execution.setVariable("CREVAS_setUpdateDbInstancePayload", payload)
			utils.logAudit("CREVAS_setUpdateDbInstancePayload: " + payload)
			logDebug('Exited ' + method, isDebugLogEnabled)
			//println("CMSO_updateDBStatusToSuccessPayload --> " + execution.getVariable("CMSO_updateDBStatusToSuccessPayload"))

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
	}
	
	
	public void callDBCatalog (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside callDBCatalog() of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
		
		try {
			
			// get variable within incoming json
			String createGenericMacroServiceNetworkVnfRequest = execution.getVariable("createGenericMacroServiceNetworkVnfRequest");
			
			String catalog_db_endpoint = execution.getVariable("URN_mso_catalog_db_endpoint")
			utils.log("DEBUG", "catalog_db_endpoint: "+catalog_db_endpoint, isDebugEnabled)
			
			String serviceModelInvariantId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.modelInfo.modelInvariantId")
			String serviceModelVersion = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.modelInfo.modelVersion")
			utils.log("DEBUG", "getting network list ", isDebugEnabled)
			
			JSONArray networkList = catalogDbUtils.getAllNetworksByServiceModelInvariantUuidAndServiceModelVersion(catalog_db_endpoint, serviceModelInvariantId, serviceModelVersion)

			//utils.log("DEBUG", "got network list: "+ networkList.toString(), isDebugEnabled)
			execution.setVariable("networkList", networkList)
			execution.setVariable("networkListString", networkList.toString())
			
			if (networkList != null && networkList.length() > 0) {

				execution.setVariable("CREVAS_NetworksCount", networkList.length())
				utils.log("DEBUG", "networks to create: "+ networkList.length(), isDebugEnabled)
			} else {
				execution.setVariable("CREVAS_NetworksCount", 0)
				utils.log("DEBUG", "no networks to create based upon Catalog DB response", isDebugEnabled)
			}	
			
			// VNFs
			JSONArray vnfList = catalogDbUtils.getAllVnfsByServiceModelInvariantUuidAndServiceModelVersion(catalog_db_endpoint, serviceModelInvariantId, serviceModelVersion)
			execution.setVariable("vnfList", vnfList)			
			
			String vnfModelInfoString = ""
			if (vnfList != null && vnfList.length() > 0) {
				execution.setVariable("CREVAS_VNFsCount", vnfList.length())
				utils.log("DEBUG", "vnfs to create: "+ vnfList.length(), isDebugEnabled)
				JSONObject vnfModelInfo = vnfList.getJSONObject(0).getJSONObject("modelInfo")
				vnfModelInfoString = vnfModelInfo.toString()
			} else {
					execution.setVariable("CREVAS_VNFsCount", 0)
					utils.log("DEBUG", "no vnfs to create based upon Catalog DB response", isDebugEnabled)
			}
				
			execution.setVariable("vnfModelInfo", vnfModelInfoString)
			//utils.log("DEBUG", " vnfModelInfoString :" + vnfModelInfoString, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed callDBCatalog() of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. callDBCatalog() - " + ex.getMessage()
		   utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}
	
	// *******************************
	//     Generate Network request Section
	// *******************************
	public void prepareNetworkCreate (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside preparenNetworkCreate of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

//			String disableRollback = execution.getVariable("disableRollback")
//			def backoutOnFailure = ""
//			if(disableRollback != null){
//				if ( disableRollback == true) {
//					backoutOnFailure = "false"
//				} else if ( disableRollback == false) {
//					backoutOnFailure = "true"
//				}
//			}
			//failIfExists - optional

			String createGenericMacroServiceNetworkVnfRequest = execution.getVariable("createGenericMacroServiceNetworkVnfRequest")
			
			JSONArray networkList = execution.getVariable("networkList")
			utils.log("DEBUG", "array networkList: "+ networkList, isDebugEnabled)
			
			if (networkList == null || networkList.length() < 1){
				networkList = new JSONArray(execution.getVariable("networkListString"))
				utils.log("DEBUG", "array from string networkList: "+ networkList, isDebugEnabled)
			}
			
			Integer networksCreatedCount = execution.getVariable("CREVAS_NetworksCreatedCount")
			String networkModelInfoString = ""
			
			if (networkList != null) {
				utils.log("DEBUG", " getting model info for network # :" + networksCreatedCount, isDebugEnabled)
				JSONObject networkModelInfo = networkList.getJSONObject(networksCreatedCount.intValue()).getJSONObject("modelInfo")
				networkModelInfoString = networkModelInfo.toString()
			} else {
				String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected number of networks to create - " + ex.getMessage()
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			}
			
			execution.setVariable("networkModelInfo", networkModelInfoString)
			utils.log("DEBUG", " networkModelInfoString :" + networkModelInfoString, isDebugEnabled)
			
//			String networkModelInfo = execution.getVariable("networkModelInfo")
			// extract cloud configuration
			String lcpCloudRegionId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", lcpCloudRegionId)
			utils.log("DEBUG","lcpCloudRegionId: "+ lcpCloudRegionId, isDebugEnabled)
			String tenantId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.cloudConfiguration.tenantId")
			execution.setVariable("tenantId", tenantId)
			utils.log("DEBUG","tenantId: "+ tenantId, isDebugEnabled)
			
			String sdncVersion = execution.getVariable("sdncVersion")
			utils.log("DEBUG","sdncVersion: "+ sdncVersion, isDebugEnabled)
			
			JSONArray vnfList = execution.getVariable("vnfList")
			utils.log("DEBUG", "vnfList: "+ vnfList, isDebugEnabled)
			
			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			utils.log("DEBUG", "vnfModelInfo: "+ vnfModelInfo, isDebugEnabled)
			
			networkList = execution.getVariable("networkList")
			utils.log("DEBUG", "networkList: "+ networkList, isDebugEnabled)
			
			utils.log("DEBUG", " ***** Completed preparenNetworkCreate of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected Error from method prepareNetworkCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	// *******************************
	//     Validate Network request Section -> increment count
	// *******************************
	public void validateNetworkCreate (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateNetworkCreate of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

			Integer networksCreatedCount = execution.getVariable("CREVAS_NetworksCreatedCount")
			networksCreatedCount++
			
			execution.setVariable("CREVAS_NetworksCreatedCount", networksCreatedCount)
			
			execution.setVariable("DCRENI_rollbackData"+networksCreatedCount, execution.getVariable("DCRENI_rollbackData"))

			utils.log("DEBUG", "networksCreatedCount: "+ networksCreatedCount, isDebugEnabled)
			utils.log("DEBUG", "DCRENI_rollbackData N : "+ execution.getVariable("DCRENI_rollbackData"+networksCreatedCount), isDebugEnabled)
			
			JSONArray vnfList = execution.getVariable("vnfList")
			utils.log("DEBUG", "vnfList: "+ vnfList, isDebugEnabled)
			
			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			utils.log("DEBUG", "vnfModelInfo: "+ vnfModelInfo, isDebugEnabled)
			
			JSONArray networkList = execution.getVariable("networkList")
			utils.log("DEBUG", "networkList: "+ networkList, isDebugEnabled)
			
			utils.log("DEBUG", " ***** Completed validateNetworkCreate of CreateGenericMacroServiceNetworkVnf ***** "+" network # "+networksCreatedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected Error from method validateNetworkCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	// *******************************
	//     Generate Network request Section
	// *******************************
	public void prepareVnfAndModulesCreate (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareVnfAndModulesCreate of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

			//			String disableRollback = execution.getVariable("disableRollback")
			//			def backoutOnFailure = ""
			//			if(disableRollback != null){
			//				if ( disableRollback == true) {
			//					backoutOnFailure = "false"
			//				} else if ( disableRollback == false) {
			//					backoutOnFailure = "true"
			//				}
			//			}
						//failIfExists - optional

			String createGenericMacroServiceNetworkVnfRequest = execution.getVariable("createGenericMacroServiceNetworkVnfRequest")
			String productFamilyId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.requestInfo.productFamilyId")
			execution.setVariable("productFamilyId", productFamilyId)
			utils.log("DEBUG","productFamilyId: "+ productFamilyId, isDebugEnabled)

			JSONArray vnfList = execution.getVariable("vnfList")

			Integer vnfsCreatedCount = execution.getVariable("CREVAS_VnfsCreatedCount")
			String vnfModelInfoString = null;
			
			if (vnfList != null && vnfList.length() > 0 ) {
				utils.log("DEBUG", "getting model info for vnf # " + vnfsCreatedCount, isDebugEnabled)
				JSONObject vnfModelInfo1 = vnfList.getJSONObject(0).getJSONObject("modelInfo")
				utils.log("DEBUG", "got 0 ", isDebugEnabled)
				JSONObject vnfModelInfo = vnfList.getJSONObject(vnfsCreatedCount.intValue()).getJSONObject("modelInfo")
				vnfModelInfoString = vnfModelInfo.toString()
			} else {
				//TODO: vnfList does not contain data. Need to investigate why ... . Fro VIPR use model stored
				vnfModelInfoString = execution.getVariable("vnfModelInfo")
			}
							
			utils.log("DEBUG", " vnfModelInfoString :" + vnfModelInfoString, isDebugEnabled)
			
			// extract cloud configuration
			String lcpCloudRegionId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", lcpCloudRegionId)
			utils.log("DEBUG","lcpCloudRegionId: "+ lcpCloudRegionId, isDebugEnabled)
			String tenantId = jsonUtil.getJsonValue(createGenericMacroServiceNetworkVnfRequest, "requestDetails.cloudConfiguration.tenantId")
			execution.setVariable("tenantId", tenantId)
			utils.log("DEBUG","tenantId: "+ tenantId, isDebugEnabled)
						
			String sdncVersion = execution.getVariable("sdncVersion")
			utils.log("DEBUG","sdncVersion: "+ sdncVersion, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed prepareVnfAndModulesCreate of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected Error from method prepareVnfAndModulesCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	// *******************************
	//     Validate Vnf request Section -> increment count
	// *******************************
	public void validateVnfCreate (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateVnfCreate of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

			Integer vnfsCreatedCount = execution.getVariable("CREVAS_VnfsCreatedCount")
			vnfsCreatedCount++
			
			execution.setVariable("CREVAS_VnfsCreatedCount", vnfsCreatedCount)
			
			utils.log("DEBUG", " ***** Completed validateVnfCreate of CreateGenericMacroServiceNetworkVnf ***** "+" vnf # "+vnfsCreatedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected Error from method validateVnfCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	// *******************************
	//     Validate Network request Section -> decrement count
	// *******************************
	public void validateNetworkRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateNetworkRollback of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

			Integer networksCreatedCount = execution.getVariable("CREVAS_NetworksCreatedCount")
			networksCreatedCount--
			
			execution.setVariable("CREVAS_NetworksCreatedCount", networksCreatedCount)
			
			execution.setVariable("DCRENI_rollbackData", execution.getVariable("DCRENI_rollbackData"+networksCreatedCount))
			
			utils.log("DEBUG", " ***** Completed validateNetworkRollback of CreateGenericMacroServiceNetworkVnf ***** "+" network # "+networksCreatedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected Error from method validateNetworkRollback() - " + ex.getMessage()
			//exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			execution.setVariable("CREVAS_NetworksCreatedCount", 0)
			utils.log("ERROR", exceptionMessage, true)
		}
	 }
	// *******************************
	//     Build DB request Section
	// *******************************
//	public void prepareDBRequest (Execution execution) {
//		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
//
//		try {
//			utils.log("DEBUG", " ***** Inside prepareDBRequest of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
//
//			String requestId = execution.getVariable("CREVAS_requestId")
//			String statusMessage = "vIPR ATM Service Instance successfully created."
//			String serviceInstanceId = execution.getVariable("CREVAS_serviceInstanceId")
//
//			//TODO - verify the format for Service Instance Create,
//			String dbRequest =
//					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
//						<soapenv:Header/>
//						<soapenv:Body>
//							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
//								<requestId>${requestId}</requestId>
//								<lastModifiedBy>BPMN</lastModifiedBy>
//								<statusMessage>${statusMessage}</statusMessage>
//								<responseBody></responseBody>
//								<requestStatus>COMPLETED</requestStatus>
//								<progress>100</progress>
//								<vnfOutputs/>
//								<serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
//							</ns:updateInfraRequest>
//						   </soapenv:Body>
//					   </soapenv:Envelope>"""
//
//		   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
//		   execution.setVariable("CREVAS_createDBRequest", buildDeleteDBRequestAsString)
//		   utils.logAudit(buildDeleteDBRequestAsString)
//
//		   utils.log("DEBUG", " ***** Completed prepareDBRequest of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
//		} catch (Exception ex) {
//			// try error in method block
//			String exceptionMessage = "Bpmn error encountered in CreateGenericMacroServiceNetworkVnf flow. Unexpected Error from method prepareDBRequest() - " + ex.getMessage()
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
//
//		}
//
//	 }


	// *****************************************
	//     Prepare Completion request Section
	// *****************************************
	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside postProcessResponse of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)

		try {
			String source = execution.getVariable("source")
			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
									xmlns:ns="http://org.openecomp/mso/request/types/v1">
							<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
								<request-id>${requestId}</request-id>
								<action>CREATE</action>
								<source>${source}</source>
							</request-info>
							<status-message>Service Instance has been created successfully via macro orchestration</status-message>
							<serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
							<mso-bpel-name>BPMN macro create</mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			utils.logAudit(xmlMsoCompletionRequest)
			execution.setVariable("CREVAS_Success", true)
			execution.setVariable("CREVAS_CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			utils.log("DEBUG", " SUCCESS flow, going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstance flow. Unexpected Error from method postProcessResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void preProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** preProcessRollback of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
		try {
			
			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Prev workflowException: " + workflowException.getErrorMessage(), isDebugEnabled)
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN Error during preProcessRollback", isDebugEnabled)
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit preProcessRollback of CreateGenericMacroServiceNetworkVnf *** ", isDebugEnabled)
	}

	public void postProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessRollback of CreateGenericMacroServiceNetworkVnf ***** ", isDebugEnabled)
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Setting prevException to WorkflowException: ", isDebugEnabled)
				execution.setVariable("WorkflowException", workflowException);
			}
		} catch (BpmnError b) {
			utils.log("DEBUG", "BPMN Error during postProcessRollback", isDebugEnabled)
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit postProcessRollback of CreateGenericMacroServiceNetworkVnf *** ", isDebugEnabled)
	}

	public void prepareFalloutRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " *** STARTED CreateGenericMacroServiceNetworkVnf prepareFalloutRequest Process *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("DEBUG", " Incoming Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestInfo = execution.getVariable("CREVAS_requestInfo")
			utils.log("DEBUG", " Incoming Request Info: " + requestInfo, isDebugEnabled)
			
			//TODO. hmmm. there is no way to UPDATE error message.
//			String errorMessage = wfex.getErrorMessage()
//			boolean successIndicator = execution.getVariable("DCRESI_rollbackSuccessful")
//			if (successIndicator){
//				errorMessage = errorMessage + ". Rollback successful."
//			} else {
//				errorMessage = errorMessage + ". Rollback not completed."
//			}
			
			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable("CREVAS_falloutRequest", falloutRequest)

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateGenericMacroServiceNetworkVnf prepareFalloutRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateGenericMacroServiceNetworkVnf prepareFalloutRequest Process")
		}
		utils.log("DEBUG", "*** COMPLETED CreateGenericMacroServiceNetworkVnf prepareFalloutRequest Process ***", isDebugEnabled)
	}


	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncError() of CreateServiceInstanceInfra ***** ", isDebugEnabled)

		try {
			String errorMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
				"""<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			utils.logAudit(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)
		} catch (Exception ex) {
			utils.log("DEBUG", " Sending Sync Error Activity Failed. " + "\n" + ex.getMessage(), isDebugEnabled)
		}
	}

	public void processJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception", isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable("CRESI_unexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
		}catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("CRESI_unexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}
}