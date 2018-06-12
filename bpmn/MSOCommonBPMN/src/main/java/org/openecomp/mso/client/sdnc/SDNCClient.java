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

package org.openecomp.mso.client.sdnc;

import java.util.LinkedHashMap;

import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.exception.MapperException;
import org.openecomp.mso.client.sdn.common.BaseClient;
import org.openecomp.mso.client.sdn.common.SdnCommonTasks;
import org.openecomp.mso.client.sdnc.beans.SDNCProperties;
import org.openecomp.mso.client.sdnc.endpoint.SDNCTopology;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

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
			LinkedHashMap<?, ?> output = STOClient.post(jsonRequest);
			msoLogger.info("Validating output...");
			return sdnCommonTasks.validateSDNResponse(output);
	}


	/**
	 * 
	 * @param queryLink
	 *            - takes in a link to topology that needs to be queried
	 *            - creates a json request string and sends it to sdnc
	 *            - receives and validates the linkedhashmap sent back from sdnc
	 * @throws MapperException 
	 * @throws BadResponseException 
	 */
	public String get(String queryLink) throws MapperException, BadResponseException {
			
			String request = "";
			String jsonRequest = sdnCommonTasks.buildJsonRequest(request);
			String targetUrl = properties.getHost() + properties.getPath() + queryLink;
			STOClient.setTargetUrl(targetUrl);
			msoLogger.info("TargetUrl: " + targetUrl);
			HttpHeaders httpHeader = sdnCommonTasks.getHttpHeaders(properties.getAuth());
			STOClient.setHttpHeader(httpHeader);
			msoLogger.info("Running SDNC CLIENT...");
			LinkedHashMap<?, ?> output = STOClient.get(jsonRequest);
			msoLogger.info("Validating output...");
			return sdnCommonTasks.validateSDNResponse(output);
	}

}
