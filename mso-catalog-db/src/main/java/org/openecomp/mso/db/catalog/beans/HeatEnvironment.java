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

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class HeatEnvironment extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String artifactUuid;
	private String name = null;
	private String description = null;
	private String environment = null;
	private String artifactChecksum;

	private Timestamp created;

	public HeatEnvironment() {}

	public String getArtifactUuid() {
		return this.artifactUuid;
	}
	public void setArtifactUuid(String artifactUuid) {
		this.artifactUuid = artifactUuid;
	}

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getEnvironment() {
		return this.environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

    @Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("Artifact UUID=").append(this.artifactUuid);
        sb.append (", name=");
        sb.append (name);
        sb.append (", version=");
        sb.append (version);
        sb.append(", description=");
        sb.append (this.description == null ? "null" : this.description);
        sb.append(", body=");
        sb.append (this.environment == null ? "null" : this.environment);
		if (this.created != null) {
	        sb.append (",creationTimestamp=");
	        sb.append (DateFormat.getInstance().format(this.created));
	    }
		return sb.toString();
	}
}
