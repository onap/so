package org.openecomp.mso.apihandler.common;

public class PostParameter {
	private String requestId;
	private boolean isBaseVfModule;
	private int recipeTimeout;
	private String requestAction;
	private String serviceInstanceId;
	private String vnfId;
	private String vfModuleId;
	private String volumeGroupId;
	private String networkId;
	private String configurationId;
	private String serviceType;
	private String vnfType;
	private String vfModuleType;
	private String networkType;
	private String requestDetails;
	private String apiVersion;
	private boolean aLaCarte;
	private String recipeParamXsd;
	private String requestUri;
	
	public PostParameter() {
		
	}
	public PostParameter(String requestId, boolean isBaseVfModule, int recipeTimeout, String requestAction,
			String serviceInstanceId, String vnfId, String vfModuleId, String volumeGroupId, String networkId,
			String configurationId, String serviceType, String vnfType, String vfModuleType, String networkType,
			String requestDetails, String apiVersion, boolean aLaCarte, String requestUri) {
		this.requestId = requestId;
		this.isBaseVfModule = isBaseVfModule;
		this.recipeTimeout = recipeTimeout;
		this.requestAction = requestAction;
		this.serviceInstanceId = serviceInstanceId;
		this.vnfId = vnfId;
		this.vfModuleId = vfModuleId;
		this.volumeGroupId = volumeGroupId;
		this.networkId = networkId;
		this.configurationId = configurationId;
		this.serviceType = serviceType;
		this.vnfType = vnfType;
		this.vfModuleType = vfModuleType;
		this.networkType = networkType;
		this.requestDetails = requestDetails;
		this.apiVersion = apiVersion;
		this.aLaCarte = aLaCarte;
		this.setRequestUri(requestUri);
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public boolean isBaseVfModule() {
		return isBaseVfModule;
	}

	public void setBaseVfModule(boolean isBaseVfModule) {
		this.isBaseVfModule = isBaseVfModule;
	}

	public int getRecipeTimeout() {
		return recipeTimeout;
	}

	public void setRecipeTimeout(int recipeTimeout) {
		this.recipeTimeout = recipeTimeout;
	}

	public String getRequestAction() {
		return requestAction;
	}

	public void setRequestAction(String requestAction) {
		this.requestAction = requestAction;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getVnfId() {
		return vnfId;
	}

	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}

	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}

	public String getVolumeGroupId() {
		return volumeGroupId;
	}

	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public String getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(String configurationId) {
		this.configurationId = configurationId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getVnfType() {
		return vnfType;
	}

	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}

	public String getVfModuleType() {
		return vfModuleType;
	}

	public void setVfModuleType(String vfModuleType) {
		this.vfModuleType = vfModuleType;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getRequestDetails() {
		return requestDetails;
	}

	public void setRequestDetails(String requestDetails) {
		this.requestDetails = requestDetails;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public boolean isaLaCarte() {
		return aLaCarte;
	}

	public void setaLaCarte(boolean aLaCarte) {
		this.aLaCarte = aLaCarte;
	}
	public String getRecipeParamXsd() {
		return recipeParamXsd;
	}
	public void setRecipeParamXsd(String recipeParamXsd) {
		this.recipeParamXsd = recipeParamXsd;
	}
	public String getRequestUri() {
		return requestUri;
	}
	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}
}