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

package org.onap.so.apihandlerinfra.tenantisolation.helpers;

import java.net.URL;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.CryptoUtils;
import org.onap.so.utils.TargetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SDCClientHelper {

	private static Logger logger = LoggerFactory.getLogger(SDCClientHelper.class);
	private static final String SDC_CONTENT_TYPE = "application/json";
	private static final String SDC_ACCEPT_TYPE = "application/json";
	private static String PARTIAL_SDC_URI = "/sdc/v1/catalog/services/";

	private static String MESSAGE_UNDEFINED_ERROR = "Undefined Error Message!";
	private static String MESSAGE_UNEXPECTED_FORMAT = "Unexpected response format from SDC.";
	private final HttpClientFactory httpClientFactory = new HttpClientFactory();

	@Value("${mso.sdc.endpoint}")
	private String sdcEndpoint;
	@Value("${mso.sdc.activate.userid}")
	private String sdcActivateUserId;
	@Value("${mso.sdc.activate.instanceid}")
	private String sdcActivateInstanceId;
	@Value("${mso.sdc.client.auth}")
	private String sdcClientAuth;
	@Value("${mso.msoKey}")
	private String msoKey;

	/**
	 * Send POST request to SDC for operational activation
	 * @param serviceModelVersionI -  String
	 * @param operationalEnvironmentId - String
	 * @param workloadContext - String
	 * @return sdcResponseJsonObj - JSONObject object
	 * @throws JSONException
	 */
	public JSONObject postActivateOperationalEnvironment(String serviceModelVersionId, String operationalEnvironmentId, String workloadContext) throws ApiException {
		JSONObject sdcResponseJsonObj = new JSONObject();

		try {
			String urlString = this.buildUriBuilder(serviceModelVersionId, operationalEnvironmentId);
			String jsonPayload = this.buildJsonWorkloadContext(workloadContext);
			String basicAuthCred = getBasicAuth();

			if ( basicAuthCred == null || "".equals(basicAuthCred) ) {
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.BusinessProcesssError).build();
                ValidateException validateException = new ValidateException.Builder(" SDC credentials 'mso.sdc.client.auth' not setup in properties file!",
                        HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();

                throw validateException;
			}

			URL url = new URL(urlString);

			HttpClient httpClient = httpClientFactory.newJsonClient(url, TargetEntity.SDC);
			httpClient.addBasicAuthHeader(sdcClientAuth, msoKey);
			httpClient.addAdditionalHeader("X-ECOMP-InstanceID", sdcActivateInstanceId);
			httpClient.addAdditionalHeader("X-ECOMP-RequestID", UUID.randomUUID().toString());
			httpClient.addAdditionalHeader("Content-Type", SDCClientHelper.SDC_CONTENT_TYPE);
			httpClient.addAdditionalHeader("Accept", SDCClientHelper.SDC_ACCEPT_TYPE);
			httpClient.addAdditionalHeader("USER_ID", sdcActivateUserId);

			Response apiResponse = setHttpPostResponse(httpClient, jsonPayload);
			int statusCode = apiResponse.getStatus();;

			String responseData = apiResponse.readEntity(String.class);
			sdcResponseJsonObj = enhanceJsonResponse(new JSONObject(responseData), statusCode);

		} catch (Exception ex) {
			logger.debug("calling SDC Exception message: {}", ex.getMessage());
			String errorMessage = " Encountered Error while calling SDC POST Activate. " + ex.getMessage();
			logger.debug(errorMessage);
			sdcResponseJsonObj.put("statusCode", String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
			sdcResponseJsonObj.put("messageId", "");
			sdcResponseJsonObj.put("message", errorMessage);

		}
		return sdcResponseJsonObj;
	}

	/**
	 * set  HttpPostResponse
	 * @param config - RESTConfig object
	 * @param jsonPayload - String
	 * @return client - RestClient object
	 */
	public Response setHttpPostResponse(HttpClient client, String jsonPayload) throws ApiException {
		try {
            return client.post(jsonPayload);
        }catch(Exception ex){
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.BusinessProcesssError).build();
            ValidateException validateException = new ValidateException.Builder("Bad request could not post payload",
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(ex).errorInfo(errorLoggerInfo).build();

            throw validateException;
        }
	}

	/**
	 * enhance Response
	 * @param sdcResponseJsonObj - JSONObject object
	 * @param statusCode - int
	 * @return enhancedAsdcResponseJsonObj - JSONObject object
	 */
	public JSONObject enhanceJsonResponse(JSONObject sdcResponseJsonObj, int statusCode) throws JSONException {

		JSONObject enhancedAsdcResponseJsonObj = new JSONObject();

		String message = "";
		String messageId = "";

		if (statusCode == Response.Status.ACCEPTED.getStatusCode()) { // Accepted
			enhancedAsdcResponseJsonObj.put("distributionId", sdcResponseJsonObj.get("distributionId"));
			enhancedAsdcResponseJsonObj.put("statusCode", Integer.toString(statusCode));
			enhancedAsdcResponseJsonObj.put("messageId", "");
			enhancedAsdcResponseJsonObj.put("message", "Success");

		} else {  // error
			if (sdcResponseJsonObj.has("requestError") ) {
				JSONObject requestErrorObj = sdcResponseJsonObj.getJSONObject("requestError");
				if (sdcResponseJsonObj.getJSONObject("requestError").has("serviceException") ) {
					message = requestErrorObj.getJSONObject("serviceException").getString("text");
					messageId = requestErrorObj.getJSONObject("serviceException").getString("messageId");
				}
				if (sdcResponseJsonObj.getJSONObject("requestError").has("policyException") ) {
					message = requestErrorObj.getJSONObject("policyException").getString("text");
					messageId = requestErrorObj.getJSONObject("policyException").getString("messageId");
				}
				enhancedAsdcResponseJsonObj.put("statusCode", Integer.toString(statusCode));
				enhancedAsdcResponseJsonObj.put("messageId", messageId);
				enhancedAsdcResponseJsonObj.put("message", message);

			} else {
				// unexpected format
				enhancedAsdcResponseJsonObj.put("statusCode", String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
				enhancedAsdcResponseJsonObj.put("messageId", MESSAGE_UNDEFINED_ERROR);
				enhancedAsdcResponseJsonObj.put("message", MESSAGE_UNEXPECTED_FORMAT);
			}
		}
		return enhancedAsdcResponseJsonObj;

	}

	/**
	 * Build Uri
	 * @param serviceModelVersionId - String
	 * @param operationalEnvironmentId - String
	 * @return uriBuilder - String
	 */
	public String buildUriBuilder(String serviceModelVersionId,  String operationalEnvironmentId) {
	    String path = serviceModelVersionId + "/distribution/" + operationalEnvironmentId +"/activate";
	    UriBuilder uriBuilder =  UriBuilder.fromPath(sdcEndpoint + SDCClientHelper.PARTIAL_SDC_URI)
	                                       .path(path);
	    return  uriBuilder.build().toString();
	}

	/**
	 * Build JSON context
	 * @param  workloadContext - String
	 * @return String json
	 * @throws JSONException
	 */
	public String buildJsonWorkloadContext(String workloadContext) throws JSONException {
		return new JSONObject().put("workloadContext", workloadContext).toString();

	}

	/**
	 * decrypt value
	 * @param toDecrypt - String
	 * @param msokey - String
	 * @return result - String
	 */
	public synchronized String decrypt(String toDecrypt, String msokey){
		String result = null;
		try {
			result = CryptoUtils.decrypt(toDecrypt, msokey);

		}
		catch (Exception e) {
			logger.debug("Failed to decrypt credentials: {}", toDecrypt, e);
		}
		return result;
	}

	private String getBasicAuth() {
		return decrypt(sdcClientAuth, msoKey);
	}
}

