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

package org.onap.so.client.sdnc;

import java.util.LinkedHashMap;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.onap.so.bpmn.common.baseclient.BaseClient;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCProperties;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SDNCClient {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCClient.class);
	private BaseClient<String, LinkedHashMap<?, ?>> STOClient = new BaseClient<>();

	@Autowired
	private SDNCProperties properties;
	@Autowired
	private SdnCommonTasks sdnCommonTasks;
	/**
	 * 
	 * @param request
	 *            - takes in a generated object from sdnc client
	 *            - creates a json request string and sends it to sdnc
	 *            - receives and validates the linkedhashmap sent back from sdnc
	 * @throws MapperException 
	 * @throws BadResponseException 
	 */
	public String post(Object request, SDNCTopology topology) throws MapperException, BadResponseException {
			String jsonRequest = sdnCommonTasks.buildJsonRequest(request);
			String targetUrl = properties.getHost() + properties.getPath() + ":" + topology.toString() + "/";
			STOClient.setTargetUrl(targetUrl);
			HttpHeaders httpHeader = sdnCommonTasks.getHttpHeaders(properties.getAuth());
			STOClient.setHttpHeader(httpHeader);
			msoLogger.info("Running SDNC CLIENT for TargetUrl: " + targetUrl);
			LinkedHashMap<?, ?> output = STOClient.post(jsonRequest, new ParameterizedTypeReference<LinkedHashMap<? ,?>>() {});
			Optional<String> sdncResponse = logSDNCResponse(output);
			if(sdncResponse.isPresent()){
				msoLogger.info(sdncResponse.get());
			}
			msoLogger.info("Validating output...");
			return sdnCommonTasks.validateSDNResponse(output);
	}

	protected Optional<String> logSDNCResponse(LinkedHashMap<?, ?> output) {
		ObjectMapper mapper = new ObjectMapper();
		String sdncOutput = "";
		try {
			sdncOutput = mapper.writeValueAsString(output);
			return Optional.of(sdncOutput);
		} catch (JsonProcessingException e) {
			msoLogger.debug("Failed to map response from sdnc to json string for logging purposes.");
		}
		return Optional.empty();
	}

	/**
	 * 
	 * @param queryLink
	 *            - takes in a link to topology that needs to be queried
	 *            - creates a json request string and sends it to sdnc
	 *            - receives and validates the linkedhashmap sent back from sdnc
	 *            	 * 
	 * @throws MapperException 
	 * @throws BadResponseException 
	 */
	public String get(String queryLink) throws MapperException, BadResponseException {
			
			String request = "";
			String jsonRequest = sdnCommonTasks.buildJsonRequest(request);
			String targetUrl = UriBuilder.fromUri(properties.getHost()).path(queryLink).build().toString();			
			STOClient.setTargetUrl(targetUrl);
			msoLogger.info("TargetUrl: " + targetUrl);
			HttpHeaders httpHeader = sdnCommonTasks.getHttpHeaders(properties.getAuth());
			STOClient.setHttpHeader(httpHeader);
			msoLogger.info("Running SDNC CLIENT...");
			LinkedHashMap<?, ?> output = STOClient.get(jsonRequest, new ParameterizedTypeReference<LinkedHashMap<? ,?>>() {});
			msoLogger.info("Validating output...");
			return sdnCommonTasks.validateSDNGetResponse(output);
	}

}
