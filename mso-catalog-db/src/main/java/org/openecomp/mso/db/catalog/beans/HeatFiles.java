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

public class HeatFiles extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String artifactUuid;
	private String description = null;
	private String fileName;
	private String fileBody;
	private Timestamp created;
	private String version;
	private String artifactChecksum;

	public HeatFiles() {}

	public String getArtifactUuid() {
		return this.artifactUuid;
	}
	public void setArtifactUuid(String artifactUuid) {
		this.artifactUuid = artifactUuid;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getFileName() {
		return this.fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileBody() {
		return this.fileBody;
	}
	public void setFileBody(String fileBody) {
		this.fileBody = fileBody;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getAsdcUuid() {
		return this.artifactUuid;
	}
	public void setAsdcUuid(String artifactUuid) {
		this.artifactUuid = artifactUuid;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}
	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("artifactUuid=").append(this.artifactUuid);
		if (this.description == null) {
			sb.append(", description=null");
		} else {
			sb.append(", description=").append(this.description);
		}
		if (this.fileName == null) {
			sb.append(", fileName=null");
		} else {
			sb.append(",fileName=").append(this.fileName);
		}
		if (this.fileBody == null) {
			sb.append(", fileBody=null");
		} else {
			sb.append(",fileBody=").append(this.fileBody);
		}
		sb.append(", artifactChecksum=").append(this.artifactChecksum);
		if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
		return sb.toString();
	}
}
