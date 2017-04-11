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

import java.sql.Timestamp;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class AllottedResourceCustomization extends MavenLikeVersioning {

	private String modelCustomizationUuid;
	private String modelUuid;
	private String modelInvariantUuid;
	private String modelVersion = null; // duplicate of version kept in parent class
	private String modelName;
	private String description;
	private Timestamp created;
	private String modelInstanceName;

	public AllottedResourceCustomization() {
		super();
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public String getModelUuid() {
		return this.modelUuid;
	}
	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}

	public String getModelInvariantUuid() {
		return this.modelInvariantUuid;
	}
	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}

	public String getModelName() {
		return this.modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreated() {
		return this.created;
	}
	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getModelInstanceName() {
		return this.modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}
	public String getModelVersion() {
		return this.modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("modelName=");
		sb.append(this.modelName);
		sb.append(",modelVersion=");
		sb.append(this.modelVersion);
		sb.append(",version=");
		sb.append(this.version);
		sb.append(",modelUuid=");
		sb.append(this.modelUuid);
		sb.append(",modelInvariantUuid=");
		sb.append(this.modelInvariantUuid);
		sb.append(",modelCustomizationUuid=");
		sb.append(this.modelCustomizationUuid);
		sb.append(",modelInstanceName=");
		sb.append(this.modelInstanceName);
		sb.append(",description=");
		sb.append(this.description);
		sb.append(",modelInstanceName=");
		sb.append(this.modelInstanceName);
		sb.append(",created=");
		sb.append(this.created);

		return sb.toString();
	}

}
