package org.openecomp.mso.adapters.vdu;

import java.util.ArrayList;
import java.util.List;

public class VduModelInfo {	
	private String modelCustomizationUUID;
	private int timeoutMinutes;
	private List<VduArtifact> artifacts = new ArrayList<VduArtifact>();
	
	public String getModelCustomizationUUID() {
		return modelCustomizationUUID;
	}
	public void setModelCustomizationUUID(String modelCustomizationUUID) {
		this.modelCustomizationUUID = modelCustomizationUUID;
	}
	public int getTimeoutMinutes() {
		return timeoutMinutes;
	}
	public void setTimeoutMinutes(int timeoutMinutes) {
		this.timeoutMinutes = timeoutMinutes;
	}
	public List<VduArtifact> getArtifacts() {
		return artifacts;
	}
	public void setArtifacts(List<VduArtifact> artifacts) {
		this.artifacts = artifacts;
	}
	
}