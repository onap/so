package org.openecomp.mso.bpmn.core.decomposition;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates VNF resource data set
 * @author
 *
 */
@JsonRootName("vnfResource")
public class VnfResource extends ResourceDecomposition {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public VnfResource(){
		resourceType = "vnfResource";
	}
	
	/*
	 * fields specific to VNF resource type
	 */
	@JsonProperty("vfModules")
	private List <ModuleResource>  vfModules;
	private String vnfType;

	/*
	 * GET and SET
	 */
	public List<ModuleResource> getVfModules() {
		return vfModules;
	}
	public void setModules(List<ModuleResource> moduleResources) {
		this.vfModules = moduleResources;
	}
	public String getVnfType() {
		return vnfType;
	}
	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
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
	
}
