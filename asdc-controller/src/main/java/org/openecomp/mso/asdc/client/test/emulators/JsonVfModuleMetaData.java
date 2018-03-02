package org.openecomp.mso.asdc.client.test.emulators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.mso.asdc.installer.IVfModuleData;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonVfModuleMetaData implements IVfModuleData {

	@JsonProperty("artifacts")
	private List<String> artifacts;
	@JsonProperty("properties")
	//private List<Map<String, Object>> properties = new ArrayList<>();
	private Map<String,String> properties = new HashMap<>();
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	@JsonIgnore
	private Map<String,Object> attributesMap = new HashMap<>();
	
	@Override
	public List<String> getArtifacts() {
		return artifacts;
	}

	@Override
	public String getVfModuleModelDescription() {
		return (String)attributesMap.get("vfModuleModelDescription");
	}

	@Override
	public String getVfModuleModelInvariantUUID() {
		return (String)attributesMap.get("vfModuleModelInvariantUUID");
	}
	
	@Override
	public String getVfModuleModelCustomizationUUID() {
		return (String)attributesMap.get("vfModuleModelCustomizationUUID");
	}

	@Override
	public String getVfModuleModelName() {
		return (String)attributesMap.get("vfModuleModelName");
	}

	@Override
	public String getVfModuleModelUUID() {
		return (String)attributesMap.get("vfModuleModelUUID");
	}

	@Override
	public String getVfModuleModelVersion() {
		return (String)attributesMap.get("vfModuleModelVersion");
	}

	@Override
	public boolean isBase() {
		return (boolean)attributesMap.get("isBase");
	}
	
	@SuppressWarnings("unused")
	@JsonAnySetter
	public final void setAttribute(String attrName, Object attrValue) {
		if ((null != attrName) && (!attrName.isEmpty()) && (null != attrValue) && (null != attrValue.toString())) {
			this.attributesMap.put(attrName,attrValue);
		}
	}

}
