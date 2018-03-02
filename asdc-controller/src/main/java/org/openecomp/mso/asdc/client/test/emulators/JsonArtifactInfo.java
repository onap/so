package org.openecomp.mso.asdc.client.test.emulators;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.api.notification.IArtifactInfo;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JsonArtifactInfo implements IArtifactInfo {

	@JsonIgnore
	private Map<String,IArtifactInfo> artifactsMapByUUID = new HashMap<>();
	
	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();
	
	public JsonArtifactInfo() {
		
	}
	
	public synchronized void addArtifactToUUIDMap (List<JsonArtifactInfo> artifactList) {
		for (JsonArtifactInfo artifact:artifactList) {
			artifactsMapByUUID.put(artifact.getArtifactUUID(), artifact);	
		}
		
	}
	
	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}
	
	
	
	public Map<String, IArtifactInfo> getArtifactsMapByUUID() {
		return artifactsMapByUUID;
	}

	@Override
	public String getArtifactChecksum() {
		return (String)attributesMap.get("artifactCheckSum");
	}

	@Override
	public String getArtifactDescription() {
		return (String)attributesMap.get("artifactDescription");
	}

	@Override
	public String getArtifactName() {
		return (String)attributesMap.get("artifactName");
	}

	@Override
	public Integer getArtifactTimeout() {
		return (Integer)attributesMap.get("artifactTimeout");
	}

	@Override
	public String getArtifactType() {
		return (String)attributesMap.get("artifactType");
	}

	@Override
	public String getArtifactURL() {
		return (String)attributesMap.get("artifactURL");
	}

	@Override
	public String getArtifactUUID() {
		return (String)attributesMap.get("artifactUUID");
	}

	@Override
	public String getArtifactVersion() {
		return (String)attributesMap.get("artifactVersion");
	}

	@Override
	public IArtifactInfo getGeneratedArtifact () {
		return artifactsMapByUUID.get(attributesMap.get("generatedArtifact"));
	}

	@Override
	public List<IArtifactInfo> getRelatedArtifacts() {
		List<IArtifactInfo> listArtifacts = new LinkedList<>();
		List<String> uuidList = (List<String>)attributesMap.get("relatedArtifact");
		if (uuidList != null) {
			for(String uuid:uuidList) {
				listArtifacts.add(artifactsMapByUUID.get(uuid));
			}
		}
		return listArtifacts;
	}

}
