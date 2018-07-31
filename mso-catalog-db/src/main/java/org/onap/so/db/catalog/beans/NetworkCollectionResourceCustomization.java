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

package org.onap.so.db.catalog.beans;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@DiscriminatorValue(value = "NetworkCollection")
public class NetworkCollectionResourceCustomization extends CollectionResourceCustomization {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4571173204548832466L;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "networkResourceCustomization")
	private Set<CollectionNetworkResourceCustomization> networkResourceCustomization;

	@LinkedResource
	public Set<CollectionNetworkResourceCustomization> getNetworkResourceCustomization() {
		return networkResourceCustomization;
	}

	public void setNetworkResourceCustomization(
			Set<CollectionNetworkResourceCustomization> networkResourceCustomization) {
		this.networkResourceCustomization = networkResourceCustomization;
	}
}
