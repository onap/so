/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sniro;

import java.util.LinkedHashMap;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.common.baseclient.BaseClient;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.sniro.beans.ManagerProperties;
import org.onap.so.client.sniro.beans.SniroConductorRequest;
import org.onap.so.client.sniro.beans.SniroManagerRequest;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;


@Component
public class SniroClient {

	private static final MsoLogger log = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SniroClient.class);

	@Autowired
	private ManagerProperties managerProperties;

	@Autowired
	private SniroValidator validator;


	/**
	 * Makes a rest call to sniro manager to perform homing and licensing for a
	 * list of demands
	 *
	 * @param homingRequest
	 * @return
	 * @throws JsonProcessingException
	 * @throws BpmnError
	 */
	public void postDemands(SniroManagerRequest homingRequest) throws BadResponseException, JsonProcessingException{
		log.trace("Started Sniro Client Post Demands");
		String url = managerProperties.getHost() + managerProperties.getUri().get("v2");
		log.debug("Post demands url: " + url);
		log.debug("Post demands payload: " + homingRequest.toJsonString());

		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		header.set("Authorization", managerProperties.getHeaders().get("auth"));
		header.set("X-patchVersion", managerProperties.getHeaders().get("patchVersion"));
		header.set("X-minorVersion", managerProperties.getHeaders().get("minorVersion"));
		header.set("X-latestVersion", managerProperties.getHeaders().get("latestVersion"));
		BaseClient<String, LinkedHashMap<?, ?>> baseClient = new BaseClient<>();

		baseClient.setTargetUrl(url);
		baseClient.setHttpHeader(header);

		LinkedHashMap<?, ?> response = baseClient.post(homingRequest.toJsonString(), new ParameterizedTypeReference<LinkedHashMap<? ,?>>() {});
		validator.validateDemandsResponse(response);
		log.trace("Completed Sniro Client Post Demands");
	}

	/**
	 * Makes a rest call to sniro conductor to notify them of successful or unsuccessful vnf
	 * creation for previously homed resources
	 *
	 * TODO Temporarily being used in groovy therefore can not utilize autowire. Once java "release"
	 * subflow is developed it will be refactored to use autowire.
	 *
	 * @param releaseRequest
	 * @return
	 * @throws BadResponseException
	 */
	public void postRelease(SniroConductorRequest releaseRequest) throws BadResponseException {
		log.trace("Started Sniro Client Post Release");
		String url = UrnPropertiesReader.getVariable("sniro.conductor.host") + UrnPropertiesReader.getVariable("sniro.conductor.uri");
		log.debug("Post release url: " + url);
		log.debug("Post release payload: " + releaseRequest.toJsonString());

		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		header.set("Authorization", UrnPropertiesReader.getVariable("sniro.conductor.headers.auth"));
		BaseClient<String, LinkedHashMap<?, ?>> baseClient = new BaseClient<>();

		baseClient.setTargetUrl(url);
		baseClient.setHttpHeader(header);

		LinkedHashMap<?, ?> response = baseClient.post(releaseRequest.toJsonString(), new ParameterizedTypeReference<LinkedHashMap<? ,?>>() {});
		SniroValidator v = new SniroValidator();
		v.validateReleaseResponse(response);
		log.trace("Completed Sniro Client Post Release");
	}

}
