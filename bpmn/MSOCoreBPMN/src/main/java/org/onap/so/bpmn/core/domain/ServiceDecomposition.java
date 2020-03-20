/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.core.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.json.JSONObject;
import org.onap.so.bpmn.core.json.DecomposeJsonUtil;
import org.onap.so.bpmn.core.json.JsonDecomposingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * Service Decomposition Structure This Java object contains service information: - Service model info - Service type
 * and role - list of VNF resource's decompositon - list of network resource's decompositon - list of allotted
 * resource's decompositon
 */
@JsonRootName(value = "serviceResources")
public class ServiceDecomposition extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ServiceDecomposition.class);

	@JsonProperty("modelInfo")
	private ModelInfo modelInfo;
	@JsonProperty("serviceType")
	private String serviceType;
	@JsonProperty("serviceRole")
	private String serviceRole;
	private ServiceInstance serviceInstance;
	private Request request;
	private String callbackURN;
	private String sdncVersion;
	@JsonProperty("project")
	private Project project;
	@JsonProperty("owningEntity")
	private OwningEntity owningEntity;
	@JsonProperty("serviceVnfs")
	private List<VnfResource> vnfResources;
	@JsonProperty("serviceNetworks")
	private List<NetworkResource> networkResources;
	@JsonProperty("serviceAllottedResources")
	private List<AllottedResource> allottedResources;
	@JsonProperty("configResource")
	private List<ConfigResource> configResources;

	// Isaac added for service decomposition - Begin
	@JsonProperty("serviceInfo")
	private ServiceInfo serviceInfo;

	@JsonProperty("serviceInfo")
	public ServiceInfo getServiceInfo() {
		return serviceInfo;
	}

	@JsonProperty("serviceInfo")
	public void setServiceInfo(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	@JsonProperty("serviceProxy")
	private List<ServiceProxy> serviceProxy;

	@JsonProperty("serviceProxy")
	public List<ServiceProxy> getServiceProxy() {
		return serviceProxy;
	}

	@JsonProperty("serviceProxy")
	public void setServiceProxy(List<ServiceProxy> serviceProxy) {
		this.serviceProxy = serviceProxy;
	}


	// Isaac added for service decomposition - Begin


	public ServiceDecomposition() {
		super();
	}

	public ServiceDecomposition(String catalogRestOutput) throws JsonDecomposingException {
		ServiceDecomposition serviceDecomposition = DecomposeJsonUtil.jsonToServiceDecomposition(catalogRestOutput);
		this.modelInfo = serviceDecomposition.getModelInfo();
		this.vnfResources = serviceDecomposition.getVnfResources();
		this.allottedResources = serviceDecomposition.getAllottedResources();
		this.networkResources = serviceDecomposition.getNetworkResources();
		this.serviceRole = serviceDecomposition.getServiceRole();
		this.serviceType = serviceDecomposition.getServiceType();
		this.configResources = serviceDecomposition.getConfigResources();
		this.serviceProxy = serviceDecomposition.getServiceProxy();
		this.serviceInfo = serviceDecomposition.getServiceInfo();
	}

	/**
	 * Constructor taking Catalog DB Adapter REST output (serviceResources model) + service Instance ID
	 *
	 * @param catalogRestOutput
	 * @param serviceInstanceId
	 */
	public ServiceDecomposition(String catalogRestOutput, String serviceInstanceId) throws JsonDecomposingException {
		ServiceDecomposition serviceDecomposition = DecomposeJsonUtil.jsonToServiceDecomposition(catalogRestOutput);
		this.modelInfo = serviceDecomposition.getModelInfo();
		this.vnfResources = serviceDecomposition.getVnfResources();
		this.allottedResources = serviceDecomposition.getAllottedResources();
		this.configResources = serviceDecomposition.getConfigResources();
		this.networkResources = serviceDecomposition.getNetworkResources();

		this.serviceRole = serviceDecomposition.getServiceRole();
		this.serviceType = serviceDecomposition.getServiceType();

		this.serviceInstance = new ServiceInstance();
		this.serviceInstance.setInstanceId(serviceInstanceId);

		this.project = serviceDecomposition.getProject();
		this.owningEntity = serviceDecomposition.getOwningEntity();
		this.serviceProxy = serviceDecomposition.getServiceProxy();
		this.serviceInfo = serviceDecomposition.getServiceInfo();
	}

	/**
	 * Constructor taking a Service Decomposition JSON serialization
	 *
	 * @param catalogRestOutput
	 * @param serviceInstanceId
	 */
	public ServiceDecomposition(JSONObject jsonServiceDecomposition, String serviceInstanceId) {
		// TODO provide constructor implementation

	}

	// *****
	// GET and SET section
	/**
	 * Return just the service model portion of the Service Decomposition as a Java object. The service model object
	 * should support retrieval as JSON string that is formatted correctly for sending serviceModelInfo to Building
	 * Blocks.
	 *
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

	public Project getProject() {
		return project;
	}

	public OwningEntity getOwningEntity() {
		return owningEntity;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setOwningEntity(OwningEntity owningEntity) {
		this.owningEntity = owningEntity;
	}

	public List<VnfResource> getVnfResources() {
		return vnfResources;
	}

	public void setVnfResources(List<VnfResource> vnfResources) {
		this.vnfResources = vnfResources;
	}

	public void setConfigResources(List<ConfigResource> configResources) {
		this.configResources = configResources;
	}

	public List<ConfigResource> getConfigResources() {
		return configResources;
	}

	public void setNetworkResources(List<NetworkResource> networkResources) {
		this.networkResources = networkResources;
	}

	public List<NetworkResource> getNetworkResources() {
		return networkResources;
	}

	public List<AllottedResource> getAllottedResources() {
		return allottedResources;
	}

	public void setAllottedResources(List<AllottedResource> allottedResources) {
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

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public String getCallbackURN() {
		return callbackURN;
	}

	public void setCallbackURN(String callbackURN) {
		this.callbackURN = callbackURN;
	}

	public String getSdncVersion() {
		return sdncVersion;
	}

	public void setSdncVersion(String sdncVersion) {
		this.sdncVersion = sdncVersion;
	}

	// *****

	// *****
	// Access methods


	/**
	 * This method returns one combined list of Resources of All Types
	 *
	 * @return
	 */
	@JsonIgnore
	public List<Resource> getServiceResources() {
		ArrayList<Resource> serviceResources = new ArrayList();
		if (this.getAllottedResources() != null) {
			serviceResources.addAll(this.getAllottedResources());
		}
		if (this.getNetworkResources() != null) {
			serviceResources.addAll(this.getNetworkResources());
		}
		if (this.getVnfResources() != null) {
			serviceResources.addAll(this.getVnfResources());
		}
		if (this.getConfigResources() != null) {
			serviceResources.addAll(this.getConfigResources());
		}
		return serviceResources;
	}

	/**
	 * This method returns String representation of one combined list of Resources of All Types
	 */
	@JsonIgnore
	public String getServiceResourcesJsonString() {
		return getServiceNetworksJson() + getServiceVnfsJson() + getServiceAllottedResourcesJson()
				+ getServiceConfigResourcesJson();
	}

	/**
	 * Returns a JSON list of all Network Resource structures (i.e. the serialized NetworkDecomposition objects).
	 *
	 * @return
	 */
	@JsonIgnore
	public String getServiceNetworksJson() {
		return listToJson(this.getNetworkResources());
	}

	/**
	 * Returns a JSON list of all VnfResource structures (i.e. the serialized VnfResource objects).
	 *
	 * @return
	 */
	@JsonIgnore
	public String getServiceVnfsJson() {
		return listToJson(this.getVnfResources());
	}

	/**
	 * Returns a JSON list of all Allotted Resource structures (i.e. the serialized AllottedResource objects).
	 *
	 * @return
	 */
	@JsonIgnore
	public String getServiceAllottedResourcesJson() {
		return listToJson(this.getAllottedResources());
	}

	/**
	 * Returns a JSON list of all Config Resource structures (i.e. the serialized ConfigResource objects).
	 *
	 * @return
	 */
	@JsonIgnore
	public String getServiceConfigResourcesJson() {
		return listToJson(this.getConfigResources());
	}

	// TODO - define Resource Object ID
	@JsonIgnore
	public String getVnfResource(String resourceObjectId) {

		for (Resource resource : getServiceResources()) {
			if ("extracted information".equals(resourceObjectId)) {
				return resource.toJsonString();
			}
		}
		return "";
	}

	// Methods to add Resource to the list
	/**
	 * Add VNF resource to the list
	 *
	 * @param vnfResource
	 */
	public void addVnfResource(Resource vnfResource) {
		if (vnfResources == null) {
			vnfResources = new ArrayList<>();
		}
		this.vnfResources.add((VnfResource) vnfResource);
	}

	/**
	 * Add Network resource to the list
	 *
	 * @param networkResource
	 */
	public void addNetworkResource(Resource networkResource) {
		if (networkResources == null) {
			networkResources = new ArrayList<>();
		}
		this.networkResources.add((NetworkResource) networkResource);
	}

	/**
	 * Add Allotted resource to the list
	 *
	 * @param allottedResource
	 */
	public void addAllottedResource(Resource allottedResource) {
		if (allottedResources == null) {
			allottedResources = new ArrayList<>();
		}
		this.allottedResources.add((AllottedResource) allottedResource);
	}

	/**
	 * Add Config resource to the list
	 *
	 * @param allottedResource
	 */
	public void addConfigResource(Resource configResource) {
		if (configResources == null) {
			configResources = new ArrayList<>();
		}
		this.configResources.add((ConfigResource) configResource);
	}

	/**
	 * Add resource to the list Given a ResourceDecomposition (subclass) object, add it to the Service Decomposition (in
	 * the appropriate category, e.g. as a VNF, Network, or Allotted Resource). As dependencies are not currently
	 * supported, add it to the end of any ordered lists.
	 *
	 * @param resource
	 */
	public void addResource(Resource resource) {
		// create resource based upon type
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
			case CONFIGURATION:
				this.addConfigResource(resource);
				break;
			default:
				throw new IllegalArgumentException("Invalid resource type: " + resource.resourceType);
		}
	}

	/**
	 * Add resource to the list
	 *
	 * @param jsonResource
	 */
	public void addVnfResource(String jsonResource) throws JsonDecomposingException {
		VnfResource vnfResource = null;
		vnfResource = DecomposeJsonUtil.jsonToVnfResource(jsonResource);
		this.addVnfResource(vnfResource);
	}

	/**
	 * Add resource to the list
	 *
	 * @param jsonResource
	 */
	public void addNetworkResource(String jsonResource) throws JsonDecomposingException {
		NetworkResource networkResource = null;
		networkResource = DecomposeJsonUtil.jsonToNetworkResource(jsonResource);
		this.addNetworkResource(networkResource);
	}

	/**
	 * Add resource to the list
	 *
	 * @param Resource
	 */
	public void addAllottedResource(String jsonResource) throws JsonDecomposingException {
		AllottedResource allottedResource = null;
		allottedResource = DecomposeJsonUtil.jsonToAllottedResource(jsonResource);
		this.addAllottedResource(allottedResource);
	}

	/**
	 * Add resource to the list
	 *
	 * @param Resource
	 */
	public void addConfigResource(String jsonResource) throws JsonDecomposingException {
		ConfigResource configResource = null;
		configResource = DecomposeJsonUtil.jsonToConfigResource(jsonResource);
		this.addConfigResource(configResource);
	}

	/**
	 * Given a ResourceDecomposition (subclass) object, locate it in the Service Decomposition by its unique ID, and
	 * replace the current version with the new one. This method should support concurrency control via an
	 * auto-incrementing field in the ResourceDecomposition class.
	 *
	 * @param newResource
	 * @return TRUE if replacement was a success
	 */
	public boolean replaceResource(Resource newResource) {
		List resources = getResourceList(newResource);

		boolean result = false;
		for (Resource resource : (Iterable<Resource>) resources) {
			logger.debug("resource found");
			if (resource.resourceType == newResource.resourceType) {
				logger.debug("resource type matches");
				if (resource.getResourceId().equalsIgnoreCase(newResource.getResourceId())) {
					logger.debug("resource id matches");
					// returns TRUE if replacement is a success
					result = Collections.replaceAll(resources, resource, newResource);
				}
			}
		}
		return result;
	}

	/**
	 * Given a ResourceDecomposition as a JSON string, locate it in the Service Decomposition by its unique ID, and
	 * replace the current version with the new one.
	 *
	 * @param jsonString
	 * @return
	 */
	public boolean replaceResource(String jsonString) {
		// TODO: define unique ID for the Resource!
		return false;
	}

	/**
	 * Given a resource object ID, locate it in the Service Decomposition by its unique ID, and delete it.
	 *
	 * @param resource
	 * @return TRUE if delete was a success
	 */
	public boolean deleteResource(Resource resource) {
		List serviceResourceList = getResourceList(resource);
		for (Resource item : (Iterable<Resource>) serviceResourceList) {
			if (item.resourceType == resource.resourceType
					&& item.getResourceId().equalsIgnoreCase(resource.getResourceId())) {
				// returns TRUE if replacement is a success
				return serviceResourceList.remove(resource);
			}
		}
		return false;
	}

	/**
	 * Generic method to get List of Resource objects based on input resource's resourceType
	 *
	 * @param resource
	 * @return List matching the resourceType of resource
	 */
	public List getResourceList(Resource resource) {
		List resourceList;
		switch (resource.getResourceType()) {
			case VNF:
				resourceList = getVnfResources();
				break;
			case NETWORK:
				resourceList = getNetworkResources();
				break;
			case ALLOTTED_RESOURCE:
				resourceList = getAllottedResources();
				break;
			case CONFIGURATION:
				resourceList = getConfigResources();
				break;
			default:
				resourceList = new ArrayList<>();
		}
		return resourceList;
	}

	/**
	 * Generic method to set List of ResourceDecomposition objects
	 *
	 * @param resources
	 * @return
	 */
	public boolean setResourceList(List<Resource> resources) {
		if (resources != null && !resources.isEmpty()) {
			// create resource based upon type
			switch (resources.get(0).resourceType) {
				case VNF:
					this.setVnfResources((List<VnfResource>) (List<?>) resources);
					break;
				case NETWORK:
					this.setNetworkResources((List<NetworkResource>) (List<?>) resources);
					break;
				case ALLOTTED_RESOURCE:
					this.setAllottedResources((List<AllottedResource>) (List<?>) resources);
					break;
				case CONFIGURATION:
					this.setConfigResources((List<ConfigResource>) (List<?>) resources);
					break;
				default:
					throw new IllegalArgumentException("Invalid resource type: " + resources.get(0).resourceType);
			}
		}

		return false;
	}

	/**
	 *
	 * This method locates and returns a resource in a given Service Decomposition object by its unique resource id.
	 * Returns null if resource doesn't exist.
	 *
	 * @param resourceId - id of the resource
	 * @return resource
	 */
	@JsonIgnore
	public Resource getServiceResource(String resourceId) {
		List<Resource> resources = getServiceResources();
		for (Resource resource : resources) {
			if (resource.getResourceId().equalsIgnoreCase(resourceId)) {
				// match
				return resource;
			}
		}
		return null;
	}
}
