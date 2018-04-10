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
import java.util.ArrayList;
import java.util.List;

/*
 * A simple holder for Service and its associated elements:
 * VnfResource, 1-n VfModule, Network TBD
 */

public class ServiceMacroHolder implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private Service service;
	private List<VnfResource> vnfResources;
	private List<NetworkResourceCustomization> networkResourceCustomization;
	private List<AllottedResourceCustomization> allottedResourceCustomization;
	private List<VnfResourceCustomization> vnfResourceCustomizations;

	public ServiceMacroHolder() {
		super();
		this.service = null;
		this.vnfResources = new ArrayList<>();
		this.networkResourceCustomization = new ArrayList<>();
		this.allottedResourceCustomization = new ArrayList<>();
		this.vnfResourceCustomizations = new ArrayList<>();
	}
	public ServiceMacroHolder(Service service) {
		this();
		this.service = service;
	}

	public Service getService() {
		return this.service;
	}
	public void setService(Service service) {
		this.service = service;
	}

	public void setVnfResources(List<VnfResource> vnfResources) {
		this.vnfResources = vnfResources;
	}
	public List<VnfResource> getVnfResources() {
		return this.vnfResources;
	}
	public void addVnfResource(VnfResource vr) {
		if (vr != null) {
			if (this.vnfResources != null) {
				this.vnfResources.add(vr);
			} else {
				this.vnfResources = new ArrayList<>();
				this.vnfResources.add(vr);
			}
		}
	}

	public void setVnfResourceCustomizations(List<VnfResourceCustomization> vnfResourceCustomizations) {
		this.vnfResourceCustomizations = vnfResourceCustomizations;
	}
	public List<VnfResourceCustomization> getVnfResourceCustomizations() {
		return this.vnfResourceCustomizations;
	}
	public void addVnfResourceCustomizations(VnfResourceCustomization vrc) {
		if (vrc != null) {
			if (this.vnfResourceCustomizations != null) {
				this.vnfResourceCustomizations.add(vrc);
			} else {
				this.vnfResourceCustomizations = new ArrayList<>();
				this.vnfResourceCustomizations.add(vrc);
			}
		}
	}
	
	public void setNetworkResourceCustomization(List<NetworkResourceCustomization> networkResourceCustomizations) {
		this.networkResourceCustomization = networkResourceCustomizations;
	}
	public List<NetworkResourceCustomization> getNetworkResourceCustomization() {
		return this.networkResourceCustomization;
	}
	public void addNetworkResourceCustomizations(NetworkResourceCustomization nrc) {
		if (this.networkResourceCustomization != null) {
			this.networkResourceCustomization.add(nrc);
		} else {
			this.networkResourceCustomization = new ArrayList<>();
			this.networkResourceCustomization.add(nrc);
		}
	}

	public void setAllottedResourceCustomization(List<AllottedResourceCustomization> allottedResourceCustomizations) {
		this.allottedResourceCustomization = allottedResourceCustomizations;
	}
	public List<AllottedResourceCustomization> getAllottedResourceCustomization() {
		return this.allottedResourceCustomization;
	}
	public void addAllottedResourceCustomization(AllottedResourceCustomization arc) {
		if (this.allottedResourceCustomization != null) {
			this.allottedResourceCustomization.add(arc);
		} else {
			this.allottedResourceCustomization = new ArrayList<>();
			this.allottedResourceCustomization.add(arc);
		}
	}

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServicePlus: ");
		if (this.service != null) {
			sb.append("service: ").append(this.service.toString());
		} else {
			sb.append("service: null");
		}
		if (this.vnfResourceCustomizations != null && this.vnfResourceCustomizations.size() > 0) {
			int i=0;
			sb.append("vnfResourceCustomization: ");
			for (VnfResourceCustomization vrc : this.vnfResourceCustomizations) {
				sb.append(", vnfResourceCustomization[").append(i++).append("]:").append(vrc.toString());
			}
		} else {
			sb.append("none");
		}
		if (this.vnfResources != null && this.vnfResources.size() > 0) {
			int i=0;
			sb.append("VnfResources: ");
			for (VnfResource vr : this.vnfResources) {
				sb.append(", vnfResource[").append(i++).append("]:").append(vr.toString());
			}
		} else {
			sb.append("none");
		}
		if (this.networkResourceCustomization != null && this.networkResourceCustomization.size() > 0) {
			int i=0;
			sb.append("NetworkResourceCustomizations:");
			for (NetworkResourceCustomization nrc : this.networkResourceCustomization) {
				sb.append("NRC[").append(i++).append("]: ").append(nrc.toString());
			}
		}
		if (this.allottedResourceCustomization != null && this.allottedResourceCustomization.size() > 0) {
			int i=0;
			sb.append("AllottedResourceCustomizations:");
			for (AllottedResourceCustomization arc : this.allottedResourceCustomization) {
				sb.append("ARC[").append(i++).append("]: ").append(arc.toString());
			}
		}

		return sb.toString();
	}


}
