package org.onap.so.serviceinstancebeans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonRootName(value = "vnfs")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Pnfs implements Serializable {

    private static final long serialVersionUID = 8081495240474276501L;
    @JsonProperty("modelInfo")
    protected ModelInfo modelInfo;
    @JsonProperty("cloudConfiguration")
    protected CloudConfiguration cloudConfiguration;
    @JsonProperty("instanceName")
    protected String instanceName;
    @JsonProperty("platform")
    protected Platform platform;
    @JsonProperty("lineOfBusiness")
    protected LineOfBusiness lineOfBusiness;
    @JsonProperty("productFamilyId")
    protected String productFamilyId;
    @JsonProperty("instanceParams")
    private List<Map<String, String>> instanceParams = new ArrayList<>();


    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public CloudConfiguration getCloudConfiguration() {
        return cloudConfiguration;
    }

    public void setCloudConfiguration(CloudConfiguration cloudConfiguration) {
        this.cloudConfiguration = cloudConfiguration;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }

    public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }

    public String getProductFamilyId() {
        return productFamilyId;
    }

    public void setProductFamilyId(String productFamilyId) {
        this.productFamilyId = productFamilyId;
    }

    public List<Map<String, String>> getInstanceParams() {
        return instanceParams;
    }

    public void setInstanceParams(List<Map<String, String>> instanceParams) {
        this.instanceParams = instanceParams;
    }

    @Override
    public String toString() {
        return "Vnfs [modelInfo=" + modelInfo + ", cloudConfiguration=" + cloudConfiguration + ", instanceName="
                + instanceName + ", platform=" + platform + ", " + "lineOfBusiness=" + lineOfBusiness
                + ", productFamilyId=" + productFamilyId + ", instanceParams=" + instanceParams;
    }

}
