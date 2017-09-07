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

import java.io.Serializable;
import java.sql.Timestamp;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;
import org.openecomp.mso.db.catalog.beans.AllottedResource;

public class AllottedResourceCustomization extends MavenLikeVersioning implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String modelCustomizationUuid;
	private String arModelUuid;
	private Timestamp created;
	private String modelInstanceName;
	private String providingServiceModelInvariantUuid;
	private String targetNetworkRole;
	private String nfFunction;
	private String nfType;
	private String nfRole;
	private String nfNamingCode;
	private Integer minInstances;
	private Integer maxInstances;
	private AllottedResource ar = null;

	public AllottedResourceCustomization() {
		super();
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public String getArModelUuid() {
		return this.arModelUuid;
	}
	public void setArModelUuid(String arModelUuid) {
		this.arModelUuid = arModelUuid;
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
	public AllottedResource getAllottedResource() {
		return this.ar;
	}
	public void setAllottedResource(AllottedResource ar) {
		this.ar = ar;
	}
	public String getProvidingServiceModelInvariantUuid() {
		return this.providingServiceModelInvariantUuid;
	}
	public void setProvidingServiceModelInvariantUuid(String providingServiceModelInvariantUuid) {
		this.providingServiceModelInvariantUuid = providingServiceModelInvariantUuid;
	}
	public String getTargetNetworkRole() {
		return this.targetNetworkRole;
	}
	public void setTargetNetworkRole(String targetNetworkRole) {
		this.targetNetworkRole = targetNetworkRole;
	}
	public String getNfFunction() {
		return this.nfFunction;
	}
	public void setNfFunction(String nfFunction) {
		this.nfFunction = nfFunction;
	}
	public String getNfType() {
		return this.nfType;
	}
	public void setNfType(String nfType) {
		this.nfType = nfType;
	}
	public String getNfRole() {
		return this.nfRole;
	}
	public void setNfRole(String nfRole) {
		this.nfRole = nfRole;
	}
	public String getNfNamingCode() {
		return this.nfNamingCode;
	}
	public void setNfNamingCode(String nfNamingCode) {
		this.nfNamingCode = nfNamingCode;
	}
	public Integer getMinInstances() {
		return this.minInstances;
	}
	public void setMinInstances(Integer minInstances) {
		this.minInstances = minInstances;
	}
	public Integer getMaxInstances() {
		return this.maxInstances;
	}
	public void setMaxInstances(Integer maxInstances) {
		this.maxInstances = maxInstances;
	}

	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("modelCustomizationUuid=");
		sb.append(this.modelCustomizationUuid);
		sb.append(",modelInstanceName=");
		sb.append(this.modelInstanceName);
		sb.append(",modelInstanceName=");
		sb.append(this.modelInstanceName);
		sb.append(",created=");
		sb.append(this.created);
		sb.append(",ar=");
		sb.append(this.ar);

		return sb.toString();
	}

}
