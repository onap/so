/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved. 
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
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig
import org.apache.commons.lang3.StringEscapeUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

class ExternalAPIUtil {
	
	String Prefix="EXTAPI_"

	public MsoUtils utils = new MsoUtils()
	
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ExternalAPIUtil.class)

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

	public ExternalAPIUtil() {
	}

//	public String getUri(DelegateExecution execution, resourceName) {
//
//		def uri = execution.getVariable("ExternalAPIURi")
//		if(uri) {
//			msoLogger.debug("ExternalAPIUtil.getUri: " + uri)
//			return uri
//		}
//		
//		exceptionUtil.buildAndThrowWorkflowException(execution, 9999, 'ExternalAPI URI not find')
//	}
	
	public String setTemplate(String template, Map<String, String> valueMap) {		
		msoLogger.debug("ExternalAPIUtil setTemplate", true);
		StringBuffer result = new StringBuffer();

		String pattern = "<.*>";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(template);

		msoLogger.debug("ExternalAPIUtil template:" + template, true);
		while (m.find()) {
			String key = template.substring(m.start() + 1, m.end() - 1);
			msoLogger.debug("ExternalAPIUtil key:" + key + " contains key? " + valueMap.containsKey(key), true);
			m.appendReplacement(result, valueMap.getOrDefault(key, "\"TBD\""));
		}
		m.appendTail(result);
		msoLogger.debug("ExternalAPIUtil return:" + result.toString(), true);
		return result.toString();
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
	public APIResponse executeExternalAPIGetCall(DelegateExecution execution, String url){
		msoLogger.debug(" ======== STARTED Execute ExternalAPI Get Process ======== ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug( "Generated uuid is: " + uuid)
			msoLogger.debug( "URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_externalapi_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/json");

			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.get()

			msoLogger.debug( "======== COMPLETED Execute ExternalAPI Get Process ======== ")
		}catch(Exception e){
			msoLogger.debug("Exception occured while executing ExternalAPI Get Call. Exception is: \n" + e)
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
	 * @return APIResponse
	 *
	 */
	public APIResponse executeExternalAPIPostCall(DelegateExecution execution, String url, String payload){
		msoLogger.debug( " ======== Started Execute ExternalAPI Post Process ======== ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug( "Generated uuid is: " + uuid)
			msoLogger.debug( "URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_externalapi_auth"),execution.getVariable("URN_mso_msoKey"))
			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/json").addHeader("Accept","application/json");

			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPost(payload)

			msoLogger.debug( "======== Completed Execute ExternalAPI Post Process ======== ")
		}catch(Exception e){
			msoLogger.error("Exception occured while executing ExternalAPI Post Call. Exception is: \n" + e)
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
	 * @param authenticationHeader - addAuthenticationHeader value
	 * @param headerName - name of header you want to add, i.e. addHeader(headerName, headerValue)
	 * @param headerValue - the header's value, i.e. addHeader(headerName, headerValue)
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeExternalAPIPostCall(DelegateExecution execution, String url, String payload, String authenticationHeaderValue, String headerName, String headerValue){
		msoLogger.debug( " ======== Started Execute ExternalAPI Post Process ======== ")
		APIResponse apiResponse = null
		try{
			msoLogger.debug( "URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_externalapi_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addAuthorizationHeader(authenticationHeaderValue).addHeader(headerName, headerValue)
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPost(payload)

			msoLogger.debug( "======== Completed Execute ExternalAPI Post Process ======== ")
		}catch(Exception e){
			msoLogger.error("Exception occured while executing ExternalAPI Post Call. Exception is: \n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}

}