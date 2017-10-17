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

package org.openecomp.mso.bpmn.core.domain;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.json.JSONObject;
import org.openecomp.mso.bpmn.core.json.DecomposeJsonUtil;



/**
 * Service Decomposition Structure
 * This Java object contains service information:
 * - Service model info
 * - Service type and role
 * - list of VNF resource's decompositon
 * - list of network resource's decompositon
 * - list of allotted resource's decompositon
 */
@JsonRootName(value = "serviceResources")
//@JsonTypeInfo(include=As.WRAPPER_OBJECT, use=Id.NAME)
public class ServiceDecomposition extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	DecomposeJsonUtil jsonUtils = new DecomposeJsonUtil();

	@JsonProperty("modelInfo")
	private ModelInfo modelInfo;
	@JsonProperty("serviceType")
	private String serviceType;
	@JsonProperty("serviceRole")
	private String serviceRole;
	private ServiceInstance serviceInstance;
	@JsonProperty("vnfResource")
	private List <VnfResource>  vnfResources;
	@JsonProperty("networkResource")
	private List <NetworkResource>  networkResources;
	@JsonProperty("allottedResource")
	private List <AllottedResource>  allottedResources;

	public ServiceDecomposition () {
		super();
	}

	public ServiceDecomposition (String catalogRestOutput) {

		ServiceDecomposition serviceDecomposition = DecomposeJsonUtil.JsonToServiceDecomposition(catalogRestOutput);
		this.modelInfo = serviceDecomposition.getModelInfo();
		this.vnfResources = serviceDecomposition.getServiceVnfs();
		this.allottedResources = serviceDecomposition.getServiceAllottedResources();
		this.networkResources = serviceDecomposition.getServiceNetworks();
		this.serviceRole = serviceDecomposition.getServiceRole();
		this.serviceType = serviceDecomposition.getServiceType();
	}

	/**
	 * Constructor taking Catalog DB Adapter REST output (serviceResources model) + service Instance ID
	 * @param catalogRestOutput
	 * @param serviceInstanceId
	 */
	public ServiceDecomposition (String catalogRestOutput, String serviceInstanceId) {

		ServiceDecomposition serviceDecomposition = DecomposeJsonUtil.JsonToServiceDecomposition(catalogRestOutput);
		this.modelInfo = serviceDecomposition.getModelInfo();
		this.vnfResources = serviceDecomposition.getServiceVnfs();
		this.allottedResources = serviceDecomposition.getServiceAllottedResources();
		this.networkResources = serviceDecomposition.getServiceNetworks();

		this.serviceRole = serviceDecomposition.getServiceRole();
		this.serviceType = serviceDecomposition.getServiceType();
		
		this.serviceInstance = new ServiceInstance();
		this.serviceInstance.setInstanceId(serviceInstanceId);
	}

	/**
	 * Constructor taking a Service Decomposition JSON serialization
	 * @param catalogRestOutput
	 * @param serviceInstanceId
	 */
	public ServiceDecomposition (JSONObject jsonServiceDecomposition, String serviceInstanceId) {
		//TODO provide constructor implementation

	}

	//*****
	//GET and SET section
	/**
	 * Return just the service model portion of the Service Decomposition as a Java object.
	 * The service model object should support retrieval as JSON string that is formatted correctly for sending serviceModelInfo to Building Blocks.
	 * @return
	 */
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}
	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}
	public void setServiceInstance(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}
	public List<VnfResource> getServiceVnfs() {
		return vnfResources;
	}
	public void setServiceVnfs(List<VnfResource> vnfResources) {
		this.vnfResources = vnfResources;
	}
	public List<NetworkResource> getServiceNetworks() {
		return networkResources;
	}
	public void setServiceNetworks(List<NetworkResource> networkResources) {
		this.networkResources = networkResources;
	}
	public List<AllottedResource> getServiceAllottedResources() {
		return allottedResources;
	}
	public void setServiceAllottedResources(List<AllottedResource> allottedResources) {
		this.allottedResources = allottedResources;
	}
	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceRole() {
		return serviceRole;
	}

	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}
	//*****

	//*****
	//Access methods


	/**
	 * This method returns one combined list of Resources of All Types
	 * @return
	 */
	@JsonIgnore
	public List<Resource> getServiceResources(){
		ArrayList serviceResources = new ArrayList();
		if(this.getServiceAllottedResources() != null){
			serviceResources.addAll(this.getServiceAllottedResources());
		}
		if(this.getServiceNetworks() != null){
			serviceResources.addAll(this.getServiceNetworks());
		}
		if(this.getServiceVnfs() != null){
			serviceResources.addAll(this.getServiceVnfs());
		}
		return serviceResources;
	}

	/**
	 * This method returns String representation of one combined list of Resources of All Types
	 * @return
	 */
	@JsonIgnore
	public String getServiceResourcesJsonString(){
		StringBuilder serviceResourcesJsonStringBuffer = new StringBuilder();
		serviceResourcesJsonStringBuffer.append(listToJson((this.getServiceNetworks())));
		serviceResourcesJsonStringBuffer.append(listToJson((this.getServiceVnfs())));
		serviceResourcesJsonStringBuffer.append(listToJson((this.getServiceAllottedResources())));
		return serviceResourcesJsonStringBuffer.toString();
	}

	/**
	 * Returns a JSON list of all Network Resource structures (i.e. the serialized NetworkDecomposition objects).
	 * @return
	 */
	@JsonIgnore
	public String getServiceNetworksJson(){
		return listToJson(this.getServiceNetworks());
	}
	/**
	 * Returns a JSON list of all VnfResource structures (i.e. the serialized VnfResource objects).
	 * @return
	 */
	@JsonIgnore
	public String getServiceVnfsJson(){
		return listToJson(this.getServiceVnfs());
	}
	/**
	 * Returns a JSON list of all Allotted Resource structures (i.e. the serialized AllottedResource objects).
	 * @return
	 */
	@JsonIgnore
	public String getServiceAllottedResourcesJson(){
		return listToJson(this.getServiceAllottedResources());
	}

	//TODO - define Resource Object ID
	@JsonIgnore
	public String getVnfResource(String resourceObjectId) {

		Iterator<Resource> iter = getServiceResources().iterator();
		while (iter.hasNext()) {
			Resource resource = iter.next();

			if ("extracted information".equals(resourceObjectId)){
				return resource.toJsonString();
			}
		}
		return "";
	}

	//Methods to add Resource to the list
	/**
	 * Add VNF resource to the list
	 * @param vnfResource
	 */
	public void addVnfResource(Resource vnfResource) {
		if (vnfResources == null){
			vnfResources = new ArrayList<VnfResource>();
		}
		this.vnfResources.add((VnfResource)vnfResource);
	}
	/**
	 * Add Network resource to the list
	 * @param networkResource
	 */
	public void addNetworkResource(Resource networkResource) {
		if (networkResources == null){
			networkResources = new ArrayList<>();
		}
		this.networkResources.add((NetworkResource)networkResource);
	}
	/**
	 * Add Allotted resource to the list
	 * @param allottedResource
	 */
	public void addAllottedResource(Resource allottedResource) {
		if (allottedResources == null){
			allottedResources = new ArrayList<>();
		}
		this.allottedResources.add((AllottedResource)allottedResource);
	}

	/**
	 * Add resource to the list
	 * Given a ResourceDecomposition (subclass) object, add it to the Service Decomposition (in the appropriate category, e.g. as a VNF, Network, or Allotted Resource).
	 * As dependencies are not currently supported, add it to the end of any ordered lists.
	 * @param Resource
	 */
	public void addResource(Resource resource) {
		//create resource based upon type
		switch (resource.resourceType) {
		case VNF:
			this.addVnfResource(resource);
			break;
		case NETWORK:
			this.addNetworkResource(resource);
		     break;
		case ALLOTTED_RESOURCE:
			this.addAllottedResource(resource);
		    break;
		default:
		     throw new IllegalArgumentException("Invalid resource type: " + resource.resourceType);
		 }
	}

	/**
	 * Add resource to the list
	 * @param Resource
	 */
	public void addVnfResource(String jsonResource) {
		VnfResource vnfResource;
		vnfResource = DecomposeJsonUtil.JsonToVnfResource(jsonResource);
		this.addVnfResource(vnfResource);
	}
	/**
	 * Add resource to the list
	 * @param Resource
	 */
	public void addNetworkResource(String jsonResource) {
		NetworkResource networkResource;
		networkResource = DecomposeJsonUtil.JsonToNetworkResource(jsonResource);
		this.addVnfResource(networkResource);
	}
	/**
	 * Add resource to the list
	 * @param Resource
	 */
	public void addAllottedResource(String jsonResource) {
		AllottedResource allottedResource;
		allottedResource = DecomposeJsonUtil.JsonToAllottedResource(jsonResource);
		this.addVnfResource(allottedResource);
	}

	/**
	 * Given a ResourceDecomposition (subclass) object, locate it in the Service Decomposition by its unique ID, and replace the current version with the new one.
	 * This method should support concurrency control via an auto-incrementing field in the ResourceDecomposition class.
	 * @param Resource
	 * @return TRUE if replacement was a success
	 */
	public boolean replaceResource(Resource newResource){
		boolean result = false;
		List serviceResources = getServiceResources();
		Iterator<Resource> iter = serviceResources.iterator();
		while (iter.hasNext()) {
			Resource resource = iter.next();
			System.out.println("resource found");
			if (resource.resourceType == newResource.resourceType){
				System.out.println("resource type matches");
				if (resource.getResourceId().equalsIgnoreCase(newResource.getResourceId())){
					System.out.println("resource id matches");
					//returns TRUE if replacement is a success
					result = Collections.replaceAll(serviceResources, resource, newResource);
				}
			}
		}
		//set updated list into ServiceDecomposition
		this.setResourceList(serviceResources);
		return result;
	}

	/**
	 * Given a ResourceDecomposition as a JSON string, locate it in the Service Decomposition by its unique ID,
	 *  and replace the current version with the new one.
	 * @param jsonString
	 * @return
	 */
	public boolean replaceResource(String jsonString){
		//TODO: define unique ID for the Resource!
		return false;
	}

	/**
	 *  Given a resource object ID, locate it in the Service Decomposition by its unique ID, and delete it.
	 * @param Resource
	 * @return TRUE if delete was a success
	 */
	public boolean deleteResource(Resource resource){
		List serviceResources = getServiceResources();
		Iterator<Resource> iter = serviceResources.iterator();
		while (iter.hasNext()) {
			Resource item = iter.next();

			if ((item.resourceType == resource.resourceType) &&
			        (item.getResourceId().equalsIgnoreCase(resource.getResourceId()))){
				return serviceResources.remove(resource);
			}
		}

		return false;
	}

	/**
	 * Generic method to set List of ResourceDecomposition objects
	 * @param resources
	 * @return
	 */
	public boolean setResourceList(List<Resource> resources){
		//create resource based upon type
		switch (resources.get(0).resourceType) {
		case VNF:
			this.setServiceVnfs((List<VnfResource>)(List<?>)resources);
			break;
		case NETWORK:
			this.setServiceNetworks((List<NetworkResource>)(List<?>)resources);
		    break;
		case ALLOTTED_RESOURCE:
			this.setServiceAllottedResources((List<AllottedResource>)(List<?>)resources);
		    break;
		default:
		     throw new IllegalArgumentException("Invalid resource type: " + resources.get(0).resourceType);
		 }

		return false;
	}

	/**
	 *
	 * This method locates and returns a resource in a given
	 * Service Decomposition object by its unique resource id.
	 * Returns null if resource doesn't exist.
	 *
	 * @param resourceId - id of the resource
	 * @return resource
	 */
	@JsonIgnore
	public Resource getServiceResource(String resourceId){
		List<Resource> resources = getServiceResources();
		Iterator<Resource> iter = resources.iterator();
		while (iter.hasNext()){
			Resource resource = iter.next();
			if (resource.getResourceId().equalsIgnoreCase(resourceId)){
				//match
				return resource;
			}
		}
		return null;
	}
}
