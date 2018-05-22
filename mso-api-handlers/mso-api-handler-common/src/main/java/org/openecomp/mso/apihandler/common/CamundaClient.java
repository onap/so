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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
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
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	private static final String CAMUNDA_URL_MESAGE = "Camunda url is: ";

	public CamundaClient() {
		super(CommonConstants.CAMUNDA);
	}


	@Override
	public HttpResponse post(String camundaReqXML, String requestId,
			String requestTimeout, String schemaVersion, String serviceInstanceId, String action) throws IOException {
		HttpPost post = new HttpPost(url);
		msoLogger.debug(CAMUNDA_URL_MESAGE + url);
		String jsonReq = wrapRequest(camundaReqXML, requestId, serviceInstanceId, requestTimeout,  schemaVersion);

		StringEntity input = new StringEntity(jsonReq);
		input.setContentType(CommonConstants.CONTENT_TYPE_JSON);

		String encryptedCredentials;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH,null);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					post.addHeader("Authorization", "Basic " + DatatypeConverter
                        .printBase64Binary(userCredentials.getBytes()));
				}
			}
		}

		post.setEntity(input);
        return client.execute(post);
	}

	@Override
	public HttpResponse post(String jsonReq) throws IOException {
		HttpPost post = new HttpPost(url);
		msoLogger.debug(CAMUNDA_URL_MESAGE + url);

		StringEntity input = new StringEntity(jsonReq);
		input.setContentType(CommonConstants.CONTENT_TYPE_JSON);

		String encryptedCredentials;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH,null);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					post.addHeader("Authorization", "Basic " + DatatypeConverter
                        .printBase64Binary(userCredentials.getBytes()));
				}
			}
		}

		post.setEntity(input);

        return client.execute(post);
	}

	@Override
	public HttpResponse post(RequestClientParamater params) throws IOException {
		HttpPost post = new HttpPost(url);
		msoLogger.debug(CAMUNDA_URL_MESAGE + url);
		String jsonReq = wrapVIDRequest(params);

		StringEntity input = new StringEntity(jsonReq);
		input.setContentType(CommonConstants.CONTENT_TYPE_JSON);
		String encryptedCredentials;
		if(props!=null){
			encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH,null);
			if(encryptedCredentials != null){
				String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH, CommonConstants.ENCRYPTION_KEY);
				if(userCredentials != null){
					post.addHeader("Authorization", "Basic " + DatatypeConverter
                        .printBase64Binary(userCredentials.getBytes()));
				}
			}
		}
		post.setEntity(input);
        return client.execute(post);
	}

	@Override
    public HttpResponse get() {
        return null;
    }

	private String wrapRequest(String reqXML, String requestId, String serviceInstanceId, String requestTimeout, String schemaVersion){
		String jsonReq = null;
		if(reqXML == null){
			reqXML ="";
		}
		if(requestTimeout == null){
			requestTimeout ="";
		}
		if(schemaVersion == null){
			schemaVersion = "";
		}
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
			msoLogger.debug("request body is " + jsonReq);
		}catch(Exception e){
			msoLogger.error(MessageEnum.APIH_WARP_REQUEST, "Camunda", "wrapRequest", MsoLogger.ErrorCode.BusinessProcesssError, "Error in APIH Warp request", e);
		}
		return jsonReq;
	}

	private String wrapVIDRequest(RequestClientParamater requestClientParamater) {
		String jsonReq = null;
		try{
			CamundaVIDRequest camundaRequest = new CamundaVIDRequest();
			CamundaInput serviceInput = new CamundaInput();
			CamundaInput host = new CamundaInput();
			CamundaInput requestIdInput= new CamundaInput();
			CamundaBooleanInput isBaseVfModuleInput = new CamundaBooleanInput();
			CamundaIntegerInput recipeTimeoutInput = new CamundaIntegerInput();
			CamundaInput requestActionInput = new CamundaInput();
			CamundaInput serviceInstanceIdInput = new CamundaInput();
			CamundaInput correlationIdInput = new CamundaInput();
			CamundaInput vnfIdInput = new CamundaInput();
			CamundaInput vfModuleIdInput = new CamundaInput();
			CamundaInput volumeGroupIdInput = new CamundaInput();
			CamundaInput networkIdInput = new CamundaInput();
			CamundaInput configurationIdInput = new CamundaInput();
			CamundaInput serviceTypeInput = new CamundaInput();
			CamundaInput vnfTypeInput = new CamundaInput();
			CamundaInput vfModuleTypeInput = new CamundaInput();
			CamundaInput networkTypeInput = new CamundaInput();
			CamundaInput recipeParamsInput = new CamundaInput();
			requestIdInput.setValue(StringUtils.defaultString(requestClientParamater.getRequestId()));
			isBaseVfModuleInput.setValue(requestClientParamater.isBaseVfModule());
			recipeTimeoutInput.setValue(requestClientParamater.getRecipeTimeout());
			requestActionInput.setValue(StringUtils.defaultString(requestClientParamater.getRequestAction()));
			serviceInstanceIdInput.setValue(StringUtils.defaultString(requestClientParamater.getServiceInstanceId()));
			correlationIdInput.setValue(StringUtils.defaultString(requestClientParamater.getCorrelationId()));
			vnfIdInput.setValue(StringUtils.defaultString(requestClientParamater.getVnfId()));
			vfModuleIdInput.setValue(StringUtils.defaultString(requestClientParamater.getVfModuleId()));
			volumeGroupIdInput.setValue(StringUtils.defaultString(requestClientParamater.getVolumeGroupId()));
			networkIdInput.setValue(StringUtils.defaultString(requestClientParamater.getNetworkId()));
			configurationIdInput.setValue(StringUtils.defaultString(requestClientParamater.getConfigurationId()));
			serviceTypeInput.setValue(StringUtils.defaultString(requestClientParamater.getServiceType()));
			vnfTypeInput.setValue(StringUtils.defaultString(requestClientParamater.getVnfType()));
			vfModuleTypeInput.setValue(StringUtils.defaultString(requestClientParamater.getVfModuleType()));
			networkTypeInput.setValue(StringUtils.defaultString(requestClientParamater.getNetworkType()));
			recipeParamsInput.setValue(requestClientParamater.getRecipeParamXsd());
			serviceInput.setValue(StringUtils.defaultString(requestClientParamater.getRequestDetails()));
			camundaRequest.setServiceInput(serviceInput);
			camundaRequest.setHost(host);
			camundaRequest.setRequestId(requestIdInput);
			camundaRequest.setMsoRequestId(requestIdInput);
			camundaRequest.setIsBaseVfModule(isBaseVfModuleInput);
			camundaRequest.setRecipeTimeout(recipeTimeoutInput);
			camundaRequest.setRequestAction(requestActionInput);
			camundaRequest.setServiceInstanceId(serviceInstanceIdInput);
			camundaRequest.setCorrelationId(correlationIdInput);
			camundaRequest.setVnfId(vnfIdInput);
			camundaRequest.setVfModuleId(vfModuleIdInput);
			camundaRequest.setVolumeGroupId(volumeGroupIdInput);
			camundaRequest.setNetworkId(networkIdInput);
			camundaRequest.setConfigurationId(configurationIdInput);
			camundaRequest.setServiceType(serviceTypeInput);
			camundaRequest.setVnfType(vnfTypeInput);
			camundaRequest.setVfModuleType(vfModuleTypeInput);
			camundaRequest.setNetworkType(networkTypeInput);
			camundaRequest.setRecipeParams(recipeParamsInput);
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

			jsonReq = mapper.writeValueAsString(camundaRequest);
			msoLogger.debug("request body is " + jsonReq);
		}catch(Exception e){
			msoLogger.error(MessageEnum.APIH_WARP_REQUEST, "Camunda", "wrapVIDRequest", MsoLogger.ErrorCode.BusinessProcesssError, "Error in APIH Warp request", e);
		}
		return jsonReq;
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
