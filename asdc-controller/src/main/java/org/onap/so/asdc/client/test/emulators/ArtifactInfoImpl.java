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

import org.onap.sdc.api.notification.IArtifactInfo;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class ArtifactInfoImpl implements IArtifactInfo {

	private String artifactName;
	private String artifactType;
	private String artifactURL;
	private String artifactChecksum;
	private String artifactDescription;
	private Integer artifactTimeout;
	private String artifactVersion;
	private String artifactUUID;
	private String generatedFromUUID;
	private ArtifactInfoImpl generatedArtifact;
	private List<IArtifactInfo> relatedArtifactsInfo;
	private List<ArtifactInfoImpl> relatedArtifactsImpl;
	ArtifactInfoImpl(){}
	
	private ArtifactInfoImpl(IArtifactInfo iArtifactInfo){
		artifactName = iArtifactInfo.getArtifactName();
		artifactType = iArtifactInfo.getArtifactType(); 
		artifactURL = iArtifactInfo.getArtifactURL();
		artifactChecksum = iArtifactInfo.getArtifactChecksum();
		artifactDescription = iArtifactInfo.getArtifactDescription();
		artifactTimeout = iArtifactInfo.getArtifactTimeout();
		artifactVersion = iArtifactInfo.getArtifactVersion();
		artifactUUID = iArtifactInfo.getArtifactUUID();
		generatedArtifact = (ArtifactInfoImpl) iArtifactInfo.getGeneratedArtifact();
		relatedArtifactsInfo = iArtifactInfo.getRelatedArtifacts();
	}
	
	public static List<ArtifactInfoImpl> convertToArtifactInfoImpl(List<IArtifactInfo> list){
		List<ArtifactInfoImpl> ret = new ArrayList<ArtifactInfoImpl>();
		if( list != null ){
			for(IArtifactInfo artifactInfo : list  ){
				ret.add(new ArtifactInfoImpl(artifactInfo));
			}
		}
		return ret;
	}
	
	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	public String getArtifactURL() {
		return artifactURL;
	}

	public void setArtifactURL(String artifactURL) {
		this.artifactURL = artifactURL;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	public String getArtifactDescription() {
		return artifactDescription;
	}

	public void setArtifactDescription(String artifactDescription) {
		this.artifactDescription = artifactDescription;
	}

	public Integer getArtifactTimeout() {
		return artifactTimeout;
	}

	public void setArtifactTimeout(Integer artifactTimeout) {
		this.artifactTimeout = artifactTimeout;
	}

	@Override
	public String toString() {
		return "BaseArtifactInfoImpl [artifactName=" + artifactName
				+ ", artifactType=" + artifactType + ", artifactURL="
				+ artifactURL + ", artifactChecksum=" + artifactChecksum
				+ ", artifactDescription=" + artifactDescription
				+ ", artifactVersion=" + artifactVersion
				+ ", artifactUUID=" + artifactUUID
				+ ", artifactTimeout=" + artifactTimeout + "]";
	}

	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	public String getArtifactUUID() {
		return artifactUUID;
	}

	public void setArtifactUUID(String artifactUUID) {
		this.artifactUUID = artifactUUID;
	}

	public String getGeneratedFromUUID() {
		return generatedFromUUID;
	}

	public void setGeneratedFromUUID(String generatedFromUUID) {
		this.generatedFromUUID = generatedFromUUID;
	}
	
	@Override
	public ArtifactInfoImpl getGeneratedArtifact() {
		return generatedArtifact;
	}

	public void setGeneratedArtifact(ArtifactInfoImpl generatedArtifact) {
		this.generatedArtifact = generatedArtifact;
	}
	
	@Override
	public List<IArtifactInfo> getRelatedArtifacts(){
		List<IArtifactInfo> temp = new ArrayList<IArtifactInfo>();
		if( relatedArtifactsInfo != null ){
			temp.addAll(relatedArtifactsImpl);
		}
		return temp;
	}
	
	public void setRelatedArtifacts(List<ArtifactInfoImpl> relatedArtifacts) {
		this.relatedArtifactsImpl = relatedArtifacts;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ArtifactInfoImpl)) {
			return false;
		}
		ArtifactInfoImpl castOther = (ArtifactInfoImpl) other;
		return new EqualsBuilder().append(artifactUUID, castOther.artifactUUID)
				.append(artifactVersion, castOther.artifactVersion).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(artifactName).append(artifactType).append(artifactURL)
				.append(artifactChecksum).append(artifactDescription).append(artifactTimeout).append(artifactVersion)
				.append(artifactUUID).append(generatedFromUUID).append(generatedArtifact).append(relatedArtifactsInfo)
				.append(relatedArtifactsImpl).toHashCode();
	}

}
