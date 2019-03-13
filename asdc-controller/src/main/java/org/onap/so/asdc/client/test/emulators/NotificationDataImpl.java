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
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Component
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class NotificationDataImpl implements INotificationData {

	@JsonProperty("distributionID")
	private String distributionID;
	@JsonProperty("serviceName")
	private String serviceName;
	@JsonProperty("serviceVersion")
	private String serviceVersion;
	@JsonProperty("serviceUUID")
	private String serviceUUID;
	@JsonProperty("serviceDescription")
	private String serviceDescription;
	@JsonProperty("serviceInvariantUUID")
	private String serviceInvariantUUID;
	@JsonProperty("resources")
	private List<ResourceInfoImpl> resources;
	@JsonProperty("serviceArtifacts")
	private List<ArtifactInfoImpl> serviceArtifacts;
	@JsonProperty("workloadContext")
	private String workloadContext;

	@Override
	public String getDistributionID() {
		return distributionID;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public String getServiceVersion() {
		return serviceVersion;
	}

	@Override
	public String getServiceUUID() {
		return serviceUUID;
	}

	public void setDistributionID(String distributionID) {
		this.distributionID = distributionID;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public void setServiceUUID(String serviceUUID) {
		this.serviceUUID = serviceUUID;
	}

	

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	@Override
	public String getWorkloadContext() { 
		return workloadContext; 
	}

	@Override
	public void setWorkloadContext(String workloadContext) {
		this.workloadContext = workloadContext;	
	}

	@Override
	public String toString() {
		return "NotificationDataImpl [distributionID=" + distributionID + ", serviceName=" + serviceName
				+ ", serviceVersion=" + serviceVersion + ", serviceUUID=" + serviceUUID + ", serviceDescription="
				+ serviceDescription + ", serviceInvariantUUID=" + serviceInvariantUUID + ", resources=" + resources
				+ ", serviceArtifacts=" + serviceArtifacts + ", workloadContext=" + workloadContext + "]";
	}

	@Override
	public List<IResourceInstance> getResources() {
		List<IResourceInstance> ret = new ArrayList<IResourceInstance>();
		if( resources != null ){
			ret.addAll(resources);
		}
		return ret;
	}
	
	public List<ResourceInfoImpl> getResourcesImpl(){
		return resources;
	}
	
	List<ArtifactInfoImpl> getServiceArtifactsImpl(){
		return serviceArtifacts;
	}

	@Override
	public List<IArtifactInfo> getServiceArtifacts() {
		List<IArtifactInfo> temp = new ArrayList<IArtifactInfo>();
		if( serviceArtifacts != null ){
			temp.addAll(serviceArtifacts);
		}
		return temp;
	}
	
	public void setServiceArtifacts(List<ArtifactInfoImpl> relevantServiceArtifacts) {
		serviceArtifacts = relevantServiceArtifacts;
		
	}
	
	@Override
	public String getServiceInvariantUUID() {
		return serviceInvariantUUID;
	}
	
	
	public void setServiceInvariantUUID(String serviceInvariantUUID) {
		this.serviceInvariantUUID = serviceInvariantUUID;
	}
	@Override
	public IArtifactInfo getArtifactMetadataByUUID(String artifactUUID){
		IArtifactInfo ret = findArtifactInfoByUUID(artifactUUID, serviceArtifacts);
		if( ret == null && resources != null ){
			for( ResourceInfoImpl currResourceInstance : resources ){
				ret = findArtifactInfoByUUID(artifactUUID, currResourceInstance.getArtifactsImpl());
				if( ret != null ){
					break;
				}
			}
		}
		return ret;

	}

	private IArtifactInfo findArtifactInfoByUUID(String artifactUUID, List<ArtifactInfoImpl> listToCheck) {
		IArtifactInfo ret = null;
		if( listToCheck != null ){
			for(IArtifactInfo curr: listToCheck ){
				if(curr.getArtifactUUID().equals(artifactUUID) ){
					ret = curr;
					break;
				}
			}
		}
		return ret;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof NotificationDataImpl)) {
			return false;
		}
		NotificationDataImpl castOther = (NotificationDataImpl) other;
		return new EqualsBuilder().append(serviceUUID, castOther.serviceUUID)
				.append(serviceVersion, castOther.serviceVersion).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(distributionID).append(serviceName).append(serviceVersion)
				.append(serviceUUID).append(serviceDescription).append(serviceInvariantUUID).append(resources)
				.append(serviceArtifacts).append(workloadContext).toHashCode();
	}

}
