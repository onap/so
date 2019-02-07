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

package org.onap.so.asdc.client.test.emulators;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.IResourceInstance;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ResourceInfoImpl implements IResourceInstance{
	ResourceInfoImpl (){}
	private String resourceInstanceName;
	private String resourceCustomizationUUID;
	private String resourceName;
	private String resourceVersion;
	private String resourceType;
	private String resourceUUID;
	private String resourceInvariantUUID;
	private String category;
	private String subcategory;
	private List<ArtifactInfoImpl> artifacts;
	
	private ResourceInfoImpl(IResourceInstance resourceInstance){
		resourceInstanceName = resourceInstance.getResourceInstanceName();
		resourceCustomizationUUID = resourceInstance.getResourceCustomizationUUID();
		resourceName = resourceInstance.getResourceName();
		resourceVersion = resourceInstance.getResourceVersion();
		resourceType = resourceInstance.getResourceType();
		resourceUUID = resourceInstance.getResourceUUID();
		resourceInvariantUUID = resourceInstance.getResourceInvariantUUID();
		category = resourceInstance.getCategory();
		subcategory = resourceInstance.getSubcategory();
		artifacts = ArtifactInfoImpl.convertToArtifactInfoImpl(resourceInstance.getArtifacts());
	}
	
	public static List<ResourceInfoImpl> convertToJsonContainer(List<IResourceInstance> resources){
		 List<ResourceInfoImpl> buildResources = new ArrayList<ResourceInfoImpl>();
		 if( resources != null ){
			 for( IResourceInstance resourceInstance : resources ){
				 buildResources.add(new ResourceInfoImpl(resourceInstance));
			 }
		 }
		 return buildResources;
	}
	
	@Override
	public String getResourceInstanceName() {
		return resourceInstanceName;
	}

	public void setResourceInstanceName(String resourceInstanceName) {
		this.resourceInstanceName = resourceInstanceName;
	}
	
	@Override
	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	@Override
	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	public void setResoucreType(String resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public String getResourceUUID() {
		return resourceUUID;
	}

	public void setResourceUUID(String resourceUUID) {
		this.resourceUUID = resourceUUID;
	}

	@Override
	public List<IArtifactInfo> getArtifacts() {
		List<IArtifactInfo> temp = new ArrayList<IArtifactInfo>();
		if( artifacts != null ){
			temp.addAll(artifacts);
		}
		return temp;
	}

	public void setArtifacts(List<ArtifactInfoImpl> artifacts) {
		this.artifacts = artifacts;
	}
	
	@JsonIgnore
	public List<ArtifactInfoImpl> getArtifactsImpl() {
		return artifacts;
	}
	
	@Override
	public String getResourceInvariantUUID() {
		return resourceInvariantUUID;
	}
	
	public void setResourceInvariantUUID(String resourceInvariantUUID) {
		this.resourceInvariantUUID = resourceInvariantUUID;
	}
	public String getResourceCustomizationUUID() {
		return resourceCustomizationUUID;
	}

	public void setResourceCustomizationUUID(String resourceCustomizationUUID) {
		this.resourceCustomizationUUID = resourceCustomizationUUID;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ResourceInfoImpl)) {
			return false;
		}
		ResourceInfoImpl castOther = (ResourceInfoImpl) other;
		return new EqualsBuilder().append(resourceUUID, castOther.resourceUUID)
				.append(resourceVersion, castOther.resourceVersion).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(resourceInstanceName).append(resourceCustomizationUUID).append(resourceName)
				.append(resourceVersion).append(resourceType).append(resourceUUID).append(resourceInvariantUUID)
				.append(category).append(subcategory).append(artifacts).toHashCode();
	}
}
