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

public class NetworkResourceCustomization extends MavenLikeVersioning{

	private String modelCustomizationUuid;
	private String modelName;
	private String modelInstanceName;
	private String modelUuid;
	private String modelVersion;
	private String modelInvariantUuid;
	private int networkResourceId = 0;
	private Timestamp created;

	// These fields are not in the table directly - but I'm adding them here for storage in the objects we're dealing with
	private NetworkResource networkResource = null;
	private String networkType = null;

	public NetworkResourceCustomization() {
		super();
	}

	public int getNetworkResourceId() {
		return this.networkResourceId;
	}
	public void setNetworkResourceId(int networkResourceId) {
		this.networkResourceId = networkResourceId;
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

	public String getModelVersion() {
		return this.modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public String getModelInstanceName() {
		return  this.modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}

	public String getModelName() {
		return  this.modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public NetworkResource getNetworkResource() {
		return this.networkResource;
	}
	public void setNetworkResource(NetworkResource networkResource) {
		this.networkResource = networkResource;
	}

	public String getNetworkType() {
		return this.networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	public Timestamp getCreated() {
		return this.created;
	}
	public void setCreated(Timestamp timestamp) {
		this.created = timestamp;
	}


	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("modelName=");
		sb.append(this.modelName);
		sb.append("modelUuid=");
		sb.append(this.modelUuid);
		sb.append("modelUuid=");
		sb.append(this.modelUuid);
		sb.append("modelInvariantUuid=");
		sb.append(this.modelInvariantUuid);
		sb.append("modelVersion=");
		sb.append(this.modelVersion);
		sb.append("modelCustomizationUuid=");
		sb.append(this.modelCustomizationUuid);
		sb.append("modelInstanceName=");
		sb.append(this.modelInstanceName);
		sb.append("networkResourceId=");
		sb.append(this.networkResourceId);
		sb.append("networkType=");
		sb.append(this.networkType);

		return sb.toString();
	}

}