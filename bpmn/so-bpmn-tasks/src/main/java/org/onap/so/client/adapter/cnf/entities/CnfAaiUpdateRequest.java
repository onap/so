package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class CnfAaiUpdateRequest {

    @JsonProperty("instanceId")
    private String instanceId;

    @JsonProperty("cloudRegion")
    private String cloudRegion;

    @JsonProperty("cloudOwner")
    private String cloudOwner;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("callbackUrl")
    private String callbackUrl;

    @JsonProperty("genericVnfId")
    private String genericVnfId;

    @JsonProperty("vfModuleId")
    private String vfModuleId;


    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(String cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getGenericVnfId() {
        return genericVnfId;
    }

    public void setGenericVnfId(String genericVnfId) {
        this.genericVnfId = genericVnfId;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    @Override
    public String toString() {
        return "CnfAaiUpdateRequest [instanceId=" + instanceId + ", cloudRegion=" + cloudRegion + ", cloudOwner="
                + cloudOwner + ", tenantId=" + tenantId + ", callbackUrl=" + callbackUrl + ", genericVnfId="
                + genericVnfId + ", vfModuleId=" + vfModuleId + " ]";
    }

}
