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

package org.openecomp.mso.db.catalog.beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Set;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class ToscaCsar extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String artifactUUID;
	private String name;
	private String artifactChecksum;
	private String url;
	private String description;
	private Timestamp created;
	private Set<Service> services;
	
	public ToscaCsar() { }
	
	public String getArtifactUUID() {
		return artifactUUID;
	}
	
	public void setArtifactUUID(String artifactUUID) {
		this.artifactUUID = artifactUUID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getArtifactChecksum() {
		return artifactChecksum;
	}
	
	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Timestamp getCreated() {
		return created;
	}
	
	public void setCreated(Timestamp created) {
		this.created = created;
	}
	
	public Set<Service> getServices() {
		return services;
	}
	
	public void setServices(Set<Service> services) {
		this.services = services;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TOSCACSAR: artifactUUID=").append(artifactUUID).append(",name=").append(name).append(",version=")
            .append(version).append(",description=").append(description).append(",artifactChecksum=")
            .append(artifactChecksum).append(",url=").append(url);
		for (Service service : services) {
			sb.append("\n").append(service.toString());
		}
		if (created != null) {
		        sb.append (",created=");
		        sb.append (DateFormat.getInstance().format(created));
		}
		return sb.toString();
	}
}
