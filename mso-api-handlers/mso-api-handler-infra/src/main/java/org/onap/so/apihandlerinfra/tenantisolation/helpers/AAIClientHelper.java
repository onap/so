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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolation.exceptions.AAIClientCallFailed;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class AAIClientHelper {
	
	private static Logger logger = LoggerFactory.getLogger(AAIClientHelper.class);

	/**
	 * Get managing ECOMP Environment Info from A&AI
	 * @param id = operationalEnvironmentId 
	 * @return AAIResultWrapper object
	 */
	public AAIResultWrapper getAaiOperationalEnvironment(String id){

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, id);
		uri.depth(Depth.ZERO); 
		AAIResourcesClient client = this.getClient();
		return client.get(uri, NotFoundException.class);
	}
	

	/**
	 * Update managing ECOMP Environment Info from A&AI
	 * @param id = operationalEnvironmentId
	 * @param AAIOperationalEnvironment object
	 */
	public void updateAaiOperationalEnvironment(String id, OperationalEnvironment aaiRequest){

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, id);
		AAIResourcesClient client = this.getClient();
		client.update(uri, aaiRequest);

	}
	

	public void updateAaiOperationalEnvironment(String operationalEnvironmentId, Map<String, String> payload) throws Exception {
		try {
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, operationalEnvironmentId);
			AAIResourcesClient aaiClient = this.getClient();
			aaiClient.update(uri, payload);
		}
		catch(Exception ex) {
			logStackTrace(ex);
			throw new AAIClientCallFailed("Call to A&AI failed!", ex);
		} 
	}
	
	/**
	 * Create an Operational Environment object in A&AI
	 * @param AAIOperationalEnvironment object
	 */
	public void createOperationalEnvironment(OperationalEnvironment operationalEnvironment){

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, operationalEnvironment.getOperationalEnvironmentId());
		AAIResourcesClient client = this.getClient();
		client.create(uri, operationalEnvironment);
	}
	
	/**
	 * Create a relationship between ECOMP managing and VNF Operational Environments
	 * @param managingEcompOperationalEnvironmentId
	 * @param vnfOperationalEnvironmentId
	 * @throws Exception
	 */
	public void createRelationship(String managingEcompOperationalEnvironmentId, String vnfOperationalEnvironmentId) {

		AAIResourceUri ecompEnvUri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, managingEcompOperationalEnvironmentId);
		AAIResourceUri vnfEnvUri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, vnfOperationalEnvironmentId);
		AAIResourcesClient client = this.getClient();
		client.connect(vnfEnvUri, ecompEnvUri);

	}
	
	private void logStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
	}
	
	protected AAIResourcesClient getClient() {
		return new AAIResourcesClient();
	}
}
