/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, Wipro Limited.
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonObject
import java.sql.Timestamp

import static org.apache.commons.lang3.StringUtils.isBlank
import org.onap.so.bpmn.core.UrnPropertiesReader

class DoModifyRanNfNssi extends AbstractServiceTaskProcessor {

	String Prefix="MANNFNSS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	ObjectMapper objectMapper = new ObjectMapper();
	AnNssmfUtils anNssmfUtils = new AnNssmfUtils()

	private static final Logger logger = LoggerFactory.getLogger(DoModifyRanNfNssi.class)

	@Override
	void preProcessRequest(DelegateExecution execution) {
		logger.debug(Prefix + "preProcessRequest Start")
		execution.setVariable("prefix", Prefix)
		execution.setVariable("startTime", System.currentTimeMillis())
		def msg
		try {

			logger.debug("input variables : msoRequestId - "+execution.getVariable("msoRequestId")+
					" globalSubscriberId - "+execution.getVariable("globalSubscriberId")+
					" serviceInstanceID - "+execution.getVariable("serviceInstanceID")+
					" subscriptionServiceType - "+execution.getVariable("subscriptionServiceType")+
					" sliceProfileId - "+execution.getVariable("sliceProfileId")+
					" snssaiList - "+execution.getVariable("snssaiList")+
					" modifyAction - "+execution.getVariable("modifyAction"))

			//validate RAN NF slice subnet inputs

			String modifyAction = execution.getVariable("modifyAction")
			if (isBlank(modifyAction)) {
				msg = "Input modifyAction is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("modifyAction", modifyAction)
				switch(modifyAction) {
					case "allocate":
						String sliceProfile = execution.getVariable("additionalProperties")
						execution.setVariable("sliceProfile", sliceProfile)
						break
					case "reconfigure":
						String resourceConfig = execution.getVariable("additionalProperties")
						execution.setVariable("resourceConfig", resourceConfig)
						break
					default:
						logger.debug("Invalid modify Action")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid modify Action : "+modifyAction)
				}
			}
			List<String> snssaiList = objectMapper.readValue(execution.getVariable("snssaiList"), List.class)
			String sliceProfileId = execution.getVariable("sliceProfileId")
			if (isBlank(sliceProfileId) || (snssaiList.empty)) {
				msg = "Mandatory fields are empty"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("sliceProfileId", sliceProfileId)
				execution.setVariable("snssaiList", snssaiList)
				execution.setVariable("snssai", snssaiList.get(0))
			}
			
		} catch(BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in DoModifyAccessNssi.preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug(Prefix + "preProcessRequest Exit")
	}
	
	def createSdnrRequest = { DelegateExecution execution ->
		logger.debug(Prefix+"createSdnrRequest method start")
		String callbackUrl = UrnPropertiesReader.getVariable("mso.workflow.message.endpoint") + "/AsyncSdnrResponse/"+execution.getVariable("msoRequestId")
		String modifyAction = execution.getVariable("modifyAction")
		String sdnrRequest = buildSdnrAllocateRequest(execution, modifyAction, "InstantiateRANSlice", callbackUrl)
		execution.setVariable("createNSSI_sdnrRequest", sdnrRequest)
		execution.setVariable("createNSSI_timeout", "PT10M")
		execution.setVariable("createNSSI_correlator", execution.getVariable("msoRequestId"))
		execution.setVariable("createNSSI_messageType", "AsyncSdnrResponse");
	}
	
	def processSdnrResponse = { DelegateExecution execution ->
		logger.debug(Prefix+"processSdnrResponse method start")
		String SDNRResponse = execution.getVariable("SDNR_asyncCallbackResponse")
		String status = jsonUtil.getJsonValue(SDNRResponse, "status")
		if(status.equalsIgnoreCase("success")) {
			String nfIds = jsonUtil.getJsonValue(SDNRResponse, "nfIds")
			execution.setVariable("ranNfIdsJson", nfIds)
		}else {
			String reason = jsonUtil.getJsonValue(SDNRResponse, "reason")
			logger.error("received failed status from SDNR "+ reason)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from SDNR "+ reason)
		}
		logger.debug("response from SDNR "+SDNRResponse)
	}
	
	private String buildSdnrAllocateRequest(DelegateExecution execution, String action, String rpcName, String callbackUrl) {
		
		String requestId = execution.getVariable("msoRequestId")
		Date date = new Date().getTime()
		Timestamp time = new Timestamp(date)
		String sliceProfileString
		JsonObject response = new JsonObject()
		JsonObject body = new JsonObject()
		JsonObject input = new JsonObject()
		JsonObject commonHeader = new JsonObject()
		JsonObject payload = new JsonObject()
		JsonObject payloadInput = new JsonObject()
		if(action.equals("allocate")) {
			Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class)
			sliceProfile.put("sliceProfileId", execution.getVariable("sliceProfileId"))
			sliceProfile.put("maxNumberofConns", sliceProfile.get("maxNumberofPDUSessions"))
			sliceProfile.put("uLThptPerSlice", sliceProfile.get("expDataRateUL"))
			sliceProfile.put("dLThptPerSlice", sliceProfile.get("expDataRateDL"))
			sliceProfileString = objectMapper.writeValueAsString(sliceProfile)
			action = "modify-"+action
			payloadInput.add("additionalproperties", new JsonObject())
		}else if(action.equals("deallocate")) {
			action = "modify-"+action
			Map<String,Object> sliceProfile = new HashMap<>()
			sliceProfile.put("sliceProfileId", execution.getVariable("sliceProfileId"))
			sliceProfile.put("sNSSAI", execution.getVariable("snssai"))
			sliceProfileString = objectMapper.writeValueAsString(sliceProfile)
			payloadInput.add("additionalproperties", new JsonObject())
		}else if(action.equals("reconfigure")) {
			Map<String,Object> sliceProfile = new HashMap<>()
			sliceProfile.put("sliceProfileId", execution.getVariable("sliceProfileId"))
			sliceProfile.put("sNSSAI", execution.getVariable("snssai"))
			sliceProfileString = objectMapper.writeValueAsString(sliceProfile)
			JsonObject resourceconfig = new JsonObject()
			resourceconfig.addProperty("resourceConfig", execution.getVariable("resourceConfig"))
			payloadInput.add("additionalproperties", resourceconfig)
		}
		commonHeader.addProperty("TimeStamp", time.toString())
		commonHeader.addProperty("APIver", "1.0")
		commonHeader.addProperty("RequestID", requestId)
		commonHeader.addProperty("SubRequestID", "1")
		commonHeader.add("RequestTrack", new JsonObject())
		commonHeader.add("Flags", new JsonObject())
		payloadInput.addProperty("sliceProfile", sliceProfileString)
		payloadInput.addProperty("RANNFNSSIId", execution.getVariable("serviceInstanceID"))
		payloadInput.addProperty("callbackURL", callbackUrl)
		payload.add("input", payloadInput)
		input.add("CommonHeader", commonHeader)
		input.addProperty("Action", action)
		input.add("Payload", payload)
		body.add("input", input)
		response.add("body", body)
		response.addProperty("version", "1.0")
		response.addProperty("rpc-name", rpcName)
		response.addProperty("correlation-id", requestId+"-1")
		response.addProperty("type", "request")
		return response.toString()
	}
	
}