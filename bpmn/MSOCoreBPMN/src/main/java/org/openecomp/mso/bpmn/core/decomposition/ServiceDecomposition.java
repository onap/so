package org.openecomp.mso.bpmn.core.decomposition;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonRootName;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONObject;



/**
 * Service Decomposition Structure
 * This Java object contains service information:
 * - Service model info
 * - list of VNF resource's decompositon
 * - list of network resource's decompositon
 * - list of allotted resource's decompositon
 */
@JsonRootName(value = "serviceResources")
//@JsonTypeInfo(include=As.WRAPPER_OBJECT, use=Id.NAME)
public class ServiceDecomposition extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("modelInfo")
	private ModelInfo modelInfo;
	private ServiceInstanceData serviceInstanceData;
	@JsonProperty("vnfResource")
	private List <VnfResource>  vnfResources;
	@JsonProperty("networkResource")
	private List <NetworkResource>  networkResources;
	@JsonProperty("allottedResource")
	private List <AllottedResource>  allottedResources;
	
	public ServiceDecomposition () {
		super();
	}
	
	public ServiceDecomposition (ModelInfo modelInfo, ServiceInstanceData serviceInstanceData, List <VnfResource>  vnfResources, List <NetworkResource>  networkResources, List <AllottedResource>  allottedResources ) {
		//TODO provide constructor implementation
//		this.modelInfo = modelInfo;
//		this.serviceInstanceData = serviceInstanceData;
//		this.vnfResources = vnfResources;
//		this.networkResources = networkResources;
//		this.allottedResources = allottedResources;
		
	}
	
	public ServiceDecomposition (String catalogRestOutput, String serviceInstanceId) {
		//TODO provide constructor implementation
		
		this.modelInfo = this.JsonToServiceDecomposition(catalogRestOutput).getModelInfo();
		this.vnfResources = this.JsonToServiceDecomposition(catalogRestOutput).getServiceVnfs();
		this.allottedResources = this.JsonToServiceDecomposition(catalogRestOutput).getServiceAllottedResources();
		this.networkResources = this.JsonToServiceDecomposition(catalogRestOutput).getServiceNetworks();
		this.serviceInstanceData = new ServiceInstanceData();
		this.serviceInstanceData.setInstanceId(serviceInstanceId);
	}
	public ServiceDecomposition (JSONObject catalogRestOutput, String serviceInstanceId) {
		//TODO provide constructor implementation
	}
	//GET and SET
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}
	public ServiceInstanceData getServiceInstanceData() {
		return serviceInstanceData;
	}
	public void setServiceInstanceData(ServiceInstanceData serviceInstanceData) {
		this.serviceInstanceData = serviceInstanceData;
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
	
	// methods to add to the list
	public void addVnfResource(VnfResource vnfResource) {
		if (vnfResources == null){
			vnfResources = new ArrayList<VnfResource>();
		}
		this.vnfResources.add(vnfResource);
	}
	public void addNetworkResource(NetworkResource networkResource) {
		if (networkResources == null){
			networkResources = new ArrayList<NetworkResource>();
		}
		this.networkResources.add(networkResource);
	}
	public void addAllottedResource(AllottedResource allottedResource) {
		if (allottedResources == null){
			allottedResources = new ArrayList<AllottedResource>();
		}
		this.allottedResources.add(allottedResource);
	}
	
//	//TODO - IF NEEDED, update below methods to return one resource decomposition by (?) ID, not index - used temporarily
//	public VnfResource getnfResource(int index) {
//		
////		Iterator<VnfResource> iter = vnfResources.iterator();
////		while (iter.hasNext()) {
////			VnfResource vnfResource = iter.next();
////			vnfResource.getModelInfo().getModelInvariantId();
////		}
//		return this.vnfResources.get(index);
//	}
//	public NetworkResource getNetworkResource( int index) {
//		return this.networkResources.get(index);
//	}
//	public AllottedResource getAllottedResource(int index) {
//		return this.allottedResources.get(index);
//	}
	
	@JsonIgnore
	public List<ResourceDecomposition> getServiceResources(){
		ArrayList serviceResources = new ArrayList();
		serviceResources.addAll(this.getServiceNetworks());
		serviceResources.addAll(this.getServiceVnfs());
		serviceResources.addAll(this.getServiceAllottedResources());
		return serviceResources;
	}
	
	@JsonIgnore
	public String getServiceResourcesJsonString(){
		StringBuffer serviceResourcesJsonStringBuffer = new StringBuffer();
		serviceResourcesJsonStringBuffer.append(listToJson((this.getServiceNetworks())));
		serviceResourcesJsonStringBuffer.append(listToJson((this.getServiceVnfs())));
		serviceResourcesJsonStringBuffer.append(listToJson((this.getServiceAllottedResources())));
		return serviceResourcesJsonStringBuffer.toString();
	}
	
	//return String representation of JSON
	@Override
	public String toString(){
		return "string representation";
	}

}
