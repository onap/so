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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.AAIClientCallFailed;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.entities.uri.Depth;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.logger.MsoLogger;

public class AAIClientHelper {
	
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    
    public AAIClientHelper() {
		super();
	}
    
    public AAIClientHelper(String serviceName, String requestId) {
		super();
		MsoLogger.setServiceName (serviceName);
		MsoLogger.setLogContext(requestId, "");
	}

	/**
	 * Get managing ECOMP Environment Info from A&AI
	 * @param id = operationalEnvironmentId 
	 * @return AAIResultWrapper object
	 */
	public AAIResultWrapper getAaiOperationalEnvironment(String id) throws Exception {
		try {
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, id);
			uri.depth(Depth.ZERO); //Do not return relationships if any
			AAIResourcesClient aaiClient = this.getClient();
			AAIResultWrapper result = aaiClient.get(uri);
			return result;
		}
		catch(Exception ex) {
			logStackTrace(ex);
			throw new AAIClientCallFailed("Call to A&AI failed!", ex);
		} 
	}
	

	/**
	 * Update managing ECOMP Environment Info from A&AI
	 * @param id = operationalEnvironmentId
	 * @param AAIOperationalEnvironment object
	 */
	public void updateAaiOperationalEnvironment(String id, AAIOperationalEnvironment aaiRequest) throws Exception {
		try {
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, id);
			AAIResourcesClient aaiClient = this.getClient();
			aaiClient.update(uri, aaiRequest);
		}
		catch(Exception ex) {
			logStackTrace(ex);
			throw new AAIClientCallFailed("Call to A&AI failed!", ex);
		} 
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
	public void createOperationalEnvironment(AAIOperationalEnvironment operationalEnvironment) throws Exception {
		try {
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, operationalEnvironment.getOperationalEnvironmentId());
			AAIResourcesClient aaiClient = this.getClient();
			aaiClient.create(uri, operationalEnvironment);
		}
		catch(Exception ex) {
			logStackTrace(ex);
			throw new AAIClientCallFailed("Call to A&AI failed!", ex);
		} 
	}
	
	/**
	 * Create a relationship between ECOMP managing and VNF Operational Environments
	 * @param managingEcompOperationalEnvironmentId
	 * @param vnfOperationalEnvironmentId
	 * @throws Exception
	 */
	public void createRelationship(String managingEcompOperationalEnvironmentId, String vnfOperationalEnvironmentId) throws Exception {
		try {
			AAIResourceUri ecompEnvUri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, managingEcompOperationalEnvironmentId);
			AAIResourceUri vnfEnvUri = AAIUriFactory.createResourceUri(AAIObjectType.OPERATIONAL_ENVIRONMENT, vnfOperationalEnvironmentId);
			AAIResourcesClient aaiClient = this.getClient();
			aaiClient.connect(vnfEnvUri, ecompEnvUri);
		}
		catch(Exception ex) {
			logStackTrace(ex);
			throw new AAIClientCallFailed("Call to A&AI failed!", ex);
		} 
	}
	
	private void logStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		msoLogger.debug(sw.toString());
	}
	
	protected AAIResourcesClient getClient() {
		return new AAIResourcesClient();
	}
}
