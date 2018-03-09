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

package org.openecomp.mso.global_tests.asdc.notif_emulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.jboss.shrinkwrap.api.exporter.FileExistsException;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;


public class JsonNotificationData implements INotificationData {

	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();
	
	@JsonProperty("serviceArtifacts")
	@JsonDeserialize(using=JsonArtifactInfoDeserializer.class)
	private List<IArtifactInfo> serviceArtifacts;
	
	@JsonProperty("resources")
	@JsonDeserialize(using=JsonResourceInfoDeserializer.class)
	private List<IResourceInstance> resourcesList;
	
	public JsonNotificationData() {
		
	}
		
	/**
	 * Method instantiate a INotificationData implementation from a JSON file.
	 * 
	 * @param notifFilePath The file path in String
	 * @return A JsonNotificationData instance
	 * @throws IOException in case of the file is not readable or not accessible 
	 */
	public static JsonNotificationData instantiateNotifFromJsonFile(String notifFilePath) throws IOException {
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(notifFilePath+"/notif-structure.json");
		
		if (is == null) {
			throw new FileExistsException("Resource Path does not exist: "+notifFilePath);
		}
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(is, JsonNotificationData.class);
	}
	
	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}

	@Override
	public IArtifactInfo getArtifactMetadataByUUID(String arg0) {
		return null;
	}

	@Override
	public String getDistributionID() {
		return (String)this.attributesMap.get("distributionID");
	}

	@Override
	public List<IResourceInstance> getResources() {
		return resourcesList;
	}

	@Override
	public List<IArtifactInfo> getServiceArtifacts() {
		return this.serviceArtifacts;
	}

	@Override
	public String getServiceDescription() {
		return (String)this.attributesMap.get("serviceDescription");
	}

	@Override
	public String getServiceInvariantUUID() {
		return (String)this.attributesMap.get("serviceInvariantUUID");
	}

	@Override
	public String getServiceName() {
		return (String)this.attributesMap.get("serviceName");
	}

	@Override
	public String getServiceUUID() {
		return (String)this.attributesMap.get("serviceUUID");
	}

	@Override
	public String getServiceVersion() {
		return (String)this.attributesMap.get("serviceVersion");
	}
}
