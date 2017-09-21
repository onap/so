/*
 * Â© 2016 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
package org.openecomp.mso.bpmn.vcpe.scripts;

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.domain.*

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>CreateVcpeResCustService.bpmn</class> process.
 *
 * @author ek1439
 *
 */
public class CreateVcpeResCustService extends AbstractServiceTaskProcessor {

	String Prefix="CVRCS_"
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

		execution.setVariable("createVcpeServiceRequest", "")
		execution.setVariable("globalSubscriberId", "")
		execution.setVariable("serviceInstanceName", "")
		execution.setVariable("msoRequestId", "")
		execution.setVariable("CVRCS_NetworksCreatedCount", 0)
		execution.setVariable("CVRCS_VnfsCreatedCount", 0)
		execution.setVariable("productFamilyId", "")
		execution.setVariable("brgWanMacAddress", "")

		//TODO
		execution.setVariable("sdncVersion", "1707")
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

		utils.log("DEBUG", " ***** Inside preProcessRequest CreateVcpeResCustService Request ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// check for incoming json message/input
			String createVcpeServiceRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(createVcpeServiceRequest)
			execution.setVariable("createVcpeServiceRequest", createVcpeServiceRequest);
			println 'createVcpeServiceRequest - ' + createVcpeServiceRequest

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

			setBasicDBAuthHeader(execution, isDebugEnabled)
			
			String source = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.source")
			if ((source == null) || (source.isEmpty())) {
				execution.setVariable("source", "VID")
			} else {
				execution.setVariable("source", source)
			}

			// extract globalSubscriberId
			String globalSubscriberId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.subscriberInfo.globalSubscriberId")

			// verify element global-customer-id is sent from JSON input, throw exception if missing
			if ((globalSubscriberId == null) || (globalSubscriberId.isEmpty())) {
				String dataErrorMessage = " Element 'globalSubscriberId' is missing. "
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
				execution.setVariable("globalCustomerId", globalSubscriberId)
			}

			// extract subscriptionServiceType
			String subscriptionServiceType = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestParameters.subscriptionServiceType")
			execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			utils.log("DEBUG", "Incoming subscriptionServiceType is: " + subscriptionServiceType, isDebugEnabled)

			String suppressRollback = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.suppressRollback")
			execution.setVariable("disableRollback", suppressRollback)
			utils.log("DEBUG", "Incoming Suppress/Disable Rollback is: " + suppressRollback, isDebugEnabled)

			String productFamilyId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.productFamilyId")
			execution.setVariable("productFamilyId", productFamilyId)
			utils.log("DEBUG", "Incoming productFamilyId is: " + productFamilyId, isDebugEnabled)
			
			String subscriberInfo = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.subscriberInfo")
			execution.setVariable("subscriberInfo", subscriberInfo)
			utils.log("DEBUG", "Incoming subscriberInfo is: " + subscriberInfo, isDebugEnabled)

		  /*
		  * Extracting User Parameters from incoming Request and converting into a Map
		  */
		  def jsonSlurper = new JsonSlurper()
		  def jsonOutput = new JsonOutput()
  
		  Map reqMap = jsonSlurper.parseText(createVcpeServiceRequest)
  
		  //InputParams
		  def userParams = reqMap.requestDetails?.requestParameters?.userParams
  
		  Map<String, String> inputMap = [:]
		  if (userParams) {
			  userParams.each {
				  name, value -> inputMap.put(name, value)
					if (name.equals("BRG_WAN_MAC_Address"))
							execution.setVariable("brgWanMacAddress", value)
			  }
		  }

		  utils.log("DEBUG", "User Input Parameters map: " + userParams.toString(), isDebugEnabled)
		  execution.setVariable("serviceInputParams", inputMap)

			utils.log("DEBUG", "Incoming brgWanMacAddress is: " + execution.getVariable('brgWanMacAddress'), isDebugEnabled)

			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>CREATE</action>
					<source>${source}</source>
				   </request-info>"""

			execution.setVariable("CVRCS_requestInfo", requestInfo)

			utils.log("DEBUG", " ***** Completed preProcessRequest CreateVcpeResCustService Request ***** ", isDebugEnabled)

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside sendSyncResponse of CreateVcpeResCustService ***** ", isDebugEnabled)

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String requestId = execution.getVariable("mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse ="""{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse, isDebugEnabled)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected from method sendSyncResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	// *******************************
	//
	// *******************************
	public void prepareDecomposeService(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareDecomposeService of CreateVcpeResCustService ***** ", isDebugEnabled)

			String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")

			//serviceModelInfo JSON string will be used as-is for DoCreateServiceInstance BB
			String serviceModelInfo = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.modelInfo")
			execution.setVariable("serviceModelInfo", serviceModelInfo)

			utils.log("DEBUG", " ***** Completed prepareDecomposeService of CreateVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	// *******************************
	//
	// *******************************
	public void prepareCreateServiceInstance(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareCreateServiceInstance of CreateVcpeResCustService ***** ", isDebugEnabled)

			/*
			 * Service modelInfo is created in earlier step. This flow can use it as-is ... or, extract from DecompositionObject
			 *		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			 *		ModelInfo modelInfo = serviceDecomposition.getModelInfo()
			 *
			 */
			String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")
//			String serviceInputParams = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestParameters")
//			execution.setVariable("serviceInputParams", serviceInputParams)


			String serviceInstanceName = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.instanceName")
			execution.setVariable("serviceInstanceName", serviceInstanceName)

			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			execution.setVariable("serviceDecompositionString", serviceDecomposition.toJsonString())

			utils.log("DEBUG", " ***** Completed prepareCreateServiceInstance of CreateVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
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
			execution.setVariable("CVRCS_setUpdateDbInstancePayload", payload)
			utils.logAudit("CVRCS_setUpdateDbInstancePayload: " + payload)
			logDebug('Exited ' + method, isDebugLogEnabled)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
	}


	public void processDecomposition (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside getDataFromDecomposition() of CreateVcpeResCustService ***** ", isDebugEnabled)

		try {

			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			List<NetworkResource> networkList = serviceDecomposition.getServiceNetworks()


			execution.setVariable("networkList", networkList)
			execution.setVariable("networkListString", networkList.toString())

			utils.log("DEBUG", "networkList: "+ networkList, isDebugEnabled)

			if (networkList != null && networkList.size() > 0) {
				execution.setVariable("CVRCS_NetworksCount", networkList.size())
				utils.log("DEBUG", "networks to create: "+ networkList.size(), isDebugEnabled)
			} else {
				execution.setVariable("CVRCS_NetworksCount", 0)
				utils.log("DEBUG", "no networks to create based upon serviceDecomposition content", isDebugEnabled)
			}

			// VNFs
			List<VnfResource> vnfList = serviceDecomposition.getServiceVnfs()
			execution.setVariable("vnfList", vnfList)
			execution.setVariable("vnfListString", vnfList.toString())

			String vnfModelInfoString = ""
			if (vnfList != null && vnfList.size() > 0) {
				execution.setVariable("CVRCS_VNFsCount", vnfList.size())
				utils.log("DEBUG", "vnfs to create: "+ vnfList.size(), isDebugEnabled)
				ModelInfo vnfModelInfo = vnfList[0].getModelInfo()

				vnfModelInfoString = vnfModelInfo.toString()
				String vnfModelInfoWithRoot = vnfModelInfo.toString()
				vnfModelInfoString = jsonUtil.getJsonValue(vnfModelInfoWithRoot, "modelInfo")
			} else {
					execution.setVariable("CVRCS_VNFsCount", 0)
					utils.log("DEBUG", "no vnfs to create based upon serviceDecomposition content", isDebugEnabled)
			}

			execution.setVariable("vnfModelInfo", vnfModelInfoString)
			execution.setVariable("vnfModelInfoString", vnfModelInfoString)
			utils.log("DEBUG", " vnfModelInfoString :" + vnfModelInfoString, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed getDataFromDecomposition() of CreateVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. getDataFromDecomposition() - " + ex.getMessage()
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
			utils.log("DEBUG", " ***** Inside preparenNetworkCreate of CreateVcpeResCustService ***** ", isDebugEnabled)


			String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")

			List<NetworkResource> networkList = execution.getVariable("networkList")
			utils.log("DEBUG", "networkList: "+ networkList, isDebugEnabled)

			Integer networksCreatedCount = execution.getVariable("CVRCS_NetworksCreatedCount")
			String networkModelInfoString = ""

			if (networkList != null) {
				utils.log("DEBUG", " getting model info for network # :" + networksCreatedCount, isDebugEnabled)
				ModelInfo networkModelInfo = networkList[networksCreatedCount.intValue()].getModelInfo()
				//Currently use String representation in JSON format as an input
				//execution.setVariable("networkModelInfo", networkModelInfo)
				networkModelInfoString = networkModelInfo.toJsonStringNoRootName()
			} else {
				String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected number of networks to create - " + ex.getMessage()
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			}

			//Currently use String representation in JSON format as an input
			execution.setVariable("networkModelInfo", networkModelInfoString)
			utils.log("DEBUG", " networkModelInfoString :" + networkModelInfoString, isDebugEnabled)

			// extract cloud configuration
			String lcpCloudRegionId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", lcpCloudRegionId)
			utils.log("DEBUG","lcpCloudRegionId: "+ lcpCloudRegionId, isDebugEnabled)
			String tenantId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.cloudConfiguration.tenantId")
			execution.setVariable("tenantId", tenantId)
			utils.log("DEBUG","tenantId: "+ tenantId, isDebugEnabled)

			String sdncVersion = execution.getVariable("sdncVersion")
			utils.log("DEBUG","sdncVersion: "+ sdncVersion, isDebugEnabled)

//			List<VnfResource> vnfList = execution.getVariable("vnfList")
//			utils.log("DEBUG", "vnfList: "+ vnfList.toString(), isDebugEnabled)
//
//			String vnfModelInfo = execution.getVariable("vnfModelInfo")
//			utils.log("DEBUG", "vnfModelInfo: "+ vnfModelInfo, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed preparenNetworkCreate of CreateVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method prepareNetworkCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	// *******************************
	//     Validate Network request Section -> increment count
	// *******************************
	public void validateNetworkCreate (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateNetworkCreate of CreateVcpeResCustService ***** ", isDebugEnabled)

			Integer networksCreatedCount = execution.getVariable("CVRCS_NetworksCreatedCount")
			networksCreatedCount++
			execution.setVariable("CVRCS_NetworksCreatedCount", networksCreatedCount)

			execution.setVariable("DCRENI_rollbackData"+networksCreatedCount, execution.getVariable("DCRENI_rollbackData"))

			utils.log("DEBUG", "networksCreatedCount: "+ networksCreatedCount, isDebugEnabled)
			utils.log("DEBUG", "DCRENI_rollbackData N : "+ execution.getVariable("DCRENI_rollbackData"+networksCreatedCount), isDebugEnabled)

//			JSONArray vnfList = execution.getVariable("vnfList")
//			utils.log("DEBUG", "vnfList: "+ vnfList, isDebugEnabled)

			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			utils.log("DEBUG", "vnfModelInfo: "+ vnfModelInfo, isDebugEnabled)

			List<NetworkResource> networkList = execution.getVariable("networkList")
			utils.log("DEBUG", "networkList: "+ networkList, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed validateNetworkCreate of CreateVcpeResCustService ***** "+" network # "+networksCreatedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method validateNetworkCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }


	public void prepareCreateAllottedResourceTXC(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareCreateAllottedResourceTXC of CreateVcpeResCustService ***** ", isDebugEnabled)

			/*
			 * Service modelInfo is created in earlier step. This flow can use it as-is ... or, extract from DecompositionObject
			 *		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			 *		ModelInfo modelInfo = serviceDecomposition.getModelInfo()
			 *
			 */
			String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

			//parentServiceInstanceId
			//The parentServiceInstanceId will be a Landing Network service.  This value will have been provided to the calling flow by SNIRO query (homing solution).
			//serviceDecomposition.getServiceNetworks()

			//For 1707, the vIPR Tenant OAM flow will use the BRG allotted resource parent service ID (since it is known that the security zone also comes from the vIPR FW).
			//Beyond 1707, this would need to be captured somehow in TOSCA model and also provided by SNIRO.

			//allottedResourceModelInfo
			//allottedResourceRole
			//The model Info parameters are a JSON structure as defined in the Service Instantiation API.
			//It would be sufficient to only include the service model UUID (i.e. the modelVersionId), since this BB will query the full model from the Catalog DB.
			List<AllottedResource> allottedResources = serviceDecomposition.getServiceAllottedResources()
			if (allottedResources != null) {
				Iterator iter = allottedResources.iterator();
				while (iter.hasNext()){
					AllottedResource allottedResource = (AllottedResource)iter.next();

					utils.log("DEBUG", " getting model info for AllottedResource # :" + allottedResource.toJsonString(), isDebugEnabled)
					utils.log("DEBUG", " allottedResource.getAllottedResourceType() :" + allottedResource.getAllottedResourceType(), isDebugEnabled)
					if(allottedResource.getAllottedResourceType() != null && allottedResource.getAllottedResourceType().equalsIgnoreCase("TunnelXConn")){
						//set create flag to true
						execution.setVariable("createTXCAR", true)
						ModelInfo allottedResourceModelInfo = allottedResource.getModelInfo()
						execution.setVariable("allottedResourceModelInfoTXC", allottedResourceModelInfo.toJsonString())
						execution.setVariable("allottedResourceRoleTXC", allottedResource.getAllottedResourceRole())
						execution.setVariable("allottedResourceTypeTXC", allottedResource.getAllottedResourceType())
						
						//from Homing Solution. This is the infraServiceInstanceId in the BRG Allotted Resource decomposition structure.
						execution.setVariable("parentServiceInstanceIdTXC", allottedResource.getHomingSolution().getServiceInstanceId())
					}
				}
			}

			//Populate with the A&AI network ID (l3-network object) for the Tenant OAM network that was created in prior step
			//String sourceNetworkId = execution.getVariable("networkId")
			//execution.setVariable("sourceNetworkId", sourceNetworkId)
			//Populate with the network-role (from A&AI l3-network object) for the Tenant OAM network from prior step
			
			//List<NetworkResource> networkResources = serviceDecomposition.getServiceNetworks()
			//if (networkResources != null) {
				//Iterator iter = networkResources.iterator();
				//while (iter.hasNext()){
					//NetworkResource networkResource = (NetworkResource)iter.next();
					//execution.setVariable("sourceNetworkRole", networkResource.getNetworkRole())
				//}
			//}

			//unit test only
			String allottedResourceId = execution.getVariable("allottedResourceId")
			execution.setVariable("allottedResourceIdTXC", allottedResourceId)
			utils.log("DEBUG", "setting allottedResourceId CreateVcpeResCustService "+allottedResourceId, isDebugEnabled)
			
			utils.log("DEBUG", " ***** Completed prepareCreateAllottedResourceTXC of CreateVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in prepareCreateAllottedResourceTXC flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	public void prepareCreateAllottedResourceBRG(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareCreateAllottedResourceBRG of CreateVcpeResCustService ***** ", isDebugEnabled)

			/*
			 * Service modelInfo is created in earlier step. This flow can use it as-is ... or, extract from DecompositionObject
			 *		ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			 *		ModelInfo modelInfo = serviceDecomposition.getModelInfo()
			 *
			 */
			String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

			//parentServiceInstanceId
			//The parentServiceInstanceId will be a Landing Network service.  This value will have been provided to the calling flow by SNIRO query (homing solution).
			//serviceDecomposition.getServiceNetworks()

			//For 1707, the vIPR Tenant OAM flow will use the BRG allotted resource parent service ID (since it is known that the security zone also comes from the vIPR FW).
			//Beyond 1707, this would need to be captured somehow in TOSCA model and also provided by SNIRO.

			//allottedResourceModelInfo
			//allottedResourceRole
			//The model Info parameters are a JSON structure as defined in the Service Instantiation API.
			//It would be sufficient to only include the service model UUID (i.e. the modelVersionId), since this BB will query the full model from the Catalog DB.
			List<AllottedResource> allottedResources = serviceDecomposition.getServiceAllottedResources()
			if (allottedResources != null) {
				Iterator iter = allottedResources.iterator();
				while (iter.hasNext()){
					AllottedResource allottedResource = (AllottedResource)iter.next();

					utils.log("DEBUG", " getting model info for AllottedResource # :" + allottedResource.toJsonString(), isDebugEnabled)
					utils.log("DEBUG", " allottedResource.getAllottedResourceType() :" + allottedResource.getAllottedResourceType(), isDebugEnabled)
					if (allottedResource.getAllottedResourceType() != null && allottedResource.getAllottedResourceType().equalsIgnoreCase("BRG")) {
						//set create flag to true
						execution.setVariable("createBRGAR", true)
						ModelInfo allottedResourceModelInfo = allottedResource.getModelInfo()
						execution.setVariable("allottedResourceModelInfoBRG", allottedResourceModelInfo.toJsonString())
						execution.setVariable("allottedResourceRoleBRG", allottedResource.getAllottedResourceRole())
						execution.setVariable("allottedResourceTypeBRG", allottedResource.getAllottedResourceType())
						//For 1707, the vIPR Tenant OAM flow will use the BRG allotted resource parent service ID (since it is known that the security zone also comes from the vIPR FW).
						//This Id should be taken from the homing solution for the BRG resource. 
						//After decomposition and homing BBs, there should be an allotted resource object in the decomposition that represents the BRG, 
						//and in its homingSolution section should be found the infraServiceInstanceId (i.e. infraServiceInstanceId in BRG Allotted Resource structure) (which the Homing BB would have populated).
						
						//from Homing Solution. This is the infraServiceInstanceId in the BRG Allotted Resource decomposition structure.
						execution.setVariable("parentServiceInstanceIdBRG", allottedResource.getHomingSolution().getServiceInstanceId())
					}
				}
			}

			//Populate with the A&AI network ID (l3-network object) for the Tenant OAM network that was created in prior step
			//String sourceNetworkId = execution.getVariable("networkId")
			//execution.setVariable("sourceNetworkId", sourceNetworkId)
			//Populate with the network-role (from A&AI l3-network object) for the Tenant OAM network from prior step
			
			//List<NetworkResource> networkResources = serviceDecomposition.getServiceNetworks()
			//if (networkResources != null) {
				//Iterator iter = networkResources.iterator();
				//while (iter.hasNext()){
					//NetworkResource networkResource = (NetworkResource)iter.next();
					//execution.setVariable("sourceNetworkRole", networkResource.getNetworkRole())
				//}
			//}

			//unit test only
			String allottedResourceId = execution.getVariable("allottedResourceId")
			execution.setVariable("allottedResourceIdBRG", allottedResourceId)
			utils.log("DEBUG", "setting allottedResourceId CreateVcpeResCustService "+allottedResourceId, isDebugEnabled)
			
			utils.log("DEBUG", " ***** Completed prepareCreateAllottedResourceBRG of CreateVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in prepareCreateAllottedResourceBRG flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }



	// *******************************
	//     Generate Network request Section
	// *******************************
	public void prepareVnfAndModulesCreate (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareVnfAndModulesCreate of CreateVcpeResCustService ***** ", isDebugEnabled)

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

			String createVcpeServiceRequest = execution.getVariable("createVcpeServiceRequest")
			String productFamilyId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.requestInfo.productFamilyId")
			execution.setVariable("productFamilyId", productFamilyId)
			utils.log("DEBUG","productFamilyId: "+ productFamilyId, isDebugEnabled)

			List<VnfResource> vnfList = execution.getVariable("vnfList")

			Integer vnfsCreatedCount = execution.getVariable("CVRCS_VnfsCreatedCount")
			String vnfModelInfoString = null;

			if (vnfList != null && vnfList.size() > 0 ) {
				utils.log("DEBUG", "getting model info for vnf # " + vnfsCreatedCount, isDebugEnabled)
				ModelInfo vnfModelInfo1 = vnfList[0].getModelInfo()
				utils.log("DEBUG", "got 0 ", isDebugEnabled)
				ModelInfo vnfModelInfo = vnfList[vnfsCreatedCount.intValue()].getModelInfo()
				vnfModelInfoString = vnfModelInfo.toString()
			} else {
				//TODO: vnfList does not contain data. Need to investigate why ... . Fro VCPE use model stored
				vnfModelInfoString = execution.getVariable("vnfModelInfo")
			}

			utils.log("DEBUG", " vnfModelInfoString :" + vnfModelInfoString, isDebugEnabled)

			// extract cloud configuration
			String lcpCloudRegionId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", lcpCloudRegionId)
			utils.log("DEBUG","lcpCloudRegionId: "+ lcpCloudRegionId, isDebugEnabled)
			String tenantId = jsonUtil.getJsonValue(createVcpeServiceRequest, "requestDetails.cloudConfiguration.tenantId")
			execution.setVariable("tenantId", tenantId)
			utils.log("DEBUG","tenantId: "+ tenantId, isDebugEnabled)

			String sdncVersion = execution.getVariable("sdncVersion")
			utils.log("DEBUG","sdncVersion: "+ sdncVersion, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed prepareVnfAndModulesCreate of CreateVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method prepareVnfAndModulesCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	// *******************************
	//     Validate Vnf request Section -> increment count
	// *******************************
	public void validateVnfCreate (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateVnfCreate of CreateVcpeResCustService ***** ", isDebugEnabled)

			Integer vnfsCreatedCount = execution.getVariable("CVRCS_VnfsCreatedCount")
			vnfsCreatedCount++

			execution.setVariable("CVRCS_VnfsCreatedCount", vnfsCreatedCount)

			utils.log("DEBUG", " ***** Completed validateVnfCreate of CreateVcpeResCustService ***** "+" vnf # "+vnfsCreatedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method validateVnfCreate() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	// *******************************
	//     Validate Network request Section -> decrement count
	// *******************************
	public void validateNetworkRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateNetworkRollback of CreateVcpeResCustService ***** ", isDebugEnabled)

			Integer networksCreatedCount = execution.getVariable("CVRCS_NetworksCreatedCount")
			networksCreatedCount--

			execution.setVariable("CVRCS_NetworksCreatedCount", networksCreatedCount)

			execution.setVariable("DCRENI_rollbackData", execution.getVariable("DCRENI_rollbackData"+networksCreatedCount))

			utils.log("DEBUG", " ***** Completed validateNetworkRollback of CreateVcpeResCustService ***** "+" network # "+networksCreatedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method validateNetworkRollback() - " + ex.getMessage()
			//exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			execution.setVariable("CVRCS_NetworksCreatedCount", 0)
			utils.log("ERROR", exceptionMessage, true)
		}
	 }

	// *****************************************
	//     Prepare Completion request Section
	// *****************************************
	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside postProcessResponse of CreateVcpeResCustService ***** ", isDebugEnabled)

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
			execution.setVariable("CVRCS_Success", true)
			execution.setVariable("CVRCS_CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			utils.log("DEBUG", " SUCCESS flow, going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateVcpeResCustService flow. Unexpected Error from method postProcessResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void preProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** preProcessRollback of CreateVcpeResCustService ***** ", isDebugEnabled)
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
		utils.log("DEBUG"," *** Exit preProcessRollback of CreateVcpeResCustService *** ", isDebugEnabled)
	}

	public void postProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessRollback of CreateVcpeResCustService ***** ", isDebugEnabled)
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
		utils.log("DEBUG"," *** Exit postProcessRollback of CreateVcpeResCustService *** ", isDebugEnabled)
	}

	public void prepareFalloutRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " *** STARTED CreateVcpeResCustService prepareFalloutRequest Process *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("DEBUG", " Incoming Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestInfo = execution.getVariable("CVRCS_requestInfo")
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

			execution.setVariable("CVRCS_falloutRequest", falloutRequest)

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVcpeResCustService prepareFalloutRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVcpeResCustService prepareFalloutRequest Process")
		}
		utils.log("DEBUG", "*** COMPLETED CreateVcpeResCustService prepareFalloutRequest Process ***", isDebugEnabled)
	}


	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncError() of CreateVcpeResCustService ***** ", isDebugEnabled)

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
