package org.openecomp.mso.asdc.client.test.emulators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.IResourceInstance;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class JsonResourceInfo implements IResourceInstance {

	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();
	
	@JsonProperty("artifacts")
	@JsonDeserialize(using=JsonArtifactInfoDeserializer.class)
	private List<IArtifactInfo> artifacts;
	
	public JsonResourceInfo() {
	
	}
	
	@Override
	public List<IArtifactInfo> getArtifacts() {
		return artifacts;
	}

	@Override
	public String getResourceInstanceName() {
		return (String)attributesMap.get("resourceInstanceName");
	}

	@Override
	public String getResourceInvariantUUID() {
		return (String)attributesMap.get("resourceInvariantUUID");
	}
	
	@Override
	public String getResourceCustomizationUUID() {
		return (String)attributesMap.get("resourceCustomizationUUID");
	}

	@Override
	public String getResourceName() {
		return (String)attributesMap.get("resourceName");
	}

	@Override
	public String getResourceType() {
		return (String)attributesMap.get("resourceType");
	}

	@Override
	public String getResourceUUID() {
		return (String)attributesMap.get("resourceUUID");
	}

	@Override
	public String getResourceVersion() {
		return (String)attributesMap.get("resourceVersion");
	}
	
	@Override
	public String getSubcategory() {
		return (String)attributesMap.get("subCategory");
	}
	
	@Override
	public String getCategory() {
		return (String)attributesMap.get("category");
	}
	
	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}
}
