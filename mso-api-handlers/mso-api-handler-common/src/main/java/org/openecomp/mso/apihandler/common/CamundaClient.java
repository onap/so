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

package org.openecomp.mso.apihandler.common;


import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.openecomp.mso.apihandler.camundabeans.CamundaBooleanInput;
import org.openecomp.mso.apihandler.camundabeans.CamundaInput;
import org.openecomp.mso.apihandler.camundabeans.CamundaIntegerInput;
import org.openecomp.mso.apihandler.camundabeans.CamundaRequest;
import org.openecomp.mso.apihandler.camundabeans.CamundaVIDRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CamundaClient extends RequestClient{
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH, CamundaClient.class);

	public CamundaClient() {
		super(CommonConstants.CAMUNDA);
	}


	@Override
	public HttpResponse post(String camundaReqXML, String requestId,
			String requestTimeout, String schemaVersion, String serviceInstanceId, String action)
					throws ClientProtocolException, IOException{
		HttpPost post = new HttpPost(url);
		msoLogger.debug("Camunda url is: "+ url);
		String jsonReq = wrapRequest(camundaReqXML, requestId, serviceInstanceId, requestTimeout,  schemaVersion);

		StringEntity input = new StringEntity(jsonReq);
		input.setContentType(CommonConstants.CONTENT_TYPE_JSON);
		msoLogger.info("Camunda Request Content: " + jsonReq);
		String encryptedCredentials = null;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					post.addHeader("Authorization", "Basic " + new String(DatatypeConverter.printBase64Binary(userCredentials.getBytes())));
				}
			}
		}

		post.setEntity(input);
		HttpResponse response = client.execute(post);
		msoLogger.debug("Response is: " + response);
		
		return response;
	}

	@Override
	public HttpResponse post(String jsonReq)
					throws ClientProtocolException, IOException{
		HttpPost post = new HttpPost(url);
		msoLogger.debug("Camunda url is: "+ url);
		//String jsonReq = wrapRequest(camundaReqXML, requestId, serviceInstanceId, requestTimeout,  schemaVersion);

		StringEntity input = new StringEntity(jsonReq);
		input.setContentType(CommonConstants.CONTENT_TYPE_JSON);

		String encryptedCredentials = null;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					post.addHeader("Authorization", "Basic " + new String(DatatypeConverter.printBase64Binary(userCredentials.getBytes())));
				}
			}
		}

		post.setEntity(input);
		HttpResponse response = client.execute(post);
		msoLogger.debug("Response is: " + response);

		return response;
	}

	

	

	/**
	 * @deprecated Use {@link #post(PostParameter)} instead
	 */
	public HttpResponse post(String requestId, boolean isBaseVfModule,
			int recipeTimeout, String requestAction, String serviceInstanceId,
			String vnfId, String vfModuleId, String volumeGroupId, String networkId, String configurationId,
			String serviceType, String vnfType, String vfModuleType, String networkType,
			String requestDetails, String apiVersion, boolean aLaCarte, String requestUri)
					throws ClientProtocolException, IOException{
						return post(new PostParameter(requestId, isBaseVfModule, recipeTimeout, requestAction,
								serviceInstanceId, vnfId, vfModuleId, volumeGroupId, networkId, configurationId,
								serviceType, vnfType, vfModuleType, networkType, requestDetails, apiVersion, aLaCarte, requestUri));
					}


	public HttpResponse post(PostParameter parameterObject)
					throws ClientProtocolException, IOException{
		HttpPost post = new HttpPost(url);
		msoLogger.debug("Camunda url is: "+ url);
		String jsonReq = wrapVIDRequest(parameterObject.getRequestId(), parameterObject.isBaseVfModule(), parameterObject.getRecipeTimeout(), parameterObject.getRequestAction(),
				parameterObject.getServiceInstanceId(), parameterObject.getVnfId(), parameterObject.getVfModuleId(), parameterObject.getVolumeGroupId(), parameterObject.getNetworkId(), parameterObject.getConfigurationId(),
				parameterObject.getServiceType(), parameterObject.getVnfType(), parameterObject.getVfModuleType(), parameterObject.getNetworkType(), parameterObject.getRequestDetails(), parameterObject.getApiVersion(), parameterObject.isaLaCarte(), parameterObject.getRequestUri());

		StringEntity input = new StringEntity(jsonReq);
		input.setContentType(CommonConstants.CONTENT_TYPE_JSON);

		String encryptedCredentials = null;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					post.addHeader("Authorization", "Basic " + new String(DatatypeConverter.printBase64Binary(userCredentials.getBytes())));
				}
			}
		}

		post.setEntity(input);
		HttpResponse response = client.execute(post);
		msoLogger.debug("Response is: " + response);

		return response;
	}
	
	@Override
    public HttpResponse get() {
        return null;
    }

	protected String wrapRequest(String reqXML, String requestId, String serviceInstanceId, String requestTimeout, String schemaVersion){
		String jsonReq = null;
		reqXML = nullCheck(reqXML);
		requestTimeout = nullCheck(requestTimeout);
		schemaVersion = nullCheck(schemaVersion);

		try{
			CamundaRequest camundaRequest = new CamundaRequest();
			CamundaInput camundaInput = new CamundaInput();
			CamundaInput host = new CamundaInput();
			CamundaInput schema = new CamundaInput();
			CamundaInput reqid = new CamundaInput();
			CamundaInput svcid = new CamundaInput();
			CamundaInput timeout = new CamundaInput();
			camundaInput.setValue(reqXML);
			host.setValue(parseURL());
			schema.setValue(schemaVersion);
			reqid.setValue(requestId);
			svcid.setValue(serviceInstanceId);
			timeout.setValue(requestTimeout);
			camundaRequest.setServiceInput(camundaInput);
			camundaRequest.setHost(host);
			camundaRequest.setReqid(reqid);
			camundaRequest.setSvcid(svcid);
			camundaRequest.setSchema(schema);
			camundaRequest.setTimeout(timeout);
			ObjectMapper mapper = new ObjectMapper();

			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

			jsonReq = mapper.writeValueAsString(camundaRequest);
			msoLogger.trace("request body is " + jsonReq);
		}catch(Exception e){
			msoLogger.error(MessageEnum.APIH_WARP_REQUEST, "Camunda", "wrapRequest", MsoLogger.ErrorCode.BusinessProcesssError, "Error in APIH Warp request", e);
		}
		return jsonReq;
	}	
	

	protected String wrapVIDRequest(String requestId, boolean isBaseVfModule,
			int recipeTimeout, String requestAction, String serviceInstanceId,
			String vnfId, String vfModuleId, String volumeGroupId, String networkId, String configurationId,
			String serviceType, String vnfType, String vfModuleType, String networkType,
			String requestDetails, String apiVersion, boolean aLaCarte, String requestUri){
		String jsonReq = null;
		requestId = nullCheck(requestId);
		requestAction = nullCheck(requestAction);
		serviceInstanceId = nullCheck(serviceInstanceId);
		vnfId = nullCheck(vnfId);
		vfModuleId = nullCheck(vfModuleId);
		volumeGroupId = nullCheck(volumeGroupId);
		networkId = nullCheck(networkId);
		configurationId = nullCheck(configurationId);
		serviceType = nullCheck(serviceType);
		vnfType = nullCheck(vnfType);
		vfModuleType = nullCheck(vfModuleType);
		networkType = nullCheck(networkType);
		requestDetails = nullCheck(requestDetails);
		apiVersion = nullCheck(apiVersion);
		requestUri = nullCheck(requestUri);

		try{
			CamundaVIDRequest camundaRequest = new CamundaVIDRequest();
			CamundaInput serviceInput = new CamundaInput();
			CamundaInput host = new CamundaInput();
			CamundaInput requestIdInput= new CamundaInput();
			CamundaBooleanInput isBaseVfModuleInput = new CamundaBooleanInput();
			CamundaIntegerInput recipeTimeoutInput = new CamundaIntegerInput();
			CamundaInput requestActionInput = new CamundaInput();
			CamundaInput serviceInstanceIdInput = new CamundaInput();
			CamundaInput vnfIdInput = new CamundaInput();
			CamundaInput vfModuleIdInput = new CamundaInput();
			CamundaInput volumeGroupIdInput = new CamundaInput();
			CamundaInput networkIdInput = new CamundaInput();
			CamundaInput configurationIdInput = new CamundaInput();
			CamundaInput serviceTypeInput = new CamundaInput();
			CamundaInput vnfTypeInput = new CamundaInput();
			CamundaInput vfModuleTypeInput = new CamundaInput();
			CamundaInput networkTypeInput = new CamundaInput();
			CamundaBooleanInput aLaCarteInput = new CamundaBooleanInput();
			CamundaInput apiVersionInput = new CamundaInput();
			CamundaInput requestUriInput = new CamundaInput();
			
			//host.setValue(parseURL());
			requestIdInput.setValue(requestId);
			isBaseVfModuleInput.setValue(isBaseVfModule);
			recipeTimeoutInput.setValue(recipeTimeout);
			requestActionInput.setValue(requestAction);
			serviceInstanceIdInput.setValue(serviceInstanceId);
			vnfIdInput.setValue(vnfId);
			vfModuleIdInput.setValue(vfModuleId);
			volumeGroupIdInput.setValue(volumeGroupId);
			networkIdInput.setValue(networkId);
			configurationIdInput.setValue(configurationId);
			serviceTypeInput.setValue(serviceType);
			vnfTypeInput.setValue(vnfType);
			vfModuleTypeInput.setValue(vfModuleType);
			networkTypeInput.setValue(networkType);
			aLaCarteInput.setValue(aLaCarte);
			apiVersionInput.setValue(apiVersion);
			requestUriInput.setValue(requestUri);

			serviceInput.setValue(requestDetails);
			camundaRequest.setServiceInput(serviceInput);
			camundaRequest.setHost(host);
			camundaRequest.setRequestId(requestIdInput);
			camundaRequest.setMsoRequestId(requestIdInput);
			camundaRequest.setIsBaseVfModule(isBaseVfModuleInput);
			camundaRequest.setRecipeTimeout(recipeTimeoutInput);
			camundaRequest.setRequestAction(requestActionInput);
			camundaRequest.setServiceInstanceId(serviceInstanceIdInput);
			camundaRequest.setVnfId(vnfIdInput);
			camundaRequest.setVfModuleId(vfModuleIdInput);
			camundaRequest.setVolumeGroupId(volumeGroupIdInput);
			camundaRequest.setNetworkId(networkIdInput);
			camundaRequest.setConfigurationId(configurationIdInput);
			camundaRequest.setServiceType(serviceTypeInput);
			camundaRequest.setVnfType(vnfTypeInput);
			camundaRequest.setVfModuleType(vfModuleTypeInput);
			camundaRequest.setNetworkType(networkTypeInput);
			camundaRequest.setaLaCarte(aLaCarteInput);
			camundaRequest.setApiVersion(apiVersionInput);
			camundaRequest.setRequestUri(requestUriInput);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

			jsonReq = mapper.writeValueAsString(camundaRequest);
			msoLogger.trace("request body is " + jsonReq);
		}catch(Exception e){
			msoLogger.error(MessageEnum.APIH_WARP_REQUEST, "Camunda", "wrapVIDRequest", MsoLogger.ErrorCode.BusinessProcesssError, "Error in APIH Warp request", e);
		}
		return jsonReq;
	}
	
	private String nullCheck(String input){
		if(input == null){
			input = "";
		}
		return input;
	}

	private String parseURL(){
		String[] parts = url.split(":");
		String host = "";
		if(parts.length>=2){
			host = parts[1];
			if(host.length()>2){
				host = host.substring(2);
			}
		}
		return host;
	}
}
