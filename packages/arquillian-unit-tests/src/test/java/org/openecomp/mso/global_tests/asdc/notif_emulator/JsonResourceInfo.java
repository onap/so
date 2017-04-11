package org.openecomp.mso.global_tests.asdc.notif_emulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.IResourceInstance;

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
	public String getResourceCustomizationUUID() {
		return (String)attributesMap.get("resourceCustomizationUUID");
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
