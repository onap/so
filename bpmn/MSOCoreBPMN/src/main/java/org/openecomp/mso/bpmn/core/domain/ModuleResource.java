package org.openecomp.mso.bpmn.core.domain;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("vfModule")
public class ModuleResource  extends Resource {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public ModuleResource(){
		resourceType = ResourceType.MODULE;
	}
	
	/*
	 * fields specific to VF Module resource type
	 */
	private String vfModuleType;
	private boolean hasVolumeGroup;
	private boolean isBase;
	private String vfModuleLabel;
	private int initialCount;
	
	/*
	 * GET && SET
	 */
	public boolean getIsBase() {
		return isBase;
	}
	public void setIsBase(boolean isBase) {
		this.isBase = isBase;
	}
	public String getVfModuleLabel() {
		return vfModuleLabel;
	}
	public void setVfModuleLabel(String vfModuleLabel) {
		this.vfModuleLabel = vfModuleLabel;
	}
	public int getInitialCount() {
		return initialCount;
	}
	public void setInitialCount(int initialCount) {
		this.initialCount = initialCount;
	}
	public String getVfModuleType() {
		return vfModuleType;
	}
	public void setVfModuleType(String vfModuleType) {
		this.vfModuleType = vfModuleType;
	}
	public boolean isHasVolumeGroup() {
		return hasVolumeGroup;
	}
	public void setHasVolumeGroup(boolean hasVolumeGroup) {
		this.hasVolumeGroup = hasVolumeGroup;
	}
	
}