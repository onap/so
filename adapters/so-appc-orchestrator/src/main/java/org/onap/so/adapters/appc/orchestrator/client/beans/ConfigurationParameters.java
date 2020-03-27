package org.onap.so.adapters.appc.orchestrator.client.beans;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigurationParameters {

    @JsonProperty("vnf_name")
    private String vnfName;
    @JsonProperty("book_name")
    private String bookName;
    @JsonProperty("node_list")
    private String nodeList;
    @JsonProperty("file_parameter_content")
    private String fileParameterContent;
    @JsonIgnore
    private Map<String, String> additionalProperties = new HashMap<>();


    @JsonProperty("vnf_name")
    public String getVnfName() {
        return vnfName;
    }

    @JsonProperty("vnf_name")
    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getNodeList() {
        return nodeList;
    }

    public void setNodeList(String nodeList) {
        this.nodeList = nodeList;
    }

    public String getFileParameterContent() {
        return fileParameterContent;
    }

    public void setFileParameterContent(String fileParameterContent) {
        this.fileParameterContent = fileParameterContent;
    }

    @JsonAnyGetter
    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

    public void setAdditionalProperties(Map<String, String> map) {
        this.additionalProperties = map;
    }

}
