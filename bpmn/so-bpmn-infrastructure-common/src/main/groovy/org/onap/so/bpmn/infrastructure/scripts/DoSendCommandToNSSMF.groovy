/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.beans.nsmf.*
import org.onap.so.bpmn.common.scripts.*
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ServiceArtifact
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import java.lang.reflect.Type

/**
 * This class supports the DoCreateVnf building block subflow
 * with the creation of a generic vnf for
 * infrastructure.
 *
 */
class DoSendCommandToNSSMF extends AbstractServiceTaskProcessor {

	private static final Logger logger = LoggerFactory.getLogger( DoSendCommandToNSSMF.class);
	String Prefix="DoCNSSMF_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
	private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		logger.debug("STARTED Do sendcommandtoNssmf PreProcessRequest Process")

		/*******************/
		try{
			// Get Variables
			String e2eserviceInstanceId = execution.getVariable("e2eserviceInstanceId")
			String serviceInstanceId = execution.getVariable("e2eserviceInstanceId")
			execution.setVariable("e2eserviceInstanceId", e2eserviceInstanceId)
			execution.setVariable("serviceInstanceId", serviceInstanceId)
			logger.debug("Incoming e2eserviceInstanceId is: " + e2eserviceInstanceId)

			String NSIserviceid =  execution.getVariable("NSIserviceid")
			execution.setVariable("NSIserviceid", NSIserviceid)
			logger.debug("Incoming NSI id is: " + NSIserviceid)


			String nssiMap  = execution.getVariable("nssiMap")
			Type type = new TypeToken<HashMap<String, NSSI>>(){}.getType()
			Map<String, NSSI> DonssiMap = new Gson().fromJson(nssiMap,type)
			String strDonssiMap = mapToJsonStr(DonssiMap)
			execution.setVariable("DonssiMap",strDonssiMap)
			logger.debug("Incoming DonssiMap is: " + strDonssiMap)

			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("msoRequestId", requestId)

			String operationType = execution.getVariable("operationType")
			execution.setVariable("operationType", operationType.toLowerCase())
			logger.debug("Incoming operationType is: " + operationType)

			if (operationType == "activation") {
				execution.setVariable("activationSequence","an,tn,cn")
			}else {
				execution.setVariable("activationSequence","cn,tn,an")
			}
			execution.setVariable("activationIndex",0)
			execution.setVariable("miniute", "0")
			execution.setVariable("activateNumberSlice",0)

			logger.info("the end !!")
		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.info("the end of catch !!")
			logger.debug(" Error Occured in DoSendCommandToNSSMF PreProcessRequest method!" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoSendCommandToNSSMF PreProcessRequest")

		}
		logger.trace("COMPLETED DoSendCommandToNSSMF PreProcessRequest Process")
	}

	private String mapToJsonStr(Map<String, NSSI> stringNSSIHashMap) {
		HashMap<String, NSSI> map = new HashMap<String, NSSI>()
		for(Map.Entry<String, NSSI> child:stringNSSIHashMap.entrySet())
		{
			map.put(child.getKey(), child.getValue())
		}
		return new Gson().toJson(map)
	}

	public	void getNSSIformlist(DelegateExecution execution) {

		String  nssiMap = execution.getVariable("DonssiMap")
		Type type = new TypeToken<HashMap<String, NSSI>>(){}.getType()
		Map<String, NSSI> DonssiMap = new Gson().fromJson(nssiMap,type)
		String isNSSIActivate = execution.getVariable("isNSSIActivate")

		String activationSequence01 = execution.getVariable("activationSequence")
		String[] strlist = activationSequence01.split(",")

		int  activationIndex = execution.getVariable("activationIndex")
		int indexcurrent = 0
		if (isNSSIActivate == "true")
		{
			execution.setVariable("isGetSuccessfull", "false")
		}else{for (int index = activationIndex; index < 3;index++) {
			String domaintype01 = strlist[index]
			if (DonssiMap.containsKey(domaintype01)) {
				NSSI nssiobject = DonssiMap.get(domaintype01)
				execution.setVariable("domainType", domaintype01)
				execution.setVariable("nssiId", nssiobject.getNssiId())
				execution.setVariable("modelInvariantUuid", nssiobject.getModelInvariantId())
				execution.setVariable("modelUuid", nssiobject.getModelVersionId())
				execution.setVariable("isGetSuccessfull", "true")
				String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
				String modelUuid = execution.getVariable("modelUuid")
				//here modelVersion is not set, we use modelUuid to decompose the service.
				String serviceModelInfo = """{
			"modelInvariantUuid":"${modelInvariantUuid}",
			"modelUuid":"${modelUuid}",
			"modelVersion":""
			 }"""
				execution.setVariable("serviceModelInfo", serviceModelInfo)
				indexcurrent = index
				execution.setVariable("activationIndex", indexcurrent)
				break
			}else
			{
				indexcurrent = index + 1

			}
		}
			if ( activationIndex > 2) {
				execution.setVariable("isGetSuccessfull", "false")
			}
			execution.setVariable("activationIndex", indexcurrent)}

	}

	/**
	 * get vendor Info
	 * @param execution
	 */
	private void processDecomposition(DelegateExecution execution) {
		logger.debug("***** processDecomposition *****")

		try {
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition") as ServiceDecomposition
			ServiceArtifact serviceArtifact = serviceDecomposition.getServiceInfo().getServiceArtifact().get(0)
			String content = serviceArtifact.getContent()
			String vendor = jsonUtil.getJsonValue(content, "metadata.vendor")
			//String domainType  = jsonUtil.getJsonValue(content, "metadata.domainType")

			execution.setVariable("vendor", vendor)
			//	currentNSSI['domainType'] = domainType
			logger.info("processDecomposition, current vendor-domainType:" +  vendor)

		} catch (any) {
			String exceptionMessage = "Bpmn error encountered in deallocate nssi. processDecomposition() - " + any.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		logger.debug("***** Exit processDecomposition *****")
	}

	public	void UpdateIndex(DelegateExecution execution) {
		def activationIndex = execution.getVariable("activationIndex")
		int activateNumberSlice = execution.getVariable("activateNumberSlice") as Integer
		def activationCount= execution.getVariable("activationCount")
		//DecimalFormat df1 = new DecimalFormat("##%")
		int  rate = (activateNumberSlice / activationCount) * 100
		if (rate == 100)
		{
			execution.setVariable("isNSSIActivate","true")
		}
		else{
			execution.setVariable("isNSSIActivate","false")
		}
		activationIndex = activationIndex + 1
		execution.setVariable("activationIndex",activationIndex)
		logger.trace("the Progress of activation is " + rate.toString() + "%" )
		try{
			String serviceId = execution.getVariable("serviceInstanceId")
			String operationId = UUID.randomUUID().toString()
			String operationType =  execution.getVariable("operationType")
			String userId = ""
			String result = (operationType.equalsIgnoreCase("activation"))? "ACTIVATING": "DEACTIVATING"
			int progress = rate
			String reason = ""
			String operationContent = "Service activation in progress"
			logger.debug("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)
			serviceId = UriUtils.encode(serviceId,"UTF-8")
			execution.setVariable("e2eserviceInstanceId", serviceId)
			execution.setVariable("operationId", operationId)
			execution.setVariable("operationType", operationType)

			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint",execution)
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

			String payload =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
						xmlns:ns="http://org.onap.so/requestsdb">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:initServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
							<serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
							<operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
							<operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
							<userId>${MsoUtils.xmlEscape(userId)}</userId>
							<result>${MsoUtils.xmlEscape(result)}</result>
							<operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
							<progress>${MsoUtils.xmlEscape(progress)}</progress>
							<reason>${MsoUtils.xmlEscape(reason)}</reason>
						</ns:initServiceOperationStatus>
					</soapenv:Body>
				</soapenv:Envelope>"""

			payload = utils.formatXml(payload)
			execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
			logger.debug("Outgoing CVFMI_updateServiceOperStatusRequest: \n" + payload)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing Activate Slice .", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during Activate Slice Method:\n" + e.getMessage())
		}
		logger.trace("finished Activate Slice")
	}

	public void WaitForReturn(DelegateExecution execution) {
		//logger.debug("Query : "+ Jobid)
		String miniute = execution.getVariable("miniute")
		Thread.sleep(10000)
		int miniute01  = Integer.parseInt(miniute) + 1
		logger.debug("waiting for : "+ miniute + "miniutes")
		execution.setVariable("miniute", String.valueOf(miniute01))
	}

	public void GetTheStatusOfActivation(DelegateExecution execution) {

		String domaintype = execution.getVariable("domainType")
		String NSIserviceid=execution.getVariable("NSIserviceid")
		String nssiId = execution.getVariable("nssiId")
		String Jobid=execution.getVariable("JobId")
		String miniute=execution.getVariable("miniute")
		String vendor = execution.getVariable("vendor")
		String jobstatus


		logger.debug("Query the jobid activation of SNSSAI: "+ Jobid)
		logger.debug("the domain is : "+ domaintype)
		logger.debug("the NSSID is : "+nssiId)
		logger.debug("the NSIserviceid is : "+NSIserviceid)

		JobStatusRequest jobStatusRequest = new JobStatusRequest()

		EsrInfo info = new EsrInfo()
		info.setNetworkType(NetworkType.fromString(domaintype))
		info.setVendor(vendor)

		jobStatusRequest.setNsiId(NSIserviceid)
		jobStatusRequest.setNssiId(nssiId)
		jobStatusRequest.setEsrInfo(info)


		ObjectMapper mapper = new ObjectMapper()
		String nssmfRequest = mapper.writeValueAsString(jobStatusRequest)
		String isActivateSuccessfull

		String urlString = "/api/rest/provMns/v1/NSS/jobs/" +Jobid

		JobStatusResponse jobStatusResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest, JobStatusResponse.class)

		if (jobStatusResponse != null) {
			execution.setVariable("statusDescription", jobStatusResponse.getResponseDescriptor().getStatusDescription())
			jobstatus = jobStatusResponse.getResponseDescriptor().getStatus()
			switch(jobstatus) {
				case "started":
				case "processing":
					isActivateSuccessfull = "waitting"
					execution.setVariable("isActivateSuccessfull", isActivateSuccessfull)
					break
				case "finished":
					isActivateSuccessfull = "true"
					execution.setVariable("isActivateSuccessfull", isActivateSuccessfull)
					execution.setVariable("activateNumberSlice",execution.getVariable("activateNumberSlice")+ 1)
					break
				case "error":
				default:
					isActivateSuccessfull = "false"
					execution.setVariable("isActivateSuccessfull", isActivateSuccessfull)

			}
			if(Integer.parseInt(miniute) > 6 )
			{
				isActivateSuccessfull = "false"
				execution.setVariable("isActivateSuccessfull", isActivateSuccessfull)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a timeout job status Response from NSSMF.")
			}
		} else {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a Bad job status Response from NSSMF.")
			isActivateSuccessfull = false
			execution.setVariable("isActivateSuccessfull", isActivateSuccessfull)
		}
	}

	public void SendCommandToNssmf(DelegateExecution execution) {

		String snssai= execution.getVariable("snssai")
		String domaintype = execution.getVariable("domainType")
		String NSIserviceid=execution.getVariable("NSIserviceid")
		String nssiId = execution.getVariable("nssiId")
		String vendor = execution.getVariable("vendor")


		logger.debug("the domain is : "+domaintype)
		logger.debug("SNSSAI: "+snssai +" will be activated")
		logger.debug("the NSSID is : "+nssiId)
		logger.debug("the NSIserviceid is : "+NSIserviceid)

		EsrInfo esr = new EsrInfo();
		esr.setNetworkType(NetworkType.fromString(domaintype))
		esr.setVendor(vendor)

		ActDeActNssi actNssi = new ActDeActNssi();
		actNssi.setNsiId(NSIserviceid);
		actNssi.setNssiId(nssiId);
		NssiActDeActRequest actRequest = new NssiActDeActRequest();
		actRequest.setActDeActNssi(actNssi);
		actRequest.setEsrInfo(esr)

		ObjectMapper mapper = new ObjectMapper()
		String nssmfRequest = mapper.writeValueAsString(actRequest)

		String operationType = execution.getVariable("operationType")

		String urlString = "/api/rest/provMns/v1/NSS/" + snssai + "/" + operationType.toLowerCase()

		NssiResponse nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest, NssiResponse.class)

		if (nssmfResponse != null) {
			String  isNSSIActivated = "true"
			execution.setVariable("isNSSIActivated", isNSSIActivated)
			String jobId  = nssmfResponse.getJobId() ?: ""
			execution.setVariable("JobId", jobId)
		} else {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a Bad Response from NSSMF.")
			String  isNSSIActivated = "false"
			execution.setVariable("isNSSIActivated", isNSSIActivated)
			execution.setVariable("isNSSIActivate","false")
		}

	}

	void sendSyncError (DelegateExecution execution) {
		logger.trace("start sendSyncError")
		try {
			String errorMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
					"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			logger.debug(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			logger.debug("Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}
		logger.trace("finished sendSyncError")
	}
}
