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

package org.openecomp.mso.apihandlerinfra.tenantisolation.helpers;

import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.json.JSONObject;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.AsdcClientCallFailed;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient;
import org.openecomp.mso.rest.RESTConfig;

public class AsdcClientHelper {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	private String className = this.getClass().getSimpleName();
	private String methodName = ""; 
	private String classMethodMessage = "";	
	
	private JSONObject asdcResponseJsonObj;

	protected MsoJavaProperties properties;
	
	public static final String ASDC_CONTENT_TYPE = "application/json";
	public static final String ASDC_ACCEPT_TYPE = "application/json";	
	
	protected String instanceid;
	protected String userid;
	protected String asdcEndpoint; 
	protected String basicAuthCred;
	protected String uri;	
	
	public static String PARTIAL_ASDC_URI = "/sdc/v1/catalog/services/";
	
	public AsdcClientHelper(MsoJavaProperties properties) {
		this.properties = properties;
		setAsdcProperties();

	};
	
	/**
	 * properties should be set during instantiation of this object
	 */	
	private void setAsdcProperties() { 
		String asdcClientAuth = this.properties.getProperty("mso.asdc.client.auth", null); 		
		String msoKey = this.properties.getProperty("mso.msoKey", null);
		this.basicAuthCred = this.properties.decrypt(asdcClientAuth, msoKey);
		this.asdcEndpoint = this.properties.getProperty("asdc.endpoint", null);
		this.userid = this.properties.getProperty("asdc.activate.userid", null);
		this.instanceid = this.properties.getProperty("asdc.activate.instanceid", null);
		
	}	
	
	/**
	 * Send POST request to ASDC for operational activation
	 * @param uri -  /sdc/v1/catalog/services/{serviceUUID}/distribution/{opEnvId}/activate
	 * @param jsonPayload - json string value of 'workloadContext'. 
	 * @return JSONObject
	 */	
	public JSONObject postActivateOperationalEnvironment(String serviceModelVersionId, String operationalEnvironmentId, String workloadContext) {

		try {
			
			String url = this.buildUriBuilder(serviceModelVersionId, operationalEnvironmentId);
			msoLogger.debug(" ASDC url : " + url);
			String jsonPayload = this.buildJsonWorkloadContext(workloadContext);
			msoLogger.debug(" ASDC jsonPayload : " + jsonPayload);
			asdcResponseJsonObj = new JSONObject();
			
			if ( basicAuthCred == null || "".equals(basicAuthCred) ) {		
				String errorMessage = " ** ERROR: ASDC credentials 'mso.asdc.client.auth' not setup in properties file!";				
				throw new AsdcClientCallFailed(errorMessage);
			}
			
			RESTConfig config = new RESTConfig(url);
			RESTClient client = setRestClient(config);
			client.addAuthorizationHeader(basicAuthCred);
			
			APIResponse apiResponse = setHttpPostResponse(client, jsonPayload);
			int statusCode = apiResponse.getStatusCode();
			msoLogger.debug(" ASDC return code : " + statusCode);
			String responseData = apiResponse.getResponseBodyAsString();
			msoLogger.debug(" ASDC responseData : " + responseData);			
			asdcResponseJsonObj = enhanceJsonResponse(new JSONObject(responseData), statusCode);
			
		} catch (Exception ex) {
			msoLogger.debug("calling ASDC Exception message: " + ex.getMessage());
			String errorMessage = " Encountered Error while calling ASDC POST Activate. " + ex.getMessage();
			msoLogger.debug(errorMessage);
			asdcResponseJsonObj.put("statusCode", "500"); 
			asdcResponseJsonObj.put("messageId", "");			
			asdcResponseJsonObj.put("message", errorMessage);

		}
		return asdcResponseJsonObj;
		
	}
	
	/**
	 * set RESTClient   
	 * @return RestClient object
	 */	
	public RESTClient setRestClient(RESTConfig config) throws Exception {
		
		RESTClient client = new RESTClient(config).addHeader("X-ECOMP-InstanceID", instanceid)
				  .addHeader("X-ECOMP-RequestID", UUID.randomUUID().toString())
				  .addHeader("Content-Type", AsdcClientHelper.ASDC_CONTENT_TYPE)
				  .addHeader("Accept", AsdcClientHelper.ASDC_ACCEPT_TYPE)
				  .addHeader("USER_ID", userid);
		return client;
		
	}	
	
	public APIResponse setHttpPostResponse(RESTClient client, String jsonPayload) throws Exception { 
		return client.httpPost(jsonPayload);
		
	}	
	
	
	public JSONObject enhanceJsonResponse(JSONObject asdcResponseJsonObj, int statusCode) {

		if (statusCode == 202) { // Accepted
			asdcResponseJsonObj.put("statusCode", Integer.toString(statusCode));
			asdcResponseJsonObj.put("messageId", "");
			asdcResponseJsonObj.put("message", "Success");					
			
		} else {  // error
			String message = "Undefined Error Message!";
			String messageId = "";			
			if (asdcResponseJsonObj.has("requestError") ) {
				JSONObject requestErrorObj = asdcResponseJsonObj.getJSONObject("requestError");
				if (asdcResponseJsonObj.getJSONObject("requestError").has("serviceException") ) {
					message = requestErrorObj.getJSONObject("serviceException").getString("text");
					messageId = requestErrorObj.getJSONObject("serviceException").getString("messageId");
				} 
				if (asdcResponseJsonObj.getJSONObject("requestError").has("policyException") ) {
						message = requestErrorObj.getJSONObject("policyException").getString("text");
						messageId = requestErrorObj.getJSONObject("policyException").getString("messageId");
				}					

			} 
			asdcResponseJsonObj.put("statusCode", Integer.toString(statusCode)); 
			asdcResponseJsonObj.put("messageId", messageId);
			asdcResponseJsonObj.put("message", message);
		}
		
		return asdcResponseJsonObj;
		
	}
	
	/**
	 * Build Uri   
	 * @return String uri
	 */		
	public String buildUriBuilder(String serviceModelVersionId,  String operationalEnvironmentId) {
	    String path = serviceModelVersionId + "/distribution/" + operationalEnvironmentId +"/activate";
	    UriBuilder uriBuilder =  UriBuilder.fromPath(asdcEndpoint + AsdcClientHelper.PARTIAL_ASDC_URI)
	                                       .path(path);
	    return  uriBuilder.build().toString();
	}
	
	/**
	 * Build JSON context  
	 * @return String json
	 */		
	public String buildJsonWorkloadContext(String workloadContext) {
		return new JSONObject().put("workloadContext", workloadContext).toString();
		
	}
	
	/**
	 * get asdc instanceId of this object
	 */		
	public String getAsdcInstanceId() {
		return this.instanceid;
	}
	
	/**
	 * get asdc asdcEndpoint of this object
	 */		
	public String getAsdcEndpoint() {
		return this.asdcEndpoint;
	}	

	/**
	 * get asdc asdcUserId of this object
	 */		
	public String getAsdcUserId() {
		return this.userid;
	}	
	
	
	
}
