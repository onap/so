package org.openecomp.mso.bpmn.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Encapsulates VNF resource data set
 *
 */
@JsonRootName("vnfResource")
public class VnfResource extends Resource {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public VnfResource(){
		resourceType = ResourceType.VNF;
		setResourceId(UUID.randomUUID().toString());
	}
	
	/*
	 * fields specific to VNF resource type
	 */
	@JsonProperty("vfModules")
	private List <ModuleResource>  vfModules;
	private String vnfType;
	private String nfFunction;
	private String nfType;
	private String nfRole;
	private String nfNamingCode;

	/*
	 * GET and SET
	 */
	public List<ModuleResource> getVfModules() {
		return vfModules;
	}
	public void setModules(List<ModuleResource> moduleResources) {
		this.vfModules = moduleResources;
	}
	@Deprecated
	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}
	public String getVnfType() {
		return vnfType;
	}
	public String getNfFunction() {
		return nfFunction;
	}
	public void setNfFunction(String nfFunction) {
		this.nfFunction = nfFunction;
	}
	public String getNfType() {
		return nfType;
	}
	public void setNfType(String nfType) {
		this.nfType = nfType;
	}
	public String getNfRole() {
		return nfRole;
	}
	public void setNfRole(String nfRole) {
		this.nfRole = nfRole;
	}
	public String getNfNamingCode() {
		return nfNamingCode;
	}
	public void setNfNamingCode(String nfNamingCode) {
		this.nfNamingCode = nfNamingCode;
	}
	/*
	 * GET accessors per design requirements
	 */
	
	/**
	 * Returns a list of all VfModule objects.
	 * Base module is first entry in the list
	 * @return ordered list of ModuleResources objects
	 */
	@JsonIgnore
	public List<ModuleResource> getAllVfModuleObjects(){
		if (vfModules == null) {
			return null;
		}
		
		for (int i = 0; i < vfModules.size(); i++) {
			ModuleResource moduleResource = vfModules.get(i);
			if (moduleResource.getIsBase()){
				vfModules.remove(moduleResource);
				vfModules.add(0,moduleResource);
			}
		}
		return vfModules;
	}
	
	/**
	 * 
	 * @return Returns JSON list of all VfModule structures.
	 */
	@JsonIgnore
	public String getAllVfModulesJson(){
		
		return listToJson(vfModules);
	}
	
	// methods to add to the list
	public void addVfModule(ModuleResource moduleResource) {
		if (vfModules == null){
			vfModules = new ArrayList<ModuleResource>();
		}
		this.vfModules.add(moduleResource);
	}
	

	/**
	 * Utility method to allow construction of the filed in the form of 
	 * <serviceResources.modelInfo.modelName>/<serviceVnfs.modelInfo.modelInstanceName>
	 * 
	 * default setter for this field deprecated
	 * @param modelName << serviceResources.modelInfo.modelName
	 * @param modelInstanceName
	 */
	public void constructVnfType(String modelName) {
		this.vnfType = modelName.concat("/").concat(this.modelInfo.getModelInstanceName());
	}
}