package org.onap.so.adapters.cnf.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = "true")
public class ResourceBundleEntity {

    @JsonProperty(value = "rb-name")
    private String rbName;

    @JsonProperty(value = "rb-version")
    private String rbVersion;

    @JsonProperty(value = "chart-name")
    private String chartName;

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "labels")
    private Map<String, String> labels;

    public String getRbName() {
        return rbName;
    }

    public void setRbName(String rbName) {
        this.rbName = rbName;
    }

    public String getRbVersion() {
        return rbVersion;
    }

    public void setRbVersion(String rbVersion) {
        this.rbVersion = rbVersion;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

}
