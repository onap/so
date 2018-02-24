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

import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VfModule;
import java.io.Serializable;
import java.util.ArrayList;

/*
 * A simple holder for Service and its associated elements:
 * VnfResource, 1-n VfModule, Network TBD
 */

public class ServiceMacroHolder implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private Service service;
	private ArrayList<VnfResource> vnfResources;
	private ArrayList<NetworkResourceCustomization> networkResourceCustomizations;
	private ArrayList<AllottedResourceCustomization> allottedResourceCustomizations;
	private ArrayList<VnfResourceCustomization> vnfResourceCustomizations;

	public ServiceMacroHolder() {
		super();
		this.service = null;
		this.vnfResources = new ArrayList<VnfResource>();
		this.networkResourceCustomizations = new ArrayList<>();
		this.allottedResourceCustomizations = new ArrayList<>();
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

	public void setVnfResources(ArrayList<VnfResource> vnfResources) {
		this.vnfResources = vnfResources;
	}
	public ArrayList<VnfResource> getVnfResources() {
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

	public void setVnfResourceCustomizations(ArrayList<VnfResourceCustomization> vnfResourceCustomizations) {
		this.vnfResourceCustomizations = vnfResourceCustomizations;
	}
	public ArrayList<VnfResourceCustomization> getVnfResourceCustomizations() {
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
	
	public void setNetworkResourceCustomization(ArrayList<NetworkResourceCustomization> networkResourceCustomizations) {
		this.networkResourceCustomizations = networkResourceCustomizations;
	}
	public ArrayList<NetworkResourceCustomization> getNetworkResourceCustomization() {
		return this.networkResourceCustomizations;
	}
	public void addNetworkResourceCustomization(NetworkResourceCustomization nrc) {
		if (this.networkResourceCustomizations != null) {
			this.networkResourceCustomizations.add(nrc);
		} else {
			this.networkResourceCustomizations = new ArrayList<>();
			this.networkResourceCustomizations.add(nrc);
		}
	}

	public void setAllottedResourceCustomization(ArrayList<AllottedResourceCustomization> allottedResourceCustomizations) {
		this.allottedResourceCustomizations = allottedResourceCustomizations;
	}
	public ArrayList<AllottedResourceCustomization> getAllottedResourceCustomization() {
		return this.allottedResourceCustomizations;
	}
	public void addAllottedResourceCustomization(AllottedResourceCustomization arc) {
		if (this.allottedResourceCustomizations != null) {
			this.allottedResourceCustomizations.add(arc);
		} else {
			this.allottedResourceCustomizations = new ArrayList<>();
			this.allottedResourceCustomizations.add(arc);
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
			sb.append("VnfResources: ");
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
		if (this.networkResourceCustomizations != null && this.networkResourceCustomizations.size() > 0) {
			int i=0;
			sb.append("NetworkResourceCustomizations:");
			for (NetworkResourceCustomization nrc : this.networkResourceCustomizations) {
				sb.append("NRC[").append(i++).append("]: ").append(nrc.toString());
			}
		}
		if (this.allottedResourceCustomizations != null && this.allottedResourceCustomizations.size() > 0) {
			int i=0;
			sb.append("AllottedResourceCustomizations:");
			for (AllottedResourceCustomization arc : this.allottedResourceCustomizations) {
				sb.append("ARC[").append(i++).append("]: ").append(arc.toString());
			}
		}

		return sb.toString();
	}


}
