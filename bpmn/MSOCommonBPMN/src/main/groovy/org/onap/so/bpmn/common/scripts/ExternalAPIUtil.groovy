/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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


import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.logging.ref.slf4j.ONAPLogConstants
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.logging.filter.base.ONAPComponents;

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.regex.Matcher
import java.util.regex.Pattern

class ExternalAPIUtil {

	String Prefix="EXTAPI_"

    private static final Logger logger = LoggerFactory.getLogger( ExternalAPIUtil.class)

	private final HttpClientFactory httpClientFactory
	private final MsoUtils utils
	private final ExceptionUtil exceptionUtil

	public static final String PostServiceOrderRequestsTemplate =
	"{\n" +
	"\t\"externalId\": <externalId>,\n" +
	"\t\"category\": <category>,\n" +
	"\t\"description\": <description>,\n" +
	"\t\"requestedStartDate\": <requestedStartDate>,\n" +
	"\t\"requestedCompletionDate\": <requestedCompletionDate>,\n" +
	"\t\"priority\": <priority>,\n" +
	"\t\"@type\": null,\n" +
	"\t\"@baseType\": null,\n" +
	"\t\"@schemaLocation\": null,\n" +
	"\t\"relatedParty\": [{\n" +
    	"\t\t\"id\": <subscriberId>, \n" +
        "\t\t\"href\": null, \n" +
        "\t\t\"role\": <customerRole>, \n" +
        "\t\t\"name\": <subscriberName>, \n" +
        "\t\t\"@referredType\": <referredType> \n" +
	"}], \n" +
	"\t\"orderItem\": [{\n" +
    	"\t\t\"id\": <orderItemId>,\n" +
        "\t\t\"action\": <action>,\n" +
        "\t\t\"service\": {\n" +
            "\t\t\t\"serviceState\": <serviceState>,\n" +
			"\t\t\t\"id\": <serviceId>,\n" +
            "\t\t\t\"name\": <serviceName>,\n" +
            "\t\t\t\"serviceSpecification\": { \n" +
                "\t\t\t\t\"id\": <serviceUuId> \n" +
            "\t\t\t},\n" +
            "\t\t\t\"serviceCharacteristic\": [ \n" +
            "<_requestInputs_> \n" +
            "\t\t\t]  \n" +
        "\t\t}\n" +
    "\t}]\n" +
	"}"

	public static final String RequestInputsTemplate =
	"{ \n" +
    "\t\"name\": <inputName>, \n" +
    "\t\"value\": { \n" +
        "\t\t\"serviceCharacteristicValue\": <inputValue> \n" +
    "\t} \n" +
    "}"

	ExternalAPIUtil(HttpClientFactory httpClientFactory, MsoUtils utils, ExceptionUtil exceptionUtil) {
		this.httpClientFactory = httpClientFactory
		this.utils = utils
		this.exceptionUtil = exceptionUtil
	}

//	public String getUri(DelegateExecution execution, resourceName) {
//
//		def uri = execution.getVariable("ExternalAPIURi")
//		if(uri) {
//			logger.debug("ExternalAPIUtil.getUri: " + uri)
//			return uri
//		}
//
//		exceptionUtil.buildAndThrowWorkflowException(execution, 9999, 'ExternalAPI URI not find')
//	}

	public String setTemplate(String template, Map<String, String> valueMap) {
		logger.debug("ExternalAPIUtil setTemplate", true)
		StringBuffer result = new StringBuffer()

		String pattern = "<.*>"
		Pattern r = Pattern.compile(pattern)
		Matcher m = r.matcher(template)

		logger.debug("ExternalAPIUtil template:" + template, true)
		while (m.find()) {
			String key = template.substring(m.start() + 1, m.end() - 1)
			logger.debug("ExternalAPIUtil key:" + key + " contains key? " + valueMap.containsKey(key), true)
			m.appendReplacement(result, valueMap.getOrDefault(key, "\"TBD\""))
		}
		m.appendTail(result)
		logger.debug("ExternalAPIUtil return:" + result.toString(), true)
		return result.toString()
	}

	/**
	 * This reusable method can be used for making ExternalAPI Get Calls. The url should
	 * be passed as a parameter along with the execution.  The method will
	 * return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 *
	 * @return APIResponse
	 *
	 */
	public Response executeExternalAPIGetCall(DelegateExecution execution, String url){
		logger.debug(" ======== STARTED Execute ExternalAPI Get Process ======== ")
		Response apiResponse = null
		try{
			String uuid = utils.getRequestID()
			logger.debug( "Generated uuid is: " + uuid)
			logger.debug( "URL to be used is: " + url)
			logger.debug("URL to be passed in header is: " + execution.getVariable("SPPartnerUrl"))

			HttpClient client = httpClientFactory.newJsonClient(new URL(url), ONAPComponents.EXTERNAL)
			client.addBasicAuthHeader(execution.getVariable("URN_externalapi_auth"), execution.getVariable("URN_mso_msoKey"))
			client.addAdditionalHeader("X-FromAppId", "MSO")
			client.addAdditionalHeader(ONAPLogConstants.Headers.REQUEST_ID, uuid)
			client.addAdditionalHeader("Accept", MediaType.APPLICATION_JSON)
			client.addAdditionalHeader("Target",execution.getVariable("SPPartnerUrl"))

			apiResponse = client.get()

			logger.debug( "======== COMPLETED Execute ExternalAPI Get Process ======== ")
		}catch(Exception e){
			logger.debug("Exception occured while executing ExternalAPI Get Call. Exception is: \n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}

	/**
	 * This reusable method can be used for making ExternalAPI Post Calls. The url
	 * and payload should be passed as a parameters along with the execution.
	 * The method will return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 * @param payload
	 *
	 * @return Response
	 *
	 */
	public Response executeExternalAPIPostCall(DelegateExecution execution, String url, String payload){
		logger.debug( " ======== Started Execute ExternalAPI Post Process ======== ")
		Response apiResponse = null
		try{
			String uuid = utils.getRequestID()
			logger.debug( "Generated uuid is: " + uuid)
			logger.debug( "URL to be used is: " + url)
			logger.debug("URL to be passed in header is: " + execution.getVariable("SPPartnerUrl"))

			HttpClient httpClient = httpClientFactory.newJsonClient(new URL(url), ONAPComponents.AAI)
			httpClient.addBasicAuthHeader(execution.getVariable("URN_externalapi_auth"), execution.getVariable("URN_mso_msoKey"))
			httpClient.addAdditionalHeader("X-FromAppId", "MSO")
			httpClient.addAdditionalHeader("Target",execution.getVariable("SPPartnerUrl"))

			apiResponse = httpClient.post(payload)

			logger.debug( "======== Completed Execute ExternalAPI Post Process ======== ")
		}catch(Exception e){
			logger.error("Exception occured while executing ExternalAPI Post Call. Exception is: \n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}



}
