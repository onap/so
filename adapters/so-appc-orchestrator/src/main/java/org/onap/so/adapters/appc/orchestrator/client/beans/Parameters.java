package org.onap.so.adapters.appc.orchestrator.client.beans;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"vm-id", "identity-url", "old_software_version", "new_software_version", "operations_timeout",
        "request-parameters", "configuration-parameters"})
public class Parameters {

    @JsonProperty("vm-id")
    private String vmId;
    @JsonProperty("identity-url")
    private String identityUrl;
    @JsonProperty("operations_timeout")
    private String operationsTimeout;
    @JsonProperty("existing_software_version")
    private String existingSoftwareVersion;
    @JsonProperty("new_software_version")
    private String newSoftwareVersion;
    @JsonProperty("request-parameters")
    private RequestParameters requestParameters;
    @JsonProperty("configuration-parameters")
    private ConfigurationParameters configurationParameters;

    @JsonProperty("request-parameters")
    public RequestParameters getRequestParameters() {
        return requestParameters;
    }

    @JsonProperty("request-parameters")
    public void setRequestParameters(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    @JsonProperty("configuration-parameters")
    public ConfigurationParameters getConfigurationParameters() {
        return configurationParameters;
    }

    @JsonProperty("configuration-parameters")
    public void setConfigurationParameters(ConfigurationParameters configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    @JsonProperty("vm-id")
    public String getVmId() {
        return vmId;
    }

    @JsonProperty("vm-id")
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @JsonProperty("identity-url")
    public String getIdentityUrl() {
        return identityUrl;
    }

    @JsonProperty("identity-url")
    public void setIdentityUrl(String identityUrl) {
        this.identityUrl = identityUrl;
    }

    @JsonProperty("operations_timeout")
    public String getOperationsTimeout() {
        return operationsTimeout;
    }

    @JsonProperty("operations_timeout")
    public void setOperationsTimeout(String operationsTimeout) {
        this.operationsTimeout = operationsTimeout;
    }

    @JsonProperty("existing_software_version")
    public String getExistingSoftwareVersion() {
        return existingSoftwareVersion;
    }

    @JsonProperty("existing_software_version")
    public void setExistingSoftwareVersion(String existingSoftwareVersion) {
        this.existingSoftwareVersion = existingSoftwareVersion;
    }

    @JsonProperty("new_software_version")
    public String getNewSoftwareVersion() {
        return newSoftwareVersion;
    }

    @JsonProperty("new_software_version")
    public void setNewSoftwareVersion(String newSoftwareVersion) {
        this.newSoftwareVersion = newSoftwareVersion;
    }


}
