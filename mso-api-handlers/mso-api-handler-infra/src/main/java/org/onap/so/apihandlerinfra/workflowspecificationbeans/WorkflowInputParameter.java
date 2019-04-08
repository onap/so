/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.apihandlerinfra.workflowspecificationbeans;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"label", "inputType", "required", "validation", "soFieldName", "soPayloadLocation"})
public class WorkflowInputParameter {

    @JsonProperty("label")
    private String label;
    @JsonProperty("inputType")
    private String inputType;
    @JsonProperty("required")
    private Boolean required;
    @JsonProperty("validation")
    private List<Validation> validation = null;
    @JsonProperty("soFieldName")
    private String soFieldName;
    @JsonProperty("soPayloadLocation")
    private String soPayloadLocation;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowInputParameter() {}

    /**
     * 
     * @param validation
     * @param inputType
     * @param soPayloadLocation
     * @param label
     * @param required
     * @param soFieldName
     */
    public WorkflowInputParameter(String label, String inputType, Boolean required, List<Validation> validation,
            String soFieldName, String soPayloadLocation) {
        super();
        this.label = label;
        this.inputType = inputType;
        this.required = required;
        this.validation = validation;
        this.soFieldName = soFieldName;
        this.soPayloadLocation = soPayloadLocation;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    public WorkflowInputParameter withLabel(String label) {
        this.label = label;
        return this;
    }

    @JsonProperty("inputType")
    public String getInputType() {
        return inputType;
    }

    @JsonProperty("inputType")
    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public WorkflowInputParameter withInputType(String inputType) {
        this.inputType = inputType;
        return this;
    }

    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    @JsonProperty("required")
    public void setRequired(Boolean required) {
        this.required = required;
    }

    public WorkflowInputParameter withRequired(Boolean required) {
        this.required = required;
        return this;
    }

    @JsonProperty("validation")
    public List<Validation> getValidation() {
        return validation;
    }

    @JsonProperty("validation")
    public void setValidation(List<Validation> validation) {
        this.validation = validation;
    }

    public WorkflowInputParameter withValidation(List<Validation> validation) {
        this.validation = validation;
        return this;
    }

    @JsonProperty("soFieldName")
    public String getSoFieldName() {
        return soFieldName;
    }

    @JsonProperty("soFieldName")
    public void setSoFieldName(String soFieldName) {
        this.soFieldName = soFieldName;
    }

    public WorkflowInputParameter withSoFieldName(String soFieldName) {
        this.soFieldName = soFieldName;
        return this;
    }

    @JsonProperty("soPayloadLocation")
    public String getSoPayloadLocation() {
        return soPayloadLocation;
    }

    @JsonProperty("soPayloadLocation")
    public void setSoPayloadLocation(String soPayloadLocation) {
        this.soPayloadLocation = soPayloadLocation;
    }

    public WorkflowInputParameter withSoPayloadLocation(String soPayloadLocation) {
        this.soPayloadLocation = soPayloadLocation;
        return this;
    }

}
