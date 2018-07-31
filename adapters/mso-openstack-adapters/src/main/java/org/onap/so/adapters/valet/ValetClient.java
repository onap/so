/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.valet;

import org.onap.so.adapters.valet.beans.HeatRequest;
import org.onap.so.adapters.valet.beans.ValetConfirmRequest;
import org.onap.so.adapters.valet.beans.ValetConfirmResponse;
import org.onap.so.adapters.valet.beans.ValetCreateRequest;
import org.onap.so.adapters.valet.beans.ValetCreateResponse;
import org.onap.so.adapters.valet.beans.ValetDeleteRequest;
import org.onap.so.adapters.valet.beans.ValetDeleteResponse;
import org.onap.so.adapters.valet.beans.ValetRollbackRequest;
import org.onap.so.adapters.valet.beans.ValetRollbackResponse;
import org.onap.so.adapters.valet.beans.ValetUpdateRequest;
import org.onap.so.adapters.valet.beans.ValetUpdateResponse;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.onap.so.adapters.valet.GenericValetResponse;

import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ValetClient {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, ValetClient.class);

	@Autowired
	private Environment environment;
	
	private static final String VALET_BASE_URL = "org.onap.so.adapters.valet.base_url";
	private static final String VALET_BASE_PATH = "org.onap.so.adapters.valet.base_path";
	private static final String VALET_AUTH = "org.onap.so.adapters.valet.basic_auth";
	private static final String REQ_ID_HEADER_NAME = "X-RequestID";
	protected static final String NO_STATUS_RETURNED = "no status returned from Valet";
	
	private static final String DEFAULT_BASE_URL = "http://localhost:8080/";
	private static final String DEFAULT_BASE_PATH = "api/valet/placement/v1";
	private static final String DEFAULT_AUTH_STRING = "";
	
	@Autowired
	private ObjectMapper mapper;
	
	protected String baseUrl;
	protected String basePath;
	protected String authString;
	
	/* 
	 * Setup the properties needed from properties file. Each will fall to a default  
	 */
	@PostConstruct
	private void setupParams() {
		try {
            this.baseUrl = this.environment.getProperty(ValetClient.VALET_BASE_URL, ValetClient.DEFAULT_BASE_URL);
            this.basePath = this.environment.getProperty(ValetClient.VALET_BASE_PATH, ValetClient.DEFAULT_BASE_PATH);
			this.authString = this.environment.getProperty(ValetClient.VALET_AUTH, ValetClient.DEFAULT_AUTH_STRING);
		} catch (Exception e) {
			LOGGER.debug("Error retrieving valet properties. " + e.getMessage());
		}
	}
		
	/*
	 * This method will be invoked to send a Create request to Valet. 
	 */
	public GenericValetResponse<ValetCreateResponse> callValetCreateRequest(String requestId, String regionId, String tenantId, String serviceInstanceId,
			String vnfId, String vnfName, String vfModuleId, String vfModuleName, String keystoneUrl, HeatRequest heatRequest) throws Exception {
		ResponseEntity<ValetCreateResponse> response = null;
		GenericValetResponse<ValetCreateResponse> gvr = null;

		try {
			UriBuilder builder = UriBuilder.fromPath(baseUrl).path(basePath).queryParam("requestId", requestId);
			URI uri = builder.build();
			
			ValetCreateRequest vcr = this.createValetCreateRequest(regionId, tenantId, serviceInstanceId, vnfId, vnfName, vfModuleId, vfModuleName, keystoneUrl, heatRequest);
			RestTemplate restTemplate = new RestTemplate();
			String body = mapper.writeValueAsString(vcr);
			HttpHeaders headers = generateHeaders(requestId);
			HttpEntity<String> entity = new HttpEntity<>(body, headers);	
			LOGGER.debug("valet create req: " + uri.toString() + ", headers=" + headers.toString() + ", body=" + body.toString());
			
			response = restTemplate.exchange(uri, HttpMethod.POST, entity, ValetCreateResponse.class);
			gvr = this.getGVRFromResponse(response);
		} catch (Exception e) {
			LOGGER.error("An exception occurred in callValetCreateRequest", e);
			throw e;
		}
		return gvr;
	}
	
	/*
	 * This method will be invoked to send an Update request to Valet. 
	 */
	public GenericValetResponse<ValetUpdateResponse> callValetUpdateRequest(String requestId, String regionId, String tenantId, String serviceInstanceId, 
			String vnfId, String vnfName, String vfModuleId, String vfModuleName, String keystoneUrl, HeatRequest heatRequest) throws Exception {
		ResponseEntity<ValetUpdateResponse> response = null;
		GenericValetResponse<ValetUpdateResponse> gvr = null;

		try {
			UriBuilder builder = UriBuilder.fromPath(baseUrl).path(basePath).queryParam("requestId", requestId);
			URI uri = builder.build();
			
			ValetUpdateRequest vur = this.createValetUpdateRequest(regionId, tenantId, serviceInstanceId, vnfId, vnfName, vfModuleId, vfModuleName, keystoneUrl, heatRequest);
			RestTemplate restTemplate = new RestTemplate();
			String body = mapper.writeValueAsString(vur);
			HttpHeaders headers = generateHeaders(requestId);	
			HttpEntity<String> entity = new HttpEntity<>(body, headers);
			LOGGER.debug("valet update req: " + uri.toString() + ", headers=" + headers.toString() + ", body=" + body.toString());
			
			response = restTemplate.exchange(uri, HttpMethod.PUT, entity, ValetUpdateResponse.class);
			gvr = this.getGVRFromResponse(response);
		} catch (Exception e) {
			LOGGER.error("An exception occurred in callValetUpdateRequest", e);
			throw e;
		}
		return gvr;
	}
	
	/*
	 * This method will be invoked to send a Delete request to Valet.
	 */
	public GenericValetResponse<ValetDeleteResponse> callValetDeleteRequest(String requestId, String regionId, String tenantId, String vfModuleId, String vfModuleName) throws Exception {
		ResponseEntity<ValetDeleteResponse> response = null;
		GenericValetResponse<ValetDeleteResponse> gvr = null;

		try {
			UriBuilder builder = UriBuilder.fromPath(baseUrl).path(basePath).queryParam("requestId", requestId);
			URI uri = builder.build();
			
			ValetDeleteRequest vdr = this.createValetDeleteRequest(regionId, tenantId, vfModuleId, vfModuleName);
			RestTemplate restTemplate = new RestTemplate();
			String body = mapper.writeValueAsString(vdr);
			HttpHeaders headers = generateHeaders(requestId);
			HttpEntity<String> entity = new HttpEntity<>(body, headers);
			LOGGER.debug("valet delete req: " + uri.toString() + ", headers=" + headers.toString() + ", body=" + body.toString());
			
			response = restTemplate.exchange(uri, HttpMethod.DELETE, entity, ValetDeleteResponse.class);
			gvr = this.getGVRFromResponse(response);
		} catch (Exception e) {
			LOGGER.error("An exception occurred in callValetDeleteRequest", e);
			throw e;
		}
		return gvr;
	}
	
	/*
	 * This method is called to invoke a Confirm request to Valet. 
	 */
	public GenericValetResponse<ValetConfirmResponse> callValetConfirmRequest(String requestId, String stackId) throws Exception {
		ResponseEntity<ValetConfirmResponse> response = null;
		GenericValetResponse<ValetConfirmResponse> gvr = null;

		try {
			UriBuilder builder = UriBuilder.fromPath(this.baseUrl).path(this.basePath).path("{requestId}/confirm/");
			URI uri = builder.build(requestId);
			
			ValetConfirmRequest vcr = this.createValetConfirmRequest(stackId);
			RestTemplate restTemplate = new RestTemplate();
			String body = mapper.writeValueAsString(vcr);
			HttpHeaders headers = generateHeaders(requestId);
			HttpEntity<String> entity = new HttpEntity<>(body, headers);
			LOGGER.debug("valet confirm req: " + uri.toString() + ", headers=" + headers.toString() + ", body=" + body);
			
			response = restTemplate.exchange(uri, HttpMethod.PUT, entity, ValetConfirmResponse.class);
			gvr = this.getGVRFromResponse(response);
		} catch (Exception e) {
			LOGGER.error("An exception occurred in callValetConfirmRequest", e);
			throw e;
		}
		return gvr;
	}
	
	/* 
	 * This method is called to invoke a Rollback request to Valet.
	 */
	public GenericValetResponse<ValetRollbackResponse> callValetRollbackRequest(String requestId, String stackId, Boolean suppressRollback, String errorMessage) throws Exception {
		ResponseEntity<ValetRollbackResponse> response = null;
		GenericValetResponse<ValetRollbackResponse> gvr = null;

		try {
			UriBuilder builder = UriBuilder.fromPath(this.baseUrl).path(this.basePath).path("{requestId}/rollback/");
			URI uri = builder.build(requestId);
			
			ValetRollbackRequest vrr = this.createValetRollbackRequest(stackId, suppressRollback, errorMessage);
			RestTemplate restTemplate = new RestTemplate();
			String body = mapper.writeValueAsString(vrr);
			HttpHeaders headers = generateHeaders(requestId);
			HttpEntity<String> entity = new HttpEntity<>(body, headers);
			LOGGER.debug("valet rollback req: " + uri.toString() + ", headers=" + headers.toString() + ", body=" + body.toString());
			
			response = restTemplate.exchange(uri, HttpMethod.PUT, entity, ValetRollbackResponse.class);
			gvr = this.getGVRFromResponse(response);
		} catch (Exception e) {
			LOGGER.error("An exception occurred in callValetRollbackRequest", e);
			throw e;
		}
		return gvr;
	}
	
	/*
	 * This method is to construct the ValetCreateRequest pojo
	 */
	private ValetCreateRequest createValetCreateRequest(String regionId, String tenantId, String serviceInstanceId,
			String vnfId, String vnfName, String vfModuleId, String vfModuleName, String keystoneUrl, HeatRequest heatRequest) {
		ValetCreateRequest vcr = new ValetCreateRequest();
		vcr.setHeatRequest(heatRequest);
		vcr.setKeystoneUrl(keystoneUrl);
		vcr.setRegionId(regionId);
		vcr.setServiceInstanceId(serviceInstanceId);
		vcr.setTenantId(tenantId);
		vcr.setVfModuleId(vfModuleId);
		vcr.setVfModuleName(vfModuleName);
		vcr.setVnfId(vnfId);
		vcr.setVnfName(vnfName);
		
		return vcr;
	}
	
	/*
	 * This method is to construct the ValetUpdateRequest pojo
	 */
	private ValetUpdateRequest createValetUpdateRequest(String regionId, String tenantId, String serviceInstanceId,
			String vnfId, String vnfName, String vfModuleId, String vfModuleName, String keystoneUrl, HeatRequest heatRequest) {
		ValetUpdateRequest vur = new ValetUpdateRequest();
		vur.setHeatRequest(heatRequest);
		vur.setKeystoneUrl(keystoneUrl);
		vur.setRegionId(regionId == null ? "" : regionId);
		vur.setServiceInstanceId(serviceInstanceId == null ? "" : serviceInstanceId);
		vur.setTenantId(tenantId == null ? "" : tenantId);
		vur.setVfModuleId(vfModuleId == null ? "" : vfModuleId);
		vur.setVfModuleName(vfModuleName == null ? "" : vfModuleName);
		vur.setVnfId(vnfId == null ? "" : vnfId);
		vur.setVnfName(vnfName == null ? "" : vnfName);
		
		return vur;
	}
	
	/*
	 * This method is to construct the ValetDeleteRequest pojo
	 */
	private ValetDeleteRequest createValetDeleteRequest(String regionId, String tenantId, String vfModuleId, String vfModuleName) {
		ValetDeleteRequest vdr = new ValetDeleteRequest();
		vdr.setRegionId(regionId == null ? "" : regionId);
		vdr.setTenantId(tenantId == null ? "" : tenantId);
		vdr.setVfModuleId(vfModuleId == null ? "" : vfModuleId);
		vdr.setVfModuleName(vfModuleName == null ? "" : vfModuleName);
		
		return vdr;
	}
	
	/*
	 * This method is to construct the ValetDeleteRequest pojo
	 */
	private ValetConfirmRequest createValetConfirmRequest(String stackId) {
		ValetConfirmRequest vcr = new ValetConfirmRequest();
		vcr.setStackId(stackId);
		
		return vcr;
	}
	
	/*
	 * This method is to construct the ValetRollbackRequest pojo
	 */
	private ValetRollbackRequest createValetRollbackRequest(String stackId, Boolean suppressRollback, String errorMessage) {
		ValetRollbackRequest vrr = new ValetRollbackRequest();
		vrr.setStackId(stackId);
		vrr.setSuppressRollback(suppressRollback);
		vrr.setErrorMessage(errorMessage);
		
		return vrr;
	}
	
	private HttpHeaders generateHeaders(String requestId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		if (!(this.authString == null || this.authString.isEmpty())) {
			headers.add("Authorization",  "Basic " + this.authString);
		}
		headers.add(ValetClient.REQ_ID_HEADER_NAME, requestId);
		
		return headers;
	}
	
	private <T> GenericValetResponse<T> getGVRFromResponse(ResponseEntity<T> response) {
		GenericValetResponse<T> gvr = null;
		if (response != null) {
			T responseObj = response.getBody();
			gvr = new GenericValetResponse<>(response.getStatusCodeValue(), ValetClient.NO_STATUS_RETURNED, responseObj);
			
		} else {
			gvr = new GenericValetResponse<>(-1, ValetClient.NO_STATUS_RETURNED, null);
		}
		return gvr;
	}
}
