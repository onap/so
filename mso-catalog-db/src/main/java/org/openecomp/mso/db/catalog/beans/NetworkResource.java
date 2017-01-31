/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.db.catalog.beans;


import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

import java.sql.Timestamp;
import java.text.DateFormat;

public class NetworkResource extends MavenLikeVersioning {
	private int id;
	private String networkType;
	private String orchestrationMode = null;
	private String description = null;
	private int templateId;
	private String neutronNetworkType = null;
	private String aicVersionMin = null;
	private String aicVersionMax = null;
	
	private Timestamp created;
	
	public NetworkResource() {}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	
	public String getOrchestrationMode() {
		return orchestrationMode;
	}
	public void setOrchestrationMode(String orchestrationMode) {
		this.orchestrationMode = orchestrationMode;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getTemplateId () {
		return templateId;
	}
	
	public void setTemplateId (int templateId) {
		this.templateId = templateId;
	}
	
	public String getNeutronNetworkType() {
		return neutronNetworkType;
	}

	public void setNeutronNetworkType(String neutronNetworkType) {
		this.neutronNetworkType = neutronNetworkType;
	}
	
	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
		
	public String getAicVersionMin() {
		return aicVersionMin;
	}

	public void setAicVersionMin(String aicVersionMin) {
		this.aicVersionMin = aicVersionMin;
	}

	public String getAicVersionMax() {
		return aicVersionMax;
	}

	public void setAicVersionMax(String aicVersionMax) {
		this.aicVersionMax = aicVersionMax;
	}

	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("NETWORK=");
		sb.append(networkType);
		sb.append(",version=");
		sb.append(version);
		sb.append(",mode=");
		sb.append(orchestrationMode);
		sb.append(",template=");
		sb.append(templateId);
		sb.append(",neutronType=");
		sb.append(neutronNetworkType);
		sb.append(",aicVersionMin=");
		sb.append(aicVersionMin);
		sb.append(",aicVersionMax=");
		sb.append(aicVersionMax);
		
		sb.append("id=");
		sb.append(id);
		
		if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		return sb.toString();
	}
}
