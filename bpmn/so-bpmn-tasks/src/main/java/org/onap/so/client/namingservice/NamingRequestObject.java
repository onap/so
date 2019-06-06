package org.onap.so.client.namingservice;

import java.util.HashMap;

public class NamingRequestObject {

    private HashMap<String, String> namingRequestMap = new HashMap<String, String>();

    public HashMap<String, String> getNamingRequestObjectMap() {
        return this.namingRequestMap;
    }

    public String getExternalKeyValue() {
        return namingRequestMap.get(NamingServiceConstants.NS_EXTERNAL_KEY);
    }

    public void setExternalKeyValue(String externalKey) {
        this.namingRequestMap.put(NamingServiceConstants.NS_EXTERNAL_KEY, externalKey);
    }

    public String getPolicyInstanceNameValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_POLICY_INSTANCE_NAME);
    }

    public void setPolicyInstanceNameValue(String policyInstanceName) {
        this.namingRequestMap.put(NamingServiceConstants.NS_POLICY_INSTANCE_NAME, policyInstanceName);
    }

    public String getNamingTypeValue() {
        return namingRequestMap.get(NamingServiceConstants.NS_NAMING_TYPE);
    }

    public void setNamingTypeValue(String namingType) {
        this.namingRequestMap.put(NamingServiceConstants.NS_NAMING_TYPE, namingType);
    }

    public String getResourceNameValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_RESOURCE_NAME);
    }

    public void setResourceNameValue(String resourceName) {
        namingRequestMap.put(NamingServiceConstants.NS_RESOURCE_NAME, resourceName);
    }

    public String getNfNamingCodeValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_NF_NAMING_CODE);
    }

    public void setNfNamingCodeValue(String nfNamingCode) {
        this.namingRequestMap.put(NamingServiceConstants.NS_NF_NAMING_CODE, nfNamingCode);
    }

    public String getServiceModelNameValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_SERVICE_MODEL_NAME);
    }

    public void setServiceModelNameValue(String serviceModelName) {
        this.namingRequestMap.put(NamingServiceConstants.NS_SERVICE_MODEL_NAME, serviceModelName);
    }

    public String getModelVersionValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_MODEL_VERSION);
    }

    public void setModelVersionValue(String modelVersion) {
        this.namingRequestMap.put(NamingServiceConstants.NS_MODEL_VERSION, modelVersion);
    }

    public String getNetworkNameValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_NETWORK_NAME);
    }

    public void setNetworkNameValue(String networkName) {
        this.namingRequestMap.put(NamingServiceConstants.NS_NETWORK_NAME, networkName);
    }

    public String getVpnNameValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_VPN_NAME);
    }

    public void setVpnNameValue(String vpnName) {
        this.namingRequestMap.put(NamingServiceConstants.NS_VPN_NAME, vpnName);
    }

    public String getZoneIdValue() {
        return this.namingRequestMap.get(NamingServiceConstants.NS_ZONE_ID);
    }

    public void setZoneIdValue(String zoneId) {
        this.namingRequestMap.put(NamingServiceConstants.NS_ZONE_ID, zoneId);
    }

}
